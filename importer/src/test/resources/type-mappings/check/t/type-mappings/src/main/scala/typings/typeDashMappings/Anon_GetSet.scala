package typings.typeDashMappings

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

trait Anon_GetSet extends js.Object {
  def get(): String
  def set(v: String): Unit
}

object Anon_GetSet {
  @scala.inline
  def apply(get: () => String, set: String => Unit): Anon_GetSet = {
    val __obj = js.Dynamic.literal(get = js.Any.fromFunction0(get), set = js.Any.fromFunction1(set))
  
    __obj.asInstanceOf[Anon_GetSet]
  }
}

