package typings.atStardustDashUiReactDashComponentDashRef.distEsTypesMod

import slinky.core.facade.ReactElement
import typings.react.reactMod.Ref
import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

trait RefProps extends js.Object {
  var children: ReactElement
  /**
    * Called when a child component will be mounted or updated.
    *
    * @param {HTMLElement} node - Referred node.
    */
  var innerRef: Ref[_]
}

object RefProps {
  @scala.inline
  def apply(children: ReactElement, innerRef: Ref[_] = null): RefProps = {
    val __obj = js.Dynamic.literal(children = children)
    if (innerRef != null) __obj.updateDynamic("innerRef")(innerRef.asInstanceOf[js.Any])
    __obj.asInstanceOf[RefProps]
  }
}

