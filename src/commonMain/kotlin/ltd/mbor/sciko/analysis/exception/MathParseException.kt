package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.ExceptionContextProvider
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats

/**
 * Class to signal parse failures.
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
    type: String
  ) {
    getContext().addMessage(
      LocalizedFormats.CANNOT_PARSE_AS_TYPE,
      wrong, position, type
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

}
