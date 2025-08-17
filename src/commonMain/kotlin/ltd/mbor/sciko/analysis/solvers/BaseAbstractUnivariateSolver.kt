package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.UnivariateFunction
import ltd.mbor.sciko.analysis.exception.MaxCountExceededException
import ltd.mbor.sciko.analysis.exception.NoBracketingException
import ltd.mbor.sciko.analysis.exception.NumberIsTooLargeException
import ltd.mbor.sciko.analysis.exception.TooManyEvaluationsException
import ltd.mbor.sciko.analysis.util.IntegerSequence

/**
 * Provide a default implementation for several functions useful to generic
 * solvers.
 * The default values for relative and function tolerances are 1e-14
 * and 1e-15, respectively. It is however highly recommended to not
 * rely on the default, but rather carefully consider values that match
 * user's expectations, as well as the specifics of each implementation.
 *
 * @param <FUNC> Type of function to solve.
 *
 * @since 2.0
</FUNC> */
abstract class BaseAbstractUnivariateSolver<FUNC : UnivariateFunction>
protected constructor(
  /** Relative accuracy.  */
  override val relativeAccuracy: Double,
  /** Absolute accuracy.  */
  override val absoluteAccuracy: Double,
  /** Function value accuracy.  */
  override val functionValueAccuracy: Double = DEFAULT_FUNCTION_VALUE_ACCURACY
) : BaseUnivariateSolver<FUNC> {
  /** Evaluations counter.  */
  var evaluations: IntegerSequence.Incrementor
  /**
   * @return the lower end of the search interval.
   */
  /** Lower end of search interval.  */
  var min: Double = 0.0
    private set
  /**
   * @return the higher end of the search interval.
   */
  /** Higher end of search interval.  */
  var max: Double = 0.0
    private set
  /**
   * @return the initial guess.
   */
  /** Initial guess.  */
  var startValue: Double = 0.0
    private set

  /** Function to solve.  */
  private lateinit var function: FUNC

  /**
   * Construct a solver with given absolute accuracy.
   *
   * @param absoluteAccuracy Maximum absolute error.
   */
  protected constructor(absoluteAccuracy: Double) : this(
    DEFAULT_RELATIVE_ACCURACY,
    absoluteAccuracy,
    DEFAULT_FUNCTION_VALUE_ACCURACY
  )
  /**
   * Construct a solver with given accuracies.
   *
   * @param relativeAccuracy Maximum relative error.
   * @param absoluteAccuracy Maximum absolute error.
   * @param functionValueAccuracy Maximum function value error.
   */
  /**
   * Construct a solver with given accuracies.
   *
   * @param relativeAccuracy Maximum relative error.
   * @param absoluteAccuracy Maximum absolute error.
   */
  init {
    this.evaluations = IntegerSequence.Incrementor.create()
  }

  /** {@inheritDoc}  */
  override fun getMaxEvaluations(): Int {
    return evaluations.maximalCount
  }

  /** {@inheritDoc}  */
  override fun getEvaluations(): Int {
    return evaluations.count
  }

  /**
   * Compute the objective function value.
   *
   * @param point Point at which the objective function must be evaluated.
   * @return the objective function value at specified point.
   * @throws TooManyEvaluationsException if the maximal number of evaluations
   * is exceeded.
   */
  @Throws(TooManyEvaluationsException::class)
  protected fun computeObjectiveValue(point: Double): Double {
    incrementEvaluationCount()
    return function.value(point)
  }

  /**
   * Prepare for computation.
   * Subclasses must call this method if they override any of the
   * `solve` methods.
   *
   * @param f Function to solve.
   * @param min Lower bound for the interval.
   * @param max Upper bound for the interval.
   * @param startValue Start value to use.
   * @param maxEval Maximum number of evaluations.
   */
  protected open fun setup(
    maxEval: Int,
    f: FUNC,
    min: Double,
    max: Double,
    startValue: Double
  ) {
    // Checks.
    checkNotNull(f)
    // Reset.
    this.min = min
    this.max = max
    this.startValue = startValue
    function = f
    evaluations = evaluations.withMaximalCount(maxEval).withStart(0)
  }

  /** {@inheritDoc}  */
  @Throws(TooManyEvaluationsException::class, NoBracketingException::class)
  override fun solve(maxEval: Int, f: FUNC, min: Double, max: Double, startValue: Double): Double {
    // Initialization.
    setup(maxEval, f, min, max, startValue)
    // Perform computation.
    return doSolve()
  }

  /** {@inheritDoc}  */
  override fun solve(maxEval: Int, f: FUNC, min: Double, max: Double): Double {
    return solve(maxEval, f, min, max, min + 0.5*(max - min))
  }

  /** {@inheritDoc}  */
  @Throws(TooManyEvaluationsException::class, NoBracketingException::class)
  override fun solve(maxEval: Int, f: FUNC, startValue: Double): Double {
    return solve(maxEval, f, Double.Companion.NaN, Double.Companion.NaN, startValue)
  }

  /**
   * Method for implementing actual optimization algorithms in derived
   * classes.
   *
   * @return the root.
   * @throws TooManyEvaluationsException if the maximal number of evaluations
   * is exceeded.
   * @throws NoBracketingException if the initial search interval does not bracket
   * a root and the solver requires it.
   */
  @Throws(TooManyEvaluationsException::class, NoBracketingException::class)
  protected abstract fun doSolve(): Double

  /**
   * Check whether the function takes opposite signs at the endpoints.
   *
   * @param lower Lower endpoint.
   * @param upper Upper endpoint.
   * @return `true` if the function values have opposite signs at the
   * given points.
   */
  protected fun isBracketing(
    lower: Double,
    upper: Double
  ): Boolean {
    return UnivariateSolverUtils.isBracketing(function, lower, upper)
  }

  /**
   * Check whether the arguments form a (strictly) increasing sequence.
   *
   * @param start First number.
   * @param mid Second number.
   * @param end Third number.
   * @return `true` if the arguments form an increasing sequence.
   */
  protected fun isSequence(
    start: Double,
    mid: Double,
    end: Double
  ): Boolean {
    return UnivariateSolverUtils.isSequence(start, mid, end)
  }

  /**
   * Check that the endpoints specify an interval.
   *
   * @param lower Lower endpoint.
   * @param upper Upper endpoint.
   * @throws NumberIsTooLargeException if `lower >= upper`.
   */
  @Throws(NumberIsTooLargeException::class)
  protected fun verifyInterval(
    lower: Double,
    upper: Double
  ) {
    UnivariateSolverUtils.verifyInterval(lower, upper)
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
  protected fun verifySequence(
    lower: Double,
    initial: Double,
    upper: Double
  ) {
    UnivariateSolverUtils.verifySequence(lower, initial, upper)
  }

  /**
   * Check that the endpoints specify an interval and the function takes
   * opposite signs at the endpoints.
   *
   * @param lower Lower endpoint.
   * @param upper Upper endpoint.
   * @throws entException if the function has not been set.
   * @throws NoBracketingException if the function has the same sign at
   * the endpoints.
   */
  @Throws(NoBracketingException::class)
  protected fun verifyBracketing(
    lower: Double,
    upper: Double
  ) {
    UnivariateSolverUtils.verifyBracketing(function, lower, upper)
  }

  /**
   * Increment the evaluation count by one.
   * Method [.computeObjectiveValue] calls this method internally.
   * It is provided for subclasses that do not exclusively use
   * `computeObjectiveValue` to solve the function.
   * See e.g. [AbstractUnivariateDifferentiableSolver].
   *
   * @throws TooManyEvaluationsException when the allowed number of function
   * evaluations has been exhausted.
   */
  @Throws(TooManyEvaluationsException::class)
  protected fun incrementEvaluationCount() {
    try {
      evaluations.increment()
    } catch (e: MaxCountExceededException) {
      throw TooManyEvaluationsException(e.max)
    }
  }

  companion object {
    /** Default relative accuracy.  */
    private const val DEFAULT_RELATIVE_ACCURACY = 1e-14

    /** Default function value accuracy.  */
    private const val DEFAULT_FUNCTION_VALUE_ACCURACY = 1e-15
  }
}