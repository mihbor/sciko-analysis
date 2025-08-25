package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.PolynomialFunction
import ltd.mbor.sciko.analysis.exception.NoBracketingException
import ltd.mbor.sciko.analysis.exception.NoDataException
import ltd.mbor.sciko.analysis.exception.NumberIsTooLargeException
import ltd.mbor.sciko.analysis.exception.TooManyEvaluationsException
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats
import ltd.mbor.sciko.analysis.exception.util.copyInto
import ltd.mbor.sciko.analysis.exception.util.sqrt
import ltd.mbor.sciko.analysis.util.ComplexUtils
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDouble
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import kotlin.math.abs
import kotlin.math.max

/**
 * Implements the [
 * Laguerre's Method](http://mathworld.wolfram.com/LaguerresMethod.html) for root finding of real coefficient polynomials.
 * For reference, see
 * <blockquote>
 * **A First Course in Numerical Analysis**,
 * ISBN 048641454X, chapter 8.
</blockquote> *
 * Laguerre's method is global in the sense that it can start with any initial
 * approximation and be able to solve all roots from that point.
 * The algorithm requires a bracketing condition.
 */
class LaguerreSolver : AbstractPolynomialSolver {
  /** Complex solver.  */
  private val complexSolver = ComplexSolver()
  /**
   * Construct a solver with default accuracy (1e-6).
   */
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
   */
  constructor(
    relativeAccuracy: Double,
    absoluteAccuracy: Double,
    functionValueAccuracy: Double
  ) : super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy)

  /**
   * {@inheritDoc}
   */
  @Throws(TooManyEvaluationsException::class, NumberIsTooLargeException::class, NoBracketingException::class)
  public override fun doSolve(): Double {
    val initial = startValue
    val functionValueAccuracy = functionValueAccuracy
    verifySequence(min, initial, max)
    // Return the initial guess if it is good enough.
    val yInitial = computeObjectiveValue(initial)
    if (abs(yInitial) <= functionValueAccuracy) {
      return initial
    }
    // Return the first endpoint if it is good enough.
    val yMin = computeObjectiveValue(min)
    if (abs(yMin) <= functionValueAccuracy) {
      return min
    }
    // Reduce interval if min and initial bracket the root.
    if (yInitial*yMin < 0) {
      return laguerre(min, initial, yMin, yInitial)
    }
    // Return the second endpoint if it is good enough.
    val yMax = computeObjectiveValue(max)
    if (abs(yMax) <= functionValueAccuracy) {
      return max
    }
    // Reduce interval if initial and max bracket the root.
    if (yInitial*yMax < 0) {
      return laguerre(initial, max, yInitial, yMax)
    }
    throw NoBracketingException(min, max, yMin, yMax)
  }

  /**
   * Find a real root in the given interval.
   *
   * Despite the bracketing condition, the root returned by
   * [LaguerreSolver.ComplexSolver.solve] may
   * not be a real zero inside `[min, max]`.
   * For example, ` p(x) = x<sup>3</sup> + 1, `
   * with `min = -2`, `max = 2`, `initial = 0`.
   * When it occurs, this code calls
   * [LaguerreSolver.ComplexSolver.solveAll]
   * in order to obtain all roots and picks up one real root.
   *
   * @param lo Lower bound of the search interval.
   * @param hi Higher bound of the search interval.
   * @param fLo Function value at the lower bound of the search interval.
   * @param fHi Function value at the higher bound of the search interval.
   * @return the point at which the function value is zero.
   */
  @Deprecated(
    """This method should not be part of the public API: It will
      be made private in version 4.0."""
  )
  fun laguerre(
    lo: Double, hi: Double,
    fLo: Double, fHi: Double
  ): Double {
    val c: MultiArray<ComplexDouble, D1> = ComplexUtils.convertToComplex(coefficients)
    val initial: ComplexDouble = ComplexDouble(0.5*(lo + hi), 0)
    val z: ComplexDouble = complexSolver.solve(c, initial)
    if (complexSolver.isRoot(lo, hi, z)) {
      return z.re
    } else {
      var r = Double.Companion.NaN
      // Solve all roots and select the one we are seeking.
      val root: MultiArray<ComplexDouble, D1> = complexSolver.solveAll(c, initial)
      for (i in root.indices) {
        if (complexSolver.isRoot(lo, hi, root[i])) {
          r = root[i].re
          break
        }
      }
      return r
    }
  }

  /**
   * Find all complex roots for the polynomial with the given
   * coefficients, starting from the given initial value.
   *
   *
   * Note: This method is not part of the API of [BaseUnivariateSolver].
   *
   * @param coefficients Polynomial coefficients.
   * @param initial Start value.
   * @return the full set of complex roots of the polynomial
   * @throws TooManyEvaluationsException
   * if the maximum number of evaluations is exceeded when solving for one of the roots
   * @throws NoDataException if the `coefficients` array is empty.
   * @since 3.1
   */
  @Throws(NoDataException::class, TooManyEvaluationsException::class)
  fun solveAllComplex(
    coefficients: MultiArray<Double, D1>,
    initial: Double
  ): MultiArray<ComplexDouble, D1> {
    return solveAllComplex(coefficients, initial, Int.Companion.MAX_VALUE)
  }

  /**
   * Find all complex roots for the polynomial with the given
   * coefficients, starting from the given initial value.
   *
   *
   * Note: This method is not part of the API of [BaseUnivariateSolver].
   *
   * @param coefficients polynomial coefficients
   * @param initial start value
   * @param maxEval maximum number of evaluations
   * @return the full set of complex roots of the polynomial
   * @throws TooManyEvaluationsException
   * if the maximum number of evaluations is exceeded when solving for one of the roots
   * @throws NoDataException if the `coefficients` array is empty
   * @since 3.5
   */
  @Throws(NoDataException::class, TooManyEvaluationsException::class)
  fun solveAllComplex(
    coefficients: MultiArray<Double, D1>,
    initial: Double, maxEval: Int
  ): MultiArray<ComplexDouble, D1> {
    setup(
      maxEval,
      PolynomialFunction(coefficients),
      Double.Companion.NEGATIVE_INFINITY,
      Double.Companion.POSITIVE_INFINITY,
      initial
    )
    return complexSolver.solveAll(
      ComplexUtils.convertToComplex(coefficients),
      ComplexDouble(initial, 0.0)
    )
  }

  /**
   * Find a complex root for the polynomial with the given coefficients,
   * starting from the given initial value.
   *
   *
   * Note: This method is not part of the API of [BaseUnivariateSolver].
   *
   * @param coefficients Polynomial coefficients.
   * @param initial Start value.
   * @return a complex root of the polynomial
   * @throws TooManyEvaluationsException
   * if the maximum number of evaluations is exceeded.
   * @throws NoDataException if the `coefficients` array is empty.
   * @since 3.1
   */
  @Throws(NoDataException::class, TooManyEvaluationsException::class)
  fun solveComplex(
    coefficients: MultiArray<Double, D1>,
    initial: Double
  ): ComplexDouble {
    return solveComplex(coefficients, initial, Int.Companion.MAX_VALUE)
  }

  /**
   * Find a complex root for the polynomial with the given coefficients,
   * starting from the given initial value.
   *
   *
   * Note: This method is not part of the API of [BaseUnivariateSolver].
   *
   * @param coefficients polynomial coefficients
   * @param initial start value
   * @param maxEval maximum number of evaluations
   * @return a complex root of the polynomial
   * @throws TooManyEvaluationsException
   * if the maximum number of evaluations is exceeded
   * @throws NoDataException if the `coefficients` array is empty
   * @since 3.1
   */
  @Throws(NoDataException::class, TooManyEvaluationsException::class)
  fun solveComplex(
    coefficients: MultiArray<Double, D1>,
    initial: Double, maxEval: Int
  ): ComplexDouble {
    setup(
      maxEval,
      PolynomialFunction(coefficients),
      Double.Companion.NEGATIVE_INFINITY,
      Double.Companion.POSITIVE_INFINITY,
      initial
    )
    return complexSolver.solve(
      ComplexUtils.convertToComplex(coefficients),
      ComplexDouble(initial, 0.0)
    )
  }

  /**
   * Class for searching all (complex) roots.
   */
  private inner class ComplexSolver {
    /**
     * Check whether the given complex root is actually a real zero
     * in the given interval, within the solver tolerance level.
     *
     * @param min Lower bound for the interval.
     * @param max Upper bound for the interval.
     * @param z Complex root.
     * @return `true` if z is a real zero.
     */
    fun isRoot(min: Double, max: Double, z: ComplexDouble): Boolean {
      if (isSequence(min, z.re, max)) {
        val tolerance = max(relativeAccuracy*z.abs(), absoluteAccuracy)
        return (abs(z.im) <= tolerance) ||
          (z.abs() <= functionValueAccuracy)
      }
      return false
    }

    /**
     * Find all complex roots for the polynomial with the given
     * coefficients, starting from the given initial value.
     *
     * @param coefficients Polynomial coefficients.
     * @param initial Start value.
     * @return the point at which the function value is zero.
     * @throws TooManyEvaluationsException
     * if the maximum number of evaluations is exceeded.
     * @throws NoDataException if the `coefficients` array is empty.
     */
    @Throws(NoDataException::class, TooManyEvaluationsException::class)
    fun solveAll(coefficients: MultiArray<ComplexDouble, D1>, initial: ComplexDouble): MultiArray<ComplexDouble, D1> {
      val n = coefficients.size - 1
      if (n == 0) {
        throw NoDataException(LocalizedFormats.POLYNOMIAL)
      }
      // Coefficients for deflated polynomial.
      val c = mk.zeros<ComplexDouble>(n + 1)
      for (i in 0..n) {
        c[i] = coefficients[i]
      }
      // Solve individual roots successively.
      val root = mutableListOf<ComplexDouble>()
      for (i in 0..<n) {
        val subarray = mk.zeros<ComplexDouble>(n - i + 1)
        c.copyInto(subarray, 0, 0, subarray.size)
        root += solve(subarray, initial)
        // Polynomial deflation using synthetic division.
        var newc: ComplexDouble = c[n - i]
        var oldc: ComplexDouble? = null
        for (j in n - i - 1 downTo 0) {
          oldc = c[j]
          c[j] = newc
          newc = oldc + (newc * root[i])
        }
      }
      return mk.ndarray(root)
    }

    /**
     * Find a complex root for the polynomial with the given coefficients,
     * starting from the given initial value.
     *
     * @param coefficients Polynomial coefficients.
     * @param initial Start value.
     * @return the point at which the function value is zero.
     * @throws TooManyEvaluationsException
     * if the maximum number of evaluations is exceeded.
     * @throws NoDataException if the `coefficients` array is empty.
     */
    @Throws(NoDataException::class, TooManyEvaluationsException::class)
    fun solve(coefficients: MultiArray<ComplexDouble, D1>, initial: ComplexDouble): ComplexDouble {
      val n = coefficients.size - 1
      if (n == 0) {
        throw NoDataException(LocalizedFormats.POLYNOMIAL)
      }
      val nC = ComplexDouble(n, 0)
      val n1C = ComplexDouble(n - 1, 0)
      var z = initial
      var oldz: ComplexDouble = ComplexDouble(
        Double.Companion.POSITIVE_INFINITY,
        Double.Companion.POSITIVE_INFINITY
      )
      while (true) {
        // Compute pv (polynomial value), dv (derivative value), and
        // d2v (second derivative value) simultaneously.
        var pv: ComplexDouble = coefficients[n]
        var dv: ComplexDouble = ComplexDouble.zero
        var d2v: ComplexDouble = ComplexDouble.zero
        for (j in n - 1 downTo 0) {
          d2v = dv + (z*(d2v))
          dv = pv + (z*(dv))
          pv = coefficients[j]+(z*(pv))
        }
        d2v = d2v*(ComplexDouble(2.0, 0.0))
        // Check for convergence.
        val tolerance = max(
          relativeAccuracy*z.abs(),
          absoluteAccuracy
        )
        if ((z-(oldz)).abs() <= tolerance) {
          return z
        }
        if (pv.abs() <= functionValueAccuracy) {
          return z
        }
        // Now pv != 0, calculate the new approximation.
        val G: ComplexDouble = dv/(pv)
        val G2: ComplexDouble = G*(G)
        val H: ComplexDouble = G2-(d2v/(pv))
        val delta: ComplexDouble = n1C*((nC*(H))-(G2))
        // Choose a denominator larger in magnitude.
        val deltaSqrt: ComplexDouble = sqrt(delta)
        val dplus: ComplexDouble = G+(deltaSqrt)
        val dminus: ComplexDouble = G-(deltaSqrt)
        val denominator: ComplexDouble = if (dplus.abs() > dminus.abs()) dplus else dminus
        // Perturb z if denominator is zero, for instance,
        // p(x) = x^3 + 1, z = 0.
        if (denominator.equals(ComplexDouble(0.0, 0.0))) {
          z = z+(ComplexDouble(absoluteAccuracy, absoluteAccuracy))
          oldz = ComplexDouble(
            Double.Companion.POSITIVE_INFINITY,
            Double.Companion.POSITIVE_INFINITY
          )
        } else {
          oldz = z
          z = z-(nC/(denominator))
        }
        incrementEvaluationCount()
      }
    }
  }

  companion object {
    /** Default absolute accuracy.  */
    private const val DEFAULT_ABSOLUTE_ACCURACY = 1e-6
  }
}