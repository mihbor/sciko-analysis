package ltd.mbor.sciko.analysis

/**
 * An interface representing a real function that depends on one independent
 * variable plus some extra parameters.
 *
 * @since 3.0
 */
interface ParametricUnivariateFunction {
  /**
   * Compute the value of the function.
   *
   * @param x Point for which the function value should be computed.
   * @param parameters Function parameters.
   * @return the value.
   */
  fun value(x: Double, vararg parameters: Double): Double

  /**
   * Compute the gradient of the function with respect to its parameters.
   *
   * @param x Point for which the function value should be computed.
   * @param parameters Function parameters.
   * @return the value.
   */
  fun gradient(x: Double, vararg parameters: Double): DoubleArray?
}
