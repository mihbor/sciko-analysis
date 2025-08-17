package ltd.mbor.sciko.analysis.exception.util
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.text.MessageFormat
import java.util.*


/**
 * Class that contains the actual implementation of the functionality mandated
 * by the [ExceptionContext] interface.
 * All Commons Math exceptions delegate the interface's methods to this class.
 *
 * @since 3.0
 */
class ExceptionContext(throwable: Throwable) : Serializable {
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

  val keys: MutableSet<String>
    /**
     * Gets all the keys stored in the exception
     *
     * @return the set of keys.
     */
    get() = context.keys
  val message: String
    /**
     * Gets the default message.
     *
     * @return the message.
     */
    get() = getMessage(Locale.US)
  val localizedMessage: String
    /**
     * Gets the message in the default locale.
     *
     * @return the localized message.
     */
    get() = getMessage(Locale.getDefault())

  /**
   * Gets the message in a specified locale.
   *
   * @param locale Locale in which the message should be translated.
   * @return the localized message.
   */
  fun getMessage(locale: Locale): String {
    return buildMessage(locale, ": ")
  }

  /**
   * Gets the message in a specified locale.
   *
   * @param locale Locale in which the message should be translated.
   * @param separator Separator inserted between the message parts.
   * @return the localized message.
   */
  fun getMessage(
    locale: Locale,
    separator: String
  ): String {
    return buildMessage(locale, separator)
  }

  /**
   * Builds a message string.
   *
   * @param locale Locale in which the message should be translated.
   * @param separator Message separator.
   * @return a localized message string.
   */
  private fun buildMessage(
    locale: Locale,
    separator: String
  ): String {
    val sb = StringBuilder()
    var count = 0
    val len = msgPatterns.size
    for (i in 0..<len) {
      val pat = msgPatterns.get(i)
      val args = msgArguments.get(i)
      val fmt = MessageFormat(
        pat.getLocalizedString(locale),
        locale
      )
      sb.append(fmt.format(args))
      if (++count < len) {
        // Add a separator if there are other messages.
        sb.append(separator)
      }
    }
    return sb.toString()
  }

  /**
   * Serialize this object to the given stream.
   *
   * @param out Stream.
   * @throws IOException This should never happen.
   */
  @Throws(IOException::class)
  private fun writeObject(out: ObjectOutputStream) {
    out.writeObject(throwable)
    serializeMessages(out)
    serializeContext(out)
  }

  /**
   * Deserialize this object from the given stream.
   *
   * @param in Stream.
   * @throws IOException This should never happen.
   * @throws ClassNotFoundException This should never happen.
   */
  @Throws(IOException::class, ClassNotFoundException::class)
  private fun readObject(`in`: ObjectInputStream) {
    throwable = `in`.readObject() as Throwable
    deSerializeMessages(`in`)
    deSerializeContext(`in`)
  }

  /**
   * Serialize  [.msgPatterns] and [.msgArguments].
   *
   * @param out Stream.
   * @throws IOException This should never happen.
   */
  @Throws(IOException::class)
  private fun serializeMessages(out: ObjectOutputStream) {
    // Step 1.
    val len = msgPatterns.size
    out.writeInt(len)
    // Step 2.
    for (i in 0..<len) {
      val pat: Localizable = msgPatterns.get(i)
      // Step 3.
      out.writeObject(pat)
      val args: Array<Any> = msgArguments.get(i)
      val aLen = args.size
      // Step 4.
      out.writeInt(aLen)
      for (j in 0..<aLen) {
        if (args[j] is Serializable) {
          // Step 5a.
          out.writeObject(args[j])
        } else {
          // Step 5b.
          out.writeObject(nonSerializableReplacement(args[j]))
        }
      }
    }
  }

  /**
   * Deserialize [.msgPatterns] and [.msgArguments].
   *
   * @param in Stream.
   * @throws IOException This should never happen.
   * @throws ClassNotFoundException This should never happen.
   */
  @Throws(IOException::class, ClassNotFoundException::class)
  private fun deSerializeMessages(`in`: ObjectInputStream) {
    // Step 1.
    val len = `in`.readInt()
    msgPatterns = ArrayList<Localizable>(len)
    msgArguments = ArrayList<Array<Any>>(len)
    // Step 2.
    for (i in 0..<len) {
      // Step 3.
      val pat = `in`.readObject() as Localizable
      msgPatterns.add(pat)
      // Step 4.
      val aLen = `in`.readInt()
      val args = arrayOfNulls<Any>(aLen)
      for (j in 0..<aLen) {
        // Step 5.
        args[j] = `in`.readObject()
      }
      msgArguments.add(args as Array<Any>)
    }
  }

  /**
   * Serialize [.context].
   *
   * @param out Stream.
   * @throws IOException This should never happen.
   */
  @Throws(IOException::class)
  private fun serializeContext(out: ObjectOutputStream) {
    // Step 1.
    val len = context.size
    out.writeInt(len)
    for (entry in context.entries) {
      // Step 2.
      out.writeObject(entry.key)
      val value: Any = entry.value
      if (value is Serializable) {
        // Step 3a.
        out.writeObject(value)
      } else {
        // Step 3b.
        out.writeObject(nonSerializableReplacement(value))
      }
    }
  }

  /**
   * Deserialize [.context].
   *
   * @param in Stream.
   * @throws IOException This should never happen.
   * @throws ClassNotFoundException This should never happen.
   */
  @Throws(IOException::class, ClassNotFoundException::class)
  private fun deSerializeContext(`in`: ObjectInputStream) {
    // Step 1.
    val len = `in`.readInt()
    context = HashMap<String, Any>()
    for (i in 0..<len) {
      // Step 2.
      val key = `in`.readObject() as String
      // Step 3.
      val value = `in`.readObject()
      context.put(key, value)
    }
  }

  /**
   * Replaces a non-serializable object with an error message string.
   *
   * @param obj Object that does not implement the `Serializable`
   * interface.
   * @return a string that mentions which class could not be serialized.
   */
  private fun nonSerializableReplacement(obj: Any): String {
    return "[Object could not be serialized: " + obj.javaClass.getName() + "]"
  }

  companion object {
    /** Serializable version Id.  */
    private val serialVersionUID = -6024911025449780478L
  }
}