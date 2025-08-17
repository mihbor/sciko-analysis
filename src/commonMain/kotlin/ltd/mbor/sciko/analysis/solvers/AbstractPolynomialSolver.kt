package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.PolynomialFunction
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray


/**
 * Base class for solvers.
 *
 * @since 3.0
 */
abstract class AbstractPolynomialSolver : BaseAbstractUnivariateSolver<PolynomialFunction>, PolynomialSolver {
  /** Function.  */
  private var polynomialFunction: PolynomialFunction? = null

  /**
   * Construct a solver with given absolute accuracy.
   *
   * @param absoluteAccuracy Maximum absolute error.
   */
  protected constructor(absoluteAccuracy: Double) : super(absoluteAccuracy)

  /**
   * Construct a solver with given accuracies.
   *
   * @param relativeAccuracy Maximum relative error.
   * @param absoluteAccuracy Maximum absolute error.
   */
  protected constructor(
    relativeAccuracy: Double,
    absoluteAccuracy: Double
  ) : super(relativeAccuracy, absoluteAccuracy)

  /**
   * Construct a solver with given accuracies.
   *
   * @param relativeAccuracy Maximum relative error.
   * @param absoluteAccuracy Maximum absolute error.
   * @param functionValueAccuracy Maximum function value error.
   */
  protected constructor(
    relativeAccuracy: Double,
    absoluteAccuracy: Double,
    functionValueAccuracy: Double
  ) : super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy)

  /**
   * {@inheritDoc}
   */
  override fun setup(
    maxEval: Int,
    f: PolynomialFunction,
    min: Double,
    max: Double,
    startValue: Double
  ) {
    super.setup(maxEval, f, min, max, startValue)
    polynomialFunction = f
  }

  protected val coefficients: MultiArray<Double, D1>
    /**
     * @return the coefficients of the polynomial function.
     */
    get() = polynomialFunction!!.getCoefficients()
}