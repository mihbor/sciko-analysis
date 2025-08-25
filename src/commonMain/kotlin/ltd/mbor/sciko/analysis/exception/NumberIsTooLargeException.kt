package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats
import ltd.mbor.sciko.analysis.exception.util.Localizable


/**
 * Exception to be thrown when a number is too large.
 */
class NumberIsTooLargeException
/**
 * Construct the exception with a specific context.
 *
 * @param specific Specific context pattern.
 * @param wrong Value that is larger than the maximum.
 * @param max Maximum.
 * @param boundIsAllowed if true the maximum is included in the allowed range.
 */(
  specific: Localizable,
  wrong: Number,
  /**
   * Higher bound.
   */
  val max: Number,
  /**
   * Whether the maximum is included in the allowed range.
   */
  val boundIsAllowed: Boolean
) : MathIllegalNumberException(specific, wrong, max) {

  /**
   * Construct the exception.
   *
   * @param wrong Value that is larger than the maximum.
   * @param max Maximum.
   * @param boundIsAllowed if true the maximum is included in the allowed range.
   */
  constructor(
    wrong: Number,
    max: Number,
    boundIsAllowed: Boolean
  ) : this(
    if (boundIsAllowed) LocalizedFormats.NUMBER_TOO_LARGE else LocalizedFormats.NUMBER_TOO_LARGE_BOUND_EXCLUDED,
    wrong, max, boundIsAllowed
  )

}
