package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.ExceptionContext
import ltd.mbor.sciko.analysis.exception.util.ExceptionContextProvider
import ltd.mbor.sciko.analysis.exception.util.Localizable
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats

/**
 * Base class for all unsupported features.
 * It is used for all the exceptions that have the semantics of the standard
 * [UnsupportedOperationException], but must also provide a localized
 * message.
 */
class MathUnsupportedOperationException(
  pattern: Localizable,
  vararg args: Any
) : UnsupportedOperationException(), ExceptionContextProvider {
  /** Context.  */
  private val context: ExceptionContext

  /**
   * Default constructor.
   */
  constructor() : this(LocalizedFormats.UNSUPPORTED_OPERATION)

  /**
   * @param pattern Message pattern providing the specific context of
   * the error.
   * @param args Arguments.
   */
  init {
    context = ExceptionContext(this)
    context.addMessage(pattern, *args)
  }

  /** {@inheritDoc}  */
  override fun getContext(): ExceptionContext {
    return context
  }

}
