package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.UnivariateFunction
import ltd.mbor.sciko.analysis.exception.NoBracketingException
import ltd.mbor.sciko.analysis.exception.NotStrictlyPositiveException
import ltd.mbor.sciko.analysis.exception.NumberIsTooLargeException
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Utility routines for [UnivariateSolver] objects.
 *
 */
object UnivariateSolverUtils {
  /**
   * Convenience method to find a zero of a univariate real function.  A default
   * solver is used.
   *
   * @param function Function.
   * @param x0 Lower bound for the interval.
   * @param x1 Upper bound for the interval.
   * @return a value where the function is zero.
   * @throws NoBracketingException if the function has the same sign at the
   * endpoints.
   */
  @Throws(NoBracketingException::class)
  fun solve(function: UnivariateFunction, x0: Double, x1: Double): Double {
    val solver: UnivariateSolver = BrentSolver()
    return solver.solve(Int.Companion.MAX_VALUE, function, x0, x1)
  }

  /**
   * Convenience method to find a zero of a univariate real function.  A default
   * solver is used.
   *
   * @param function Function.
   * @param x0 Lower bound for the interval.
   * @param x1 Upper bound for the interval.
   * @param absoluteAccuracy Accuracy to be used by the solver.
   * @return a value where the function is zero.
   * @throws NoBracketingException if the function has the same sign at the
   * endpoints.
   */
  @Throws(NoBracketingException::class)
  fun solve(
    function: UnivariateFunction,
    x0: Double, x1: Double,
    absoluteAccuracy: Double
  ): Double {
    val solver: UnivariateSolver = BrentSolver(absoluteAccuracy)
    return solver.solve(Int.Companion.MAX_VALUE, function, x0, x1)
  }

  /**
   * Force a root found by a non-bracketing solver to lie on a specified side,
   * as if the solver were a bracketing one.
   *
   * @param maxEval maximal number of new evaluations of the function
   * (evaluations already done for finding the root should have already been subtracted
   * from this number)
   * @param f function to solve
   * @param bracketing bracketing solver to use for shifting the root
   * @param baseRoot original root found by a previous non-bracketing solver
   * @param min minimal bound of the search interval
   * @param max maximal bound of the search interval
   * @param allowedSolution the kind of solutions that the root-finding algorithm may
   * accept as solutions.
   * @return a root approximation, on the specified side of the exact root
   * @throws NoBracketingException if the function has the same sign at the
   * endpoints.
   */
  @Throws(NoBracketingException::class)
  fun forceSide(
    maxEval: Int, f: UnivariateFunction,
    bracketing: BracketedUnivariateSolver<UnivariateFunction>,
    baseRoot: Double, min: Double, max: Double,
    allowedSolution: AllowedSolution?
  ): Double {
    if (allowedSolution == AllowedSolution.ANY_SIDE) {
      // no further bracketing required
      return baseRoot
    }
    // find a very small interval bracketing the root
    val step = max(
      bracketing.absoluteAccuracy,
      abs(baseRoot*bracketing.relativeAccuracy)
    )
    var xLo = max(min, baseRoot - step)
    var fLo: Double = f.value(xLo)
    var xHi = min(max, baseRoot + step)
    var fHi: Double = f.value(xHi)
    var remainingEval = maxEval - 2
    while (remainingEval > 0) {
      if ((fLo >= 0 && fHi <= 0) || (fLo <= 0 && fHi >= 0)) {
        // compute the root on the selected side
        return bracketing.solve(remainingEval, f, xLo, xHi, baseRoot, allowedSolution)
      }
      // try increasing the interval
      var changeLo = false
      var changeHi = false
      if (fLo < fHi) {
        // increasing function
        if (fLo >= 0) {
          changeLo = true
        } else {
          changeHi = true
        }
      } else if (fLo > fHi) {
        // decreasing function
        if (fLo <= 0) {
          changeLo = true
        } else {
          changeHi = true
        }
      } else {
        // unknown variation
        changeLo = true
        changeHi = true
      }
      // update the lower bound
      if (changeLo) {
        xLo = max(min, xLo - step)
        fLo = f.value(xLo)
        remainingEval--
      }
      // update the higher bound
      if (changeHi) {
        xHi = min(max, xHi + step)
        fHi = f.value(xHi)
        remainingEval--
      }
    }
    throw NoBracketingException(
      LocalizedFormats.FAILED_BRACKETING,
      xLo, xHi, fLo, fHi,
      maxEval - remainingEval, maxEval, baseRoot,
      min, max
    )
  }

