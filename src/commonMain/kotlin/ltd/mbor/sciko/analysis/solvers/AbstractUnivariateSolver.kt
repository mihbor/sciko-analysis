package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.UnivariateFunction

/**
 * Base class for solvers.
 */
abstract class AbstractUnivariateSolver : BaseAbstractUnivariateSolver<UnivariateFunction>, UnivariateSolver {
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
}
