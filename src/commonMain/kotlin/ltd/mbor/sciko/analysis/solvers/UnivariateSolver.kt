package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.UnivariateFunction

/**
 * Interface for (univariate real) root-finding algorithms.
 * Implementations will search for only one zero in the given interval.
 *
 */
interface UnivariateSolver : BaseUnivariateSolver<UnivariateFunction>
