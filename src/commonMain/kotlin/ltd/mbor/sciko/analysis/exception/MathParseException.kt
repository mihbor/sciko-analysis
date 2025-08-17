package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.ExceptionContextProvider
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats

/**
 * Class to signal parse failures.
 *
 * @since 2.2
 */
class MathParseException : MathIllegalStateException, ExceptionContextProvider {
  /**
   * @param wrong Bad string representation of the object.
   * @param position Index, in the `wrong` string, that caused the
   * parsing to fail.
   * @param type Class of the object supposedly represented by the
   * `wrong` string.
   */
  constructor(
    wrong: String,
    position: Int,
    type: Class<*>
  ) {
    getContext().addMessage(
      LocalizedFormats.CANNOT_PARSE_AS_TYPE,
      wrong, position, type.getName()
    )
  }

  /**
   * @param wrong Bad string representation of the object.
   * @param position Index, in the `wrong` string, that caused the
   * parsing to fail.
   */
  constructor(
    wrong: String,
    position: Int
  ) {
    getContext().addMessage(
      LocalizedFormats.CANNOT_PARSE,
      wrong, position
    )
  }

  companion object {
    /** Serializable version Id.  */
    private val serialVersionUID = -6024911025449780478L
  }
}
