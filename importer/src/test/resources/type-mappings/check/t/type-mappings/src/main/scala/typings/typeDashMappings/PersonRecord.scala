package typings.typeDashMappings

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

/* Inlined std.Record<'name' | 'age', string> */
trait PersonRecord extends js.Object {
  var age: String
  var name: String
}

object PersonRecord {
  @scala.inline
  def apply(age: String, name: String): PersonRecord = {
    val __obj = js.Dynamic.literal(age = age, name = name)
  
    __obj.asInstanceOf[PersonRecord]
  }
}

