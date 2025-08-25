package ltd.mbor.sciko.analysis.exception.util

/**
 * Class that contains the actual implementation of the functionality mandated
 * by the [ExceptionContext] interface.
 * All Commons Math exceptions delegate the interface's methods to this class.
 */
class ExceptionContext(throwable: Throwable) {
  /** Get a reference to the exception to which the context relates.
   * @return a reference to the exception to which the context relates
   */
  /**
   * The throwable to which this context refers to.
   */
  var throwable: Throwable
    private set

  /**
   * Various informations that enrich the informative message.
   */
  private var msgPatterns: MutableList<Localizable>

  /**
   * Various informations that enrich the informative message.
   * The arguments will replace the corresponding place-holders in
   * [.msgPatterns].
   */
  private var msgArguments: MutableList<Array<Any>>

  /**
   * Arbitrary context information.
   */
  private var context: MutableMap<String, Any>

  /** Simple constructor.
   * @param throwable the exception this context refers too
   */
  init {
    this.throwable = throwable
    msgPatterns = ArrayList<Localizable>()
    msgArguments = ArrayList<Array<Any>>()
    context = HashMap<String, Any>()
  }

  /**
   * Adds a message.
   *
   * @param pattern Message pattern.
   * @param arguments Values for replacing the placeholders in the message
   * pattern.
   */
  fun addMessage(
    pattern: Localizable,
    vararg arguments: Any
  ) {
    msgPatterns.add(pattern)
    msgArguments.add(ArgUtils.flatten(arguments))
  }

  /**
   * Sets the context (key, value) pair.
   * Keys are assumed to be unique within an instance. If the same key is
   * assigned a new value, the previous one will be lost.
   *
   * @param key Context key (not null).
   * @param value Context value.
   */
  fun setValue(key: String, value: Any) {
    context.put(key, value)
  }

  /**
   * Gets the value associated to the given context key.
   *
   * @param key Context key.
   * @return the context value or `null` if the key does not exist.
   */
  fun getValue(key: String): Any? {
    return context.get(key)
  }

}