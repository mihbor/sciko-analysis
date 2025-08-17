package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.PolynomialFunction


/**
 * Interface for (polynomial) root-finding algorithms.
 * Implementations will search for only one zero in the given interval.
 *
 * @since 3.0
 */
interface PolynomialSolver : BaseUnivariateSolver<PolynomialFunction>
