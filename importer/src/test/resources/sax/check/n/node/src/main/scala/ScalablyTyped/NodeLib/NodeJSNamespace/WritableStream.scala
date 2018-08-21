package ScalablyTyped
package NodeLib
package NodeJSNamespace

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

@js.native
trait WritableStream extends NodeLib.NodeJSNamespace.EventEmitter {
  val writable: scala.Boolean = js.native
  def end(str: java.lang.String): scala.Unit = js.native
  def end(str: java.lang.String, encoding: java.lang.String): scala.Unit = js.native
  def end(str: java.lang.String, encoding: java.lang.String, cb: js.Function): scala.Unit = js.native
}
