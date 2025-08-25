package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.Localizable
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats


/**
 * Exception to be thrown when some counter maximum value is exceeded.
 */
open class MaxCountExceededException(
  specific: Localizable,
  max: Number,
  vararg args: Any
) : MathIllegalStateException() {
  /**
   * @return the maximum number of evaluations.
   */
  /**
   * Maximum number of evaluations.
   */
  val max: Number

  /**
   * Construct the exception.
   *
   * @param max Maximum.
   */
  constructor(max: Number) : this(LocalizedFormats.MAX_COUNT_EXCEEDED, max)

  /**
   * Construct the exception with a specific context.
   *
   * @param specific Specific context pattern.
   * @param max Maximum.
   * @param args Additional arguments.
   */
  init {
    getContext().addMessage(specific, max, args)
    this.max = max
  }

}
