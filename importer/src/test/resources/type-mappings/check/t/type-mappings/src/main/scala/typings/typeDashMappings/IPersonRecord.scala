package typings.typeDashMappings

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

/* Inlined parent type-mappings.PersonRecord */
trait IPersonRecord extends js.Object {
  var age: String
  var name: String
}

object IPersonRecord {
  @scala.inline
  def apply(age: String, name: String): IPersonRecord = {
    val __obj = js.Dynamic.literal(age = age, name = name)
  
    __obj.asInstanceOf[IPersonRecord]
  }
}

