package typings.reactDashTransitionDashGroup.transitionGroupMod

import typings.reactDashTransitionDashGroup.reactDashTransitionDashGroupStrings.abbr
import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

trait IntrinsicTransitionGroupProps extends js.Object {
  var component: js.UndefOr[abbr] = js.undefined
}

object IntrinsicTransitionGroupProps {
  @scala.inline
  def apply(component: abbr = null): IntrinsicTransitionGroupProps = {
    val __obj = js.Dynamic.literal()
    if (component != null) __obj.updateDynamic("component")(component)
    __obj.asInstanceOf[IntrinsicTransitionGroupProps]
  }
}

