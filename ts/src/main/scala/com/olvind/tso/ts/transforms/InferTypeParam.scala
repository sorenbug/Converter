package com.olvind.tso
package ts
package transforms

import seqs._

/**
  * Given
  * ```
  * type Foo<T = "div">{
  *   component: T
  * }
  * ```
  * We infer the T to always be "div"
  *
  * ```
  * type Foo {
  *   component: "div"
  * }
  * ```
  *
  * This is not necessarily good, but we don't have an encoding to make this as flexible in scala :/
  */
object InferTypeParam extends TreeTransformationScopedChanges {
  def isDeterminedByMember(i: TsDeclInterface, name: TsIdent): Boolean =
    i.members.exists {
      case TsMemberProperty(_, _, _, Some(TsTypeRef(_, TsQIdent(List(`name`)), _)), _, _, _, _) => true
      case _                                                                                    => false
    }

  override def enterTsDeclInterface(t: TsTreeScope)(x: TsDeclInterface): TsDeclInterface =
    x.tparams.reverse.takeWhile {
      case TsTypeParam(_, name, _, Some(_: TsTypeLiteral)) if isDeterminedByMember(x, name) => true
      case _ => false

    } match {
      case Nil => x
      case toInline =>
        val replacements: Map[TsType, TsType] =
          toInline.map(tp => TsTypeRef.of(tp.name) -> tp.default.get).toMap

        new TypeRewriter(x).visitTsDeclInterface(replacements)(x.copy(tparams = x.tparams.dropRight(toInline.length)))
    }
}
