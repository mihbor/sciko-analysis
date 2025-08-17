package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.ExceptionContext
import ltd.mbor.sciko.analysis.exception.util.ExceptionContextProvider
import ltd.mbor.sciko.analysis.exception.util.Localizable
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats


/**
 * Base class for all exceptions that signal that the process
 * throwing the exception is in a state that does not comply with
 * the set of states that it is designed to be in.
 *
 * @since 2.2
 */
open class MathIllegalStateException : IllegalStateException, ExceptionContextProvider {
  /** Context.  */
  private val context: ExceptionContext

  /**
   * Simple constructor.
   *
   * @param pattern Message pattern explaining the cause of the error.
   * @param args Arguments.
   */
  constructor(
    pattern: Localizable,
    vararg args: Any
  ) {
    context = ExceptionContext(this)
    context.addMessage(pattern, *args)
  }

  /**
   * Simple constructor.
   *
   * @param cause Root cause.
   * @param pattern Message pattern explaining the cause of the error.
   * @param args Arguments.
   */
  constructor(
    cause: Throwable,
    pattern: Localizable,
    vararg args: Any
  ) : super(cause) {
    context = ExceptionContext(this)
    context.addMessage(pattern, *args)
  }

  /**
   * Default constructor.
   */
  constructor() : this(LocalizedFormats.ILLEGAL_STATE)

  /** {@inheritDoc}  */
  override fun getContext(): ExceptionContext {
    return context
  }

  override val message: String get() = context.message

  /** {@inheritDoc}  */
  override fun getLocalizedMessage(): String = context.localizedMessage

  companion object {
    /** Serializable version Id.  */
    private val serialVersionUID = -6024911025449780478L
  }
}