  /**
   * This method simply calls [bracket(function, initial, lowerBound, upperBound, q, r, maximumIterations)][.bracket]
   * with `q` and `r` set to 1.0 and `maximumIterations` set to `Integer.MAX_VALUE`.
   *
   *
   * **Note: ** this method can take `Integer.MAX_VALUE`
   * iterations to throw a `ConvergenceException.`  Unless you are
   * confident that there is a root between `lowerBound` and
   * `upperBound` near `initial`, it is better to use
   * [ bracket(function, initial, lowerBound, upperBound, q, r, maximumIterations)][.bracket],
   * explicitly specifying the maximum number of iterations.
   *
   * @param function Function.
   * @param initial Initial midpoint of interval being expanded to
   * bracket a root.
   * @param lowerBound Lower bound (a is never lower than this value)
   * @param upperBound Upper bound (b never is greater than this
   * value).
   * @return a two-element array holding a and b.
   * @throws NoBracketingException if a root cannot be bracketted.
   * @throws NotStrictlyPositiveException if `maximumIterations <= 0`.
   */
  @Throws(NotStrictlyPositiveException::class, NoBracketingException::class)
  fun bracket(
    function: UnivariateFunction,
    initial: Double,
    lowerBound: Double, upperBound: Double
  ): DoubleArray {
    return bracket(function, initial, lowerBound, upperBound, 1.0, 1.0, Int.Companion.MAX_VALUE)
  }

  /**
   * This method simply calls [bracket(function, initial, lowerBound, upperBound, q, r, maximumIterations)][.bracket]
   * with `q` and `r` set to 1.0.
   * @param function Function.
   * @param initial Initial midpoint of interval being expanded to
   * bracket a root.
   * @param lowerBound Lower bound (a is never lower than this value).
   * @param upperBound Upper bound (b never is greater than this
   * value).
   * @param maximumIterations Maximum number of iterations to perform
   * @return a two element array holding a and b.
   * @throws NoBracketingException if the algorithm fails to find a and b
   * satisfying the desired conditions.
   * @throws NotStrictlyPositiveException if `maximumIterations <= 0`.
   */
  @Throws(NotStrictlyPositiveException::class, NoBracketingException::class)
  fun bracket(
    function: UnivariateFunction,
    initial: Double,
    lowerBound: Double, upperBound: Double,
    maximumIterations: Int
  ): DoubleArray {
    return bracket(function, initial, lowerBound, upperBound, 1.0, 1.0, maximumIterations)
  }

  /**
   * This method attempts to find two values a and b satisfying
   *  *  `lowerBound <= a < initial < b <= upperBound`
   *  *  `f(a) * f(b) <= 0`
   *
   * If `f` is continuous on `[a,b]`, this means that `a`
   * and `b` bracket a root of `f`.
   *
   *
   * The algorithm checks the sign of \( f(l_k) \) and \( f(u_k) \) for increasing
   * values of k, where \( l_k = max(lower, initial - \delta_k) \),
   * \( u_k = min(upper, initial + \delta_k) \), using recurrence
   * \( \delta_{k+1} = r \delta_k + q, \delta_0 = 0\) and starting search with \( k=1 \).
   * The algorithm stops when one of the following happens:
   *  *  at least one positive and one negative value have been found --  success!
   *  *  both endpoints have reached their respective limits -- NoBracketingException
   *  *  `maximumIterations` iterations elapse -- NoBracketingException
   *
   *
   * If different signs are found at first iteration (`k=1`), then the returned
   * interval will be \( [a, b] = [l_1, u_1] \). If different signs are found at a later
   * iteration `k>1`, then the returned interval will be either
   * \( [a, b] = [l_{k+1}, l_{k}] \) or \( [a, b] = [u_{k}, u_{k+1}] \). A root solver called
   * with these parameters will therefore start with the smallest bracketing interval known
   * at this step.
   *
   *
   *
   * Interval expansion rate is tuned by changing the recurrence parameters `r` and
   * `q`. When the multiplicative factor `r` is set to 1, the sequence is a
   * simple arithmetic sequence with linear increase. When the multiplicative factor `r`
   * is larger than 1, the sequence has an asymptotically exponential rate. Note than the
   * additive parameter `q` should never be set to zero, otherwise the interval would
   * degenerate to the single initial point for all values of `k`.
   *
   *
   *
   * As a rule of thumb, when the location of the root is expected to be approximately known
   * within some error margin, `r` should be set to 1 and `q` should be set to the
   * order of magnitude of the error margin. When the location of the root is really a wild guess,
   * then `r` should be set to a value larger than 1 (typically 2 to double the interval
   * length at each iteration) and `q` should be set according to half the initial
   * search interval length.
   *
   *
   *
   * As an example, if we consider the trivial function `f(x) = 1 - x` and use
   * `initial = 4`, `r = 1`, `q = 2`, the algorithm will compute
   * `f(4-2) = f(2) = -1` and `f(4+2) = f(6) = -5` for `k = 1`, then
   * `f(4-4) = f(0) = +1` and `f(4+4) = f(8) = -7` for `k = 2`. Then it will
   * return the interval `[0, 2]` as the smallest one known to be bracketing the root.
   * As shown by this example, the initial value (here `4`) may lie outside of the returned
   * bracketing interval.
   *
   * @param function function to check
   * @param initial Initial midpoint of interval being expanded to
   * bracket a root.
   * @param lowerBound Lower bound (a is never lower than this value).
   * @param upperBound Upper bound (b never is greater than this
   * value).
   * @param q additive offset used to compute bounds sequence (must be strictly positive)
   * @param r multiplicative factor used to compute bounds sequence
   * @param maximumIterations Maximum number of iterations to perform
   * @return a two element array holding the bracketing values.
   * @exception NoBracketingException if function cannot be bracketed in the search interval
   */
  @Throws(NoBracketingException::class)
  fun bracket(
    function: UnivariateFunction, initial: Double,
    lowerBound: Double, upperBound: Double,
    q: Double, r: Double, maximumIterations: Int
  ): DoubleArray {
    if (q <= 0) {
      throw NotStrictlyPositiveException(q)
    }
    if (maximumIterations <= 0) {
      throw NotStrictlyPositiveException(LocalizedFormats.INVALID_MAX_ITERATIONS, maximumIterations)
    }
    verifySequence(lowerBound, initial, upperBound)
    // initialize the recurrence
    var a = initial
    var b = initial
    var fa = Double.Companion.NaN
    var fb = Double.Companion.NaN
    var delta = 0.0
    var numIterations = 0
    while ((numIterations < maximumIterations) && (a > lowerBound || b < upperBound)
    ) {
      val previousA = a
      val previousFa = fa
      val previousB = b
      val previousFb = fb
      delta = r*delta + q
      a = max(initial - delta, lowerBound)
      b = min(initial + delta, upperBound)
      fa = function.value(a)
      fb = function.value(b)
      if (numIterations == 0) {
        // at first iteration, we don't have a previous interval
        // we simply compare both sides of the initial interval
        if (fa*fb <= 0) {
          // the first interval already brackets a root
          return doubleArrayOf(a, b)
        }
      } else {
        // we have a previous interval with constant sign and expand it,
        // we expect sign changes to occur at boundaries
        if (fa*previousFa <= 0) {
          // sign change detected at near lower bound
          return doubleArrayOf(a, previousA)
        } else if (fb*previousFb <= 0) {
          // sign change detected at near upper bound
          return doubleArrayOf(previousB, b)
        }
      }
      ++numIterations
    }
    // no bracketing found
    throw NoBracketingException(a, b, fa, fb)
  }

