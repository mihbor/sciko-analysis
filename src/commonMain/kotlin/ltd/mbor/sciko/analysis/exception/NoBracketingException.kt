package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats
import ltd.mbor.sciko.analysis.exception.util.Localizable


/**
 * Exception to be thrown when function values have the same sign at both
 * ends of an interval.
 */
class NoBracketingException
/**
 * Construct the exception with a specific context.
 *
 * @param specific Contextual information on what caused the exception.
 * @param lo Lower end of the interval.
 * @param hi Higher end of the interval.
 * @param fLo Value at lower end of the interval.
 * @param fHi Value at higher end of the interval.
 * @param args Additional arguments.
 */(
  specific: Localizable,
  /** Lower end of the interval.  */
  val lo: Double,
  /** Higher end of the interval.  */
  val hi: Double,
  /** Value at lower end of the interval.  */
  val fLo: Double,
  /** Value at higher end of the interval.  */
  val fHi: Double,
  vararg args: Any?
) : MathIllegalArgumentException(specific, lo, hi, fLo, fHi, args) {

  /**
   * Construct the exception.
   *
   * @param lo Lower end of the interval.
   * @param hi Higher end of the interval.
   * @param fLo Value at lower end of the interval.
   * @param fHi Value at higher end of the interval.
   */
  constructor(
    lo: Double, hi: Double,
    fLo: Double, fHi: Double
  ) : this(LocalizedFormats.SAME_SIGN_AT_ENDPOINTS, lo, hi, fLo, fHi)

}
