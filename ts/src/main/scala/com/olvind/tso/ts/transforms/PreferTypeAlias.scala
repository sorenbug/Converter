package com.olvind.tso
package ts
package transforms
import scala.collection.mutable

/**
  * Perform two related rewrites to make the resulting scala code easier to use
  * ```typescript
  *
  * interface Foo {bar: number}
  * interface Bar extends Foo {}
  * ```
  * =>
  * ```typescript
  * interface Foo {bar: number}
  * type Bar = Foo
  * ````
  *
  * We do this rewrite because on the typescript side these two are equal,
  *  while in nominally typed Scala we need to cast a `Foo` to a `Bar`
  *
  * Relatedly, do this:
  *
  * ```typescript
  * interface Foo {&LT;T extends object&GT;(a: T) => void}
  * ```
  * =>
  * ```typescript
  * type Foo = (a: object) => void;
  * ```
  *
  * We do this rewrite because in Scala we have no way to instantiate a new instance of `Foo``
  */
object PreferTypeAlias extends TreeVisitorScopedChanges {

  private object IsFunction {
    def unapply(i: TsDeclInterface): Option[TsDeclTypeAlias] =
      if (i.members.size =/= 1 || i.inheritance.nonEmpty) None
      else {
        i.members.head match {
          case call: TsMemberCall =>
            Some(TsDeclTypeAlias(i.comments, i.declared, i.name, i.tparams, TsTypeFunction(call.signature), i.codePath))
          case _ => None
        }
      }
  }

  /**
    * Typescript and Scala share limitations on recursive/circular types.
    * For instance this is not allowed
    * ```typescript
    * type T = T[] | number;
    * ```
    *
    * Howevery, in both languages you can the the know by using an interface/trait
    * ```typescript
    * type T = TArray | number
    * interface TArray extends T[] [}
    * ```
    *
    * So to avoid compilation failure after we simplify, we leave it to the user of the generated
    *  code to cast appropriately
    */
  def hasCircularReference(self: TsIdent, cache: mutable.Set[TsTypeRef], scope: TreeScope, tree: TsTree): Boolean =
    TreeTraverse.collect(tree) { case x: TsIdent if x === self => x } match {
      case Nil =>
        val refs = TreeTraverse.collect(tree) { case x: TsTypeRef => x }.to[Set]
        refs exists { ref =>
          if (cache(ref)) false
          else
            scope
              .lookupTypeIncludeScope(ref.name, /* lazy handling of type parameters */ skipValidation = true)
              .exists {
                case (found, newScope) =>
                  cache += ref
                  hasCircularReference(self, cache, newScope, found)
              }
        }
      case circularReferences =>
        scope.logger.info(
          s"Could not simplify ${self.value} to function type alias because of circular references $circularReferences"
        )
        true
    }

  override def enterTsDecl(t: TreeScope)(x: TsDecl): TsDecl = x match {
    case i @ TsDeclInterface(comments, declared, name, tparams, inheritance, Nil, codePath) if inheritance.nonEmpty =>
      val intersect = TsTypeIntersect(inheritance)
      if (hasCircularReference(i.name, mutable.Set(), t, intersect))
        i
      else {
        t.logger.info("Simplified to type alias")
        TsDeclTypeAlias(comments, declared, name, tparams, intersect, codePath)
      }
    case IsFunction(typeAlias) =>
      if (hasCircularReference(typeAlias.name, mutable.Set(), t, typeAlias.alias)) x
      else {
        t.logger.info("Simplified to function type alias")
        typeAlias
      }
    case other => other
  }
}