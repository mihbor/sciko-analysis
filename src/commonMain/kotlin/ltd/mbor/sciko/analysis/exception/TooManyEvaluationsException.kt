package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats

/**
 * Exception to be thrown when the maximal number of evaluations is exceeded.
 *
 * @since 3.0
 */
class TooManyEvaluationsException(max: Number) : MaxCountExceededException(max) {
  /**
   * Construct the exception.
   *
   * @param max Maximum number of evaluations.
   */
  init {
    getContext().addMessage(LocalizedFormats.EVALUATIONS)
  }

  companion object {
    /** Serializable version Id.  */
    private const val serialVersionUID = 4330003017885151975L
  }
}
