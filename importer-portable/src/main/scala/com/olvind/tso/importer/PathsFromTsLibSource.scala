package com.olvind.tso
package importer

import com.olvind.tso.ts._

object PathsFromTsLibSource {
  def apply(source: Source.TsLibSource): Set[Source.TsHelperFile] = {

    val files: Seq[InFile] =
      source match {
        case Source.StdLibSource(_, files, _)                             => files
        case f @ Source.FromFolder(_, TsIdentLibrarySimple("typescript")) => allTopLevel(f.folder)
        case f: Source.FromFolder =>
          /* There are often whole trees parallel to what is specified in `typings` (or similar). This ignores them */
          val bound = f.shortenedFiles.headOption.map(_.folder).getOrElse(f.folder)
          filesFrom(bound, f.libName)
      }

    files.map(file => Source.TsHelperFile(file, source, LibraryResolver.moduleNameFor(source, file))).toSet
  }

  val V  = "v[\\d\\.]+".r
  val TS = "ts[\\d\\.]+".r

  def allTopLevel(folder: InFolder): Seq[InFile] =
    os.list(folder.path)
      .filter(_.last.endsWith("d.ts"))
      .to[Seq]
      .map(InFile.apply)

  def filesFrom(bound: InFolder, libName: TsIdentLibrary): Seq[InFile] = {
    def skip(dir: os.Path) =
      dir.last match {
        case "node_modules" => true
        /* The presence of these folders mostly means unnecessary duplication.
           If we desperately want these perhaps the user can configure that,
            though it won't be as easy as just discarding them
         */
        case "amd" => true
        case "umd" => true
        case "es"  => true
        case "es6" => true
        /* DefinitelyTyped uses this pattern for newer versions of typescript. We just use the default */
        case TS() => true
        /* DefinitelyTyped uses this pattern for old versions of the library */
        case V() => true
        case _   => false
      }

    os.walk(bound.path, skip)
      .filter(_.last.endsWith(".d.ts"))
      .filterNot(_.last.contains(".src.")) // filter out files like highlight.src.d.ts,
      .to[Seq]
      .map(InFile.apply)
  }
}
