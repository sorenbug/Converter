package typings.typeDashMappings

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

/* Inlined std.Pick<{  name  :string,   age  :number}, 'name' | 'age'> */
trait U extends js.Object {
  var age: Double
  var name: String
}

object U {
  @scala.inline
  def apply(age: Double, name: String): U = {
    val __obj = js.Dynamic.literal(age = age, name = name)
  
    __obj.asInstanceOf[U]
  }
}