  /**
   * Compute the midpoint of two values.
   *
   * @param a first value.
   * @param b second value.
   * @return the midpoint.
   */
  fun midpoint(a: Double, b: Double): Double {
    return (a + b)*0.5
  }

  /**
   * Check whether the interval bounds bracket a root. That is, if the
   * values at the endpoints are not equal to zero, then the function takes
   * opposite signs at the endpoints.
   *
   * @param function Function.
   * @param lower Lower endpoint.
   * @param upper Upper endpoint.
   * @return `true` if the function values have opposite signs at the
   * given points.
   */
  fun isBracketing(
    function: UnivariateFunction,
    lower: Double,
    upper: Double
  ): Boolean {
    val fLo: Double = function.value(lower)
    val fHi: Double = function.value(upper)
    return (fLo >= 0 && fHi <= 0) || (fLo <= 0 && fHi >= 0)
  }

  /**
   * Check whether the arguments form a (strictly) increasing sequence.
   *
   * @param start First number.
   * @param mid Second number.
   * @param end Third number.
   * @return `true` if the arguments form an increasing sequence.
   */
  fun isSequence(
    start: Double,
    mid: Double,
    end: Double
  ): Boolean {
    return (start < mid) && (mid < end)
  }

  /**
   * Check that the endpoints specify an interval.
   *
   * @param lower Lower endpoint.
   * @param upper Upper endpoint.
   * @throws NumberIsTooLargeException if `lower >= upper`.
   */
  @Throws(NumberIsTooLargeException::class)
  fun verifyInterval(
    lower: Double,
    upper: Double
  ) {
    if (lower >= upper) {
      throw NumberIsTooLargeException(
        LocalizedFormats.ENDPOINTS_NOT_AN_INTERVAL,
        lower, upper, false
      )
    }
  }

  /**
   * Check that `lower < initial < upper`.
   *
   * @param lower Lower endpoint.
   * @param initial Initial value.
   * @param upper Upper endpoint.
   * @throws NumberIsTooLargeException if `lower >= initial` or
   * `initial >= upper`.
   */
  @Throws(NumberIsTooLargeException::class)
  fun verifySequence(
    lower: Double,
    initial: Double,
    upper: Double
  ) {
    verifyInterval(lower, initial)
    verifyInterval(initial, upper)
  }

  /**
   * Check that the endpoints specify an interval and the end points
   * bracket a root.
   *
   * @param function Function.
   * @param lower Lower endpoint.
   * @param upper Upper endpoint.
   * @throws NoBracketingException if the function has the same sign at the
   * endpoints.
   */
  @Throws(NoBracketingException::class)
  fun verifyBracketing(
    function: UnivariateFunction,
    lower: Double,
    upper: Double
  ) {
    verifyInterval(lower, upper)
    if (!isBracketing(function, lower, upper)) {
      throw NoBracketingException(
        lower, upper,
        function.value(lower),
        function.value(upper)
      )
    }
  }
}
