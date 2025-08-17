package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.exception.NoBracketingException
import ltd.mbor.sciko.analysis.exception.NumberIsTooLargeException
import ltd.mbor.sciko.analysis.exception.TooManyEvaluationsException
import ltd.mbor.sciko.linalg.FastMath
import ltd.mbor.sciko.linalg.Precision


/**
 * This class implements the [
 * Brent algorithm](http://mathworld.wolfram.com/BrentsMethod.html) for finding zeros of real univariate functions.
 * The function should be continuous but not necessarily smooth.
 * The `solve` method returns a zero `x` of the function `f`
 * in the given interval `[a, b]` to within a tolerance
 * `2 eps abs(x) + t` where `eps` is the relative accuracy and
 * `t` is the absolute accuracy.
 *
 * The given interval must bracket the root.
 *
 *
 * The reference implementation is given in chapter 4 of
 * <blockquote>
 * **Algorithms for Minimization Without Derivatives**,
 * *Richard P. Brent*,
 * Dover, 2002
</blockquote> *
 *
 * @see BaseAbstractUnivariateSolver
 */
class BrentSolver : AbstractUnivariateSolver {
  /**
   * Construct a solver.
   *
   * @param absoluteAccuracy Absolute accuracy.
   */
  /**
   * Construct a solver with default absolute accuracy (1e-6).
   */
  @JvmOverloads
  constructor(absoluteAccuracy: Double = DEFAULT_ABSOLUTE_ACCURACY) : super(absoluteAccuracy)

  /**
   * Construct a solver.
   *
   * @param relativeAccuracy Relative accuracy.
   * @param absoluteAccuracy Absolute accuracy.
   */
  constructor(
    relativeAccuracy: Double,
    absoluteAccuracy: Double
  ) : super(relativeAccuracy, absoluteAccuracy)

  /**
   * Construct a solver.
   *
   * @param relativeAccuracy Relative accuracy.
   * @param absoluteAccuracy Absolute accuracy.
   * @param functionValueAccuracy Function value accuracy.
   *
   * @see BaseAbstractUnivariateSolver.BaseAbstractUnivariateSolver
   */
  constructor(
    relativeAccuracy: Double,
    absoluteAccuracy: Double,
    functionValueAccuracy: Double
  ) : super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy)

  /**
   * {@inheritDoc}
   */
  @Throws(NoBracketingException::class, TooManyEvaluationsException::class, NumberIsTooLargeException::class)
  override fun doSolve(): Double {
    val initial = startValue
    verifySequence(min, initial, max)
    // Return the initial guess if it is good enough.
    val yInitial = computeObjectiveValue(initial)
    if (FastMath.abs(yInitial) <= functionValueAccuracy) {
      return initial
    }
    // Return the first endpoint if it is good enough.
    val yMin = computeObjectiveValue(min)
    if (FastMath.abs(yMin) <= functionValueAccuracy) {
      return min
    }
    // Reduce interval if min and initial bracket the root.
    if (yInitial*yMin < 0) {
      return brent(min, initial, yMin, yInitial)
    }
    // Return the second endpoint if it is good enough.
    val yMax = computeObjectiveValue(max)
    if (FastMath.abs(yMax) <= functionValueAccuracy) {
      return max
    }
    // Reduce interval if initial and max bracket the root.
    if (yInitial*yMax < 0) {
      return brent(initial, max, yInitial, yMax)
    }
    throw NoBracketingException(min, max, yMin, yMax)
  }

  /**
   * Search for a zero inside the provided interval.
   * This implementation is based on the algorithm described at page 58 of
   * the book
   * <blockquote>
   * **Algorithms for Minimization Without Derivatives**,
   * <it>Richard P. Brent</it>,
   * Dover 0-486-41998-3
  </blockquote> *
   *
   * @param lo Lower bound of the search interval.
   * @param hi Higher bound of the search interval.
   * @param fLo Function value at the lower bound of the search interval.
   * @param fHi Function value at the higher bound of the search interval.
   * @return the value where the function is zero.
   */
  private fun brent(
    lo: Double, hi: Double,
    fLo: Double, fHi: Double
  ): Double {
    var a = lo
    var fa = fLo
    var b = hi
    var fb = fHi
    var c = a
    var fc = fa
    var d = b - a
    var e = d
    val t = absoluteAccuracy
    val eps = relativeAccuracy
    while (true) {
      if (FastMath.abs(fc) < FastMath.abs(fb)) {
        a = b
        b = c
        c = a
        fa = fb
        fb = fc
        fc = fa
      }
      val tol = 2*eps*FastMath.abs(b) + t
      val m = 0.5*(c - b)
      if (FastMath.abs(m) <= tol ||
        Precision.equals(fb, 0.0)
      ) {
        return b
      }
      if (FastMath.abs(e) < tol ||
        FastMath.abs(fa) <= FastMath.abs(fb)
      ) {
        // Force bisection.
        d = m
        e = d
      } else {
        var s = fb/fa
        var p: Double
        var q: Double
        // The equality test (a == c) is intentional,
        // it is part of the original Brent's method and
        // it should NOT be replaced by proximity test.
        if (a == c) {
          // Linear interpolation.
          p = 2*m*s
          q = 1 - s
        } else {
          // Inverse quadratic interpolation.
          q = fa/fc
          val r = fb/fc
          p = s*(2*m*q*(q - r) - (b - a)*(r - 1))
          q = (q - 1)*(r - 1)*(s - 1)
        }
        if (p > 0) {
          q = -q
        } else {
          p = -p
        }
        s = e
        e = d
        if (p >= 1.5*m*q - FastMath.abs(tol*q) ||
          p >= FastMath.abs(0.5*s*q)
        ) {
          // Inverse quadratic interpolation gives a value
          // in the wrong direction, or progress is slow.
          // Fall back to bisection.
          d = m
          e = d
        } else {
          d = p/q
        }
      }
      a = b
      fa = fb
      if (FastMath.abs(d) > tol) {
        b += d
      } else if (m > 0) {
        b += tol
      } else {
        b -= tol
      }
      fb = computeObjectiveValue(b)
      if ((fb > 0 && fc > 0) ||
        (fb <= 0 && fc <= 0)
      ) {
        c = a
        fc = fa
        d = b - a
        e = d
      }
    }
  }

  companion object {
    /** Default absolute accuracy.  */
    private const val DEFAULT_ABSOLUTE_ACCURACY = 1e-6
  }
}