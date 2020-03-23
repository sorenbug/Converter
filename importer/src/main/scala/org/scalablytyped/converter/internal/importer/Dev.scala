package org.scalablytyped.converter
package internal
package importer

import java.io.FileWriter
import java.time.LocalDateTime
import java.util.concurrent._

import com.olvind.logging
import com.olvind.logging.LogLevel
import org.scalablytyped.converter.internal.importer.build._
import org.scalablytyped.converter.internal.importer.documentation.Npmjs
import org.scalablytyped.converter.internal.phases.{PhaseListener, PhaseRes, PhaseRunner, RecPhase}
import org.scalablytyped.converter.internal.scalajs.{Name, Versions}
import org.scalablytyped.converter.internal.ts.CalculateLibraryVersion.PackageJsonOnly
import org.scalablytyped.converter.internal.ts._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object Dev {
  object paths {
    val base          = files.existing(constants.defaultCacheFolder / 'dev)
    val publishFolder = files.existing(base / 'publish)
    val parseCache    = files.existing(base / 'parseCache)
    val logs          = files.existing(base / 'logs)
    val npm           = files.existing(base / 'typescript)
    val node_modules  = files.existing(npm / 'node_modules)
    val out           = files.existing(base / 'out)
  }

  val parallelLibraries = 20
  val parallelScalas    = 4
  val parseCache        = Some(paths.parseCache.toNIO)
  val pool              = new ForkJoinPool(parallelLibraries)
  val ec                = ExecutionContext.fromExecutorService(pool)

  val logsFolder  = files.existing(paths.logs)
  val t0          = System.currentTimeMillis
  val flavour     = Flavour.Normal
  val org         = "org.scalablytyped"
  val repo        = "https://github.com/oyvindberg/ScalablyTyped.git"
  val projectName = "ScalablyTyped"

  val logger = {
    val logFile = new FileWriter((logsFolder / s"${constants.DateTimePattern format LocalDateTime.now}.log").toIO)
    logging.appendable(logFile) zipWith logging.stdout.filter(LogLevel.warn)
  }

  object WithVersion {
    val NameBackslashVersion = "([^\\\\]+)\\\\(.*)".r
    def unapply(str: String): Option[(String, String)] =
      str match {
        case NameBackslashVersion(name, version) => Some((name, version))
        case _                                   => None
      }
  }

  def main(args: Array[String]): Unit = {
    require(args.nonEmpty, "supply names of libraries, possibly with an / for versions")

    {
      import ammonite.ops.%
      implicit val wd = paths.npm
      val libs = args
        .map {
          case WithVersion(name, version) => s"$name@$version"
          case other                      => other
        }
      % yarn ('add, "typescript", libs.toSeq)
    }

    val wantedLibs = args.map {
      case WithVersion(name, _) => TsIdentLibrary(name)
      case name                 => TsIdentLibrary(name)
    }.toSet

    val shared = SharedInput(
      shouldUseScalaJsDomTypes = false,
      outputPackage            = Name.typings,
      enableScalaJsDefined     = Selection.AllExcept(Libraries.Slow.to[Seq]: _*),
      flavour                  = flavour,
      wantedLibs               = wantedLibs,
      ignoredLibs              = Set(),
      ignoredModulePrefixes    = Set(),
      stdLibs                  = IArray("es6"),
      expandTypeMappings       = EnabledTypeMappingExpansion.DefaultSelection,
      versions                 = Versions(Versions.Scala213, Versions.ScalaJs1),
    )

    val compilerF: Future[BloopCompiler] =
      BloopCompiler(logger.filter(LogLevel.debug).void, shared.versions, failureCacheFolderOpt = None)(ec)

    val compiler = Await.result(compilerF, Duration.Inf)

    val Source.FromNodeModules(sources, folders, libraryResolver, stdLibSource) =
      Source.fromNodeModules(InFolder(paths.node_modules), shared)

    val Pipeline: RecPhase[Source, PublishedSbtProject] =
      RecPhase[Source]
        .next(
          new Phase1ReadTypescript(
            calculateLibraryVersion = PackageJsonOnly,
            resolve                 = libraryResolver,
            ignored                 = shared.ignoredLibs,
            ignoredModulePrefixes   = shared.ignoredModulePrefixes,
            stdlibSource            = stdLibSource,
            pedantic                = false,
            parser                  = PersistingParser(parseCache, folders, logger.void),
            expandTypeMappings      = shared.expandTypeMappings,
          ),
          "typescript",
        )
        .next(
          new Phase2ToScalaJs(
            pedantic             = false,
            enableScalaJsDefined = shared.enableScalaJsDefined,
            outputPkg            = shared.outputPackage,
          ),
          "scala.js",
        )
        .next(new PhaseFlavour(flavourImpl.fromInput(shared)), flavour.toString)
        .next(
          new Phase3Compile(
            resolve                    = libraryResolver,
            versions                   = shared.versions,
            compiler                   = compiler,
            targetFolder               = paths.out,
            projectName                = projectName,
            organization               = org,
            publishUser                = "publishUser",
            publishFolder              = paths.publishFolder,
            metadataFetcher            = Npmjs.No,
            softWrites                 = true,
            flavour                    = flavourImpl.fromInput(shared),
            generateScalaJsBundlerFile = false,
            ensureSourceFilesWritten   = true,
          ),
          "build",
        )

    val results: Map[Source, PhaseRes[Source, PublishedSbtProject]] =
      sources.toVector
        .map(source =>
          source -> PhaseRunner.go(Pipeline, source, Nil, (_: Source) => logger.void, PhaseListener.NoListener),
        )
        .toMap

    val successes: Map[Source, PublishedSbtProject] = {
      def go(source: Source, p: PublishedSbtProject): Map[Source, PublishedSbtProject] =
        Map(source -> p) ++ p.project.deps.flatMap { case (k, v) => go(k, v) }

      results.collect { case (s, PhaseRes.Ok(res)) => go(s, res) }.reduceOption(_ ++ _).getOrElse(Map.empty)
    }

    val failures: Map[Source, Either[Throwable, String]] =
      results.collect { case (_, PhaseRes.Failure(errors)) => errors }.reduceOption(_ ++ _).getOrElse(Map.empty)

    println(s"Successes: ${successes.keys.map(_.libName.value).mkString(", ")}")
    println(s"Failures: ${failures.keys.map(_.libName.value).mkString(", ")}")

    val td = System.currentTimeMillis - t0
    logger warn td
    pool.shutdown()
  }
}
