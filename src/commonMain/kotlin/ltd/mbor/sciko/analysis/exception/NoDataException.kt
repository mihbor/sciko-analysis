package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.Localizable
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats

/**
 * Exception to be thrown when the required data is missing.
 */
class NoDataException(specific: Localizable = LocalizedFormats.NO_DATA) : MathIllegalArgumentException(specific)