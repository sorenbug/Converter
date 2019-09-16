package typings.typeDashMappings

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

/* Inlined std.Pick<type-mappings.Person, 'name'> */
trait NamePerson extends js.Object {
  var name: js.Any
}

object NamePerson {
  @scala.inline
  def apply(name: js.Any): NamePerson = {
    val __obj = js.Dynamic.literal(name = name)
  
    __obj.asInstanceOf[NamePerson]
  }
}

