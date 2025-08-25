package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.Localizable
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats

/**
 * Exception to be thrown when a number is too small.
 */
open class NumberIsTooSmallException
/**
 * Construct the exception with a specific context.
 *
 * @param specific Specific context pattern.
 * @param wrong Value that is smaller than the minimum.
 * @param min Minimum.
 * @param boundIsAllowed Whether `min` is included in the allowed range.
 */(
  specific: Localizable,
  wrong: Number,
  /**
   * Higher bound.
   */
  val min: Number,
  /**
   * Whether the maximum is included in the allowed range.
   */
  val boundIsAllowed: Boolean
) : MathIllegalNumberException(specific, wrong, min) {
  /**
   * Construct the exception.
   *
   * @param wrong Value that is smaller than the minimum.
   * @param min Minimum.
   * @param boundIsAllowed Whether `min` is included in the allowed range.
   */
  constructor(
    wrong: Number,
    min: Number,
    boundIsAllowed: Boolean
  ) : this(
    if (boundIsAllowed) LocalizedFormats.NUMBER_TOO_SMALL else LocalizedFormats.NUMBER_TOO_SMALL_BOUND_EXCLUDED,
    wrong, min, boundIsAllowed
  )

}
