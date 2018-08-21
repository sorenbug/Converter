package com.olvind.tso
package importer

import com.olvind.logging.Logger
import com.olvind.tso.ts._

import scala.collection.mutable

object ResolveExternalReferences {

  case class Result(
      rewritten:      TsParsedFile,
      resolvedDeps:   Set[TsSource],
      unresolvedDeps: Set[TsIdentModule]
  )

  def apply(sources: Seq[InFolder], source: TsSource, tsParsedFile: TsParsedFile, logger: Logger[Unit]): Result = {
    val imported: Set[TsIdentModule] =
      tsParsedFile.imports.collect {
        case TsImport(_, TsImporteeFrom(from))     => from
        case TsImport(_, TsImporteeRequired(from)) => from
      }.toSet ++ tsParsedFile.exports.collect {
        case TsExport(_, _, TsExporteeNames(_, Some(from))) => from
      }

    /**
      * Todo: `InferredDependency` takes care of undeclared node dependency.
      * However, that is not solid enough when there actually exists a library
      * with the same name as the requested module.
      */
    val doResolve: TsIdentModule => Option[(TsSource, TsIdentModule)] = {
      case TsIdentModule(None, "events" :: Nil) => None
      case name                                 => libraryResolver(sources, source, name.value)
    }
    val v = new V(doResolve, imported)

    val after = v.visitTsParsedFile(TreeScope(source.inLibrary.libName, Map.empty, logger))(tsParsedFile)

    Result(after, v.foundSources.to[Set], v.notFound.to[Set])

  }

  private class V(doResolve: TsIdentModule => Option[(TsSource, TsIdentModule)], imported: Set[TsIdentModule])
      extends TreeVisitorScopedChanges {
    val foundSources = mutable.Set.empty[TsSource]
    val notFound     = mutable.Set.empty[TsIdentModule]

    private def resolveAndStore(name: TsIdentModule): TsIdentModule =
      doResolve(name) match {
        case Some((found: TsSource, moduleName)) =>
          foundSources += found
          moduleName
        case None =>
          notFound += name
          name
      }

    override def enterTsContainer(t: TreeScope)(x: TsContainer): TsContainer =
      x match {
        case m: TsDeclModule =>
          val newName: Option[TsIdentModule] = doResolve(m.name) flatMap {
            case (_, newName) if newName =/= m.name => Some(newName)
            case _                                  => None
          }

          val isWithinModule = t.`..`.surroundingTsContainer.fold(false)(_.isModule)

          if (newName.isDefined || imported(m.name) || isWithinModule) {
            t.logger.info(s"Inferred augmented module $newName (${m.name})")
            TsAugmentedModule(newName.getOrElse(m.name), m.members, m.codePath, m.jsLocation)
          } else {
            m
          }

        case other => other
      }

    override def enterTsExporteeStar(t: TreeScope)(x: TsExporteeStar): TsExporteeStar =
      x.copy(from = resolveAndStore(x.from))

    override def enterTsImporteeRequired(t: TreeScope)(x: TsImporteeRequired): TsImporteeRequired =
      x.copy(from = resolveAndStore(x.from))

    override def enterTsImporteeFrom(t: TreeScope)(x: TsImporteeFrom): TsImporteeFrom =
      x.copy(from = resolveAndStore(x.from))

    override def enterTsExporteeNames(t: TreeScope)(x: TsExporteeNames): TsExporteeNames =
      x.fromOpt.fold(x)(from => x.copy(fromOpt = Some(resolveAndStore(from))))
  }

}