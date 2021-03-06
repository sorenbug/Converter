package org.scalablytyped.converter.internal
package ts

object TypeParamsReferencedInTree {
  def apply(inScope: Map[TsIdent, TsTypeParam], tree: TsTree): IArray[TsTypeParam] = {

    val locallyDefined: Set[TsIdent] =
      TsTreeTraverse
        .collect(tree) {
          case HasTParams(tparams) => tparams.map(_.name)
        }
        .flatten
        .toSet

    val referencedInTree: IArray[TsIdent] =
      TsTreeTraverse
        .collect(tree) {
          case TsTypeRef(_, TsQIdent(IArray.exactlyOne(unprefixedName)), _) if inScope.contains(unprefixedName) =>
            unprefixedName
        }
        .distinct

    referencedInTree.filterNot(locallyDefined).map(inScope)
  }
}
