package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.Localizable

/**
 * Base class for exceptions raised by a wrong number.
 * This class is not intended to be instantiated directly: it should serve
 * as a base class to create all the exceptions that are raised because some
 * precondition is violated by a number argument.
 */
open class MathIllegalNumberException
/**
 * Construct an exception.
 *
 * @param pattern Localizable pattern.
 * @param argument Wrong number.
 * @param arguments Arguments.
 */ protected constructor(
  pattern: Localizable,
  /** Requested.  */
  val argument: Number,
  vararg arguments: Any
) : MathIllegalArgumentException(pattern, argument, arguments) {
  /**
   * @return the requested value.
   */

  companion object {
    /** Helper to avoid boxing warnings. @since 3.3  */
    const val INTEGER_ZERO: Int = 0
  }
}
