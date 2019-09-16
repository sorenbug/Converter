package typings.typeDashMappings

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation._

/* Inlined std.Required<std.Pick<type-mappings.CSSProperties, 'fontFamily' | 'fontSize' | 'fontWeight' | 'color'>> & std.Partial<std.Pick<type-mappings.CSSProperties, 'letterSpacing' | 'lineHeight' | 'textTransform'>> */
trait TypographyStyle extends js.Object {
  var color: js.Any
  var fontFamily: js.Any
  var fontSize: js.Any
  var fontWeight: js.Any
  var letterSpacing: js.UndefOr[js.Any] = js.undefined
  var lineHeight: js.UndefOr[js.Any] = js.undefined
  var textTransform: js.UndefOr[js.Any] = js.undefined
}

object TypographyStyle {
  @scala.inline
  def apply(
    color: js.Any,
    fontFamily: js.Any,
    fontSize: js.Any,
    fontWeight: js.Any,
    letterSpacing: js.Any = null,
    lineHeight: js.Any = null,
    textTransform: js.Any = null
  ): TypographyStyle = {
    val __obj = js.Dynamic.literal(color = color, fontFamily = fontFamily, fontSize = fontSize, fontWeight = fontWeight)
    if (letterSpacing != null) __obj.updateDynamic("letterSpacing")(letterSpacing)
    if (lineHeight != null) __obj.updateDynamic("lineHeight")(lineHeight)
    if (textTransform != null) __obj.updateDynamic("textTransform")(textTransform)
    __obj.asInstanceOf[TypographyStyle]
  }
}

