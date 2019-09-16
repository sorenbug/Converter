package typings.typeDashMappings

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

/* Inlined std.Pick<{  name  :string,   age  :number}, 'age'> */
trait V extends js.Object {
  var age: Double
}

object V {
  @scala.inline
  def apply(age: Double): V = {
    val __obj = js.Dynamic.literal(age = age)
  
    __obj.asInstanceOf[V]
  }
}

