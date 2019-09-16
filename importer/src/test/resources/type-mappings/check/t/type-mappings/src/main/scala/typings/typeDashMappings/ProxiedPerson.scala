package typings.typeDashMappings

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

trait ProxiedPerson extends js.Object {
  var age: Anon_Get
  var name: Anon_GetSet
}

object ProxiedPerson {
  @scala.inline
  def apply(age: Anon_Get, name: Anon_GetSet): ProxiedPerson = {
    val __obj = js.Dynamic.literal(age = age, name = name)
  
    __obj.asInstanceOf[ProxiedPerson]
  }
}

