package ltd.mbor.sciko.analysis.exception

import ltd.mbor.sciko.analysis.exception.util.ExceptionContext
import ltd.mbor.sciko.analysis.exception.util.ExceptionContextProvider
import ltd.mbor.sciko.analysis.exception.util.Localizable

/**
 * Base class for all preconditions violation exceptions.
 * In most cases, this class should not be instantiated directly: it should
 * serve as a base class to create all the exceptions that have the semantics
 * of the standard [IllegalArgumentException].
 */
open class MathIllegalArgumentException(
  pattern: Localizable,
  vararg args: Any
) : IllegalArgumentException(), ExceptionContextProvider {
  /** Context.  */
  private val context: ExceptionContext

  /**
   * @param pattern Message pattern explaining the cause of the error.
   * @param args Arguments.
   */
  init {
    context = ExceptionContext(this)
    context.addMessage(pattern, *args)
  }

  override fun getContext(): ExceptionContext {
    return context
  }

}
