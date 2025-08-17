package ltd.mbor.sciko.analysis.exception.util

import java.io.Serializable
import java.util.Locale

/**
 * Interface for localizable strings.
 *
 * @since 2.2
 */
interface Localizable : Serializable {
  /**
   * Gets the source (non-localized) string.
   *
   * @return the source string.
   */
  val sourceString: String

  /**
   * Gets the localized string.
   *
   * @param locale locale into which to get the string.
   * @return the localized string or the source string if no
   * localized version is available.
   */
  fun getLocalizedString(locale: Locale): String
}