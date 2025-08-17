package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.Localizable
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats

/**
 * Exception to be thrown when the required data is missing.
 *
 * @since 2.2
 */
class NoDataException
/**
 * Construct the exception with a specific context.
 *
 * @param specific Contextual information on what caused the exception.
 */
/**
 * Construct the exception.
 */
@JvmOverloads constructor(specific: Localizable = LocalizedFormats.NO_DATA) : MathIllegalArgumentException(specific) {
  companion object {
    /** Serializable version Id.  */
    private val serialVersionUID = -3629324471511904459L
  }
}