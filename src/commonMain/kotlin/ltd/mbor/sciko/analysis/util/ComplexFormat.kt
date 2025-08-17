package ltd.mbor.sciko.analysis.util

import ltd.mbor.sciko.analysis.exception.MathIllegalArgumentException
import ltd.mbor.sciko.analysis.exception.MathParseException
import ltd.mbor.sciko.analysis.exception.NoDataException
import ltd.mbor.sciko.analysis.exception.util.CompositeFormat
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats
import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDouble
import java.text.FieldPosition
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.*
import kotlin.Any
import kotlin.Array
import kotlin.IllegalArgumentException
import kotlin.Number
import kotlin.String
import kotlin.Throws
import kotlin.unaryMinus


/**
 * Formats a Complex number in cartesian format "Re(c) + Im(c)i".  'i' can
 * be replaced with 'j' (or anything else), and the number format for both real
 * and imaginary parts can be configured.
 *
 */
class ComplexFormat {
  /**
   * Access the imaginaryCharacter.
   * @return the imaginaryCharacter.
   */
  /** The notation used to signify the imaginary part of the complex number.  */
  val imaginaryCharacter: String
  /**
   * Access the imaginaryFormat.
   * @return the imaginaryFormat.
   */
  /** The format used for the imaginary part.  */
  val imaginaryFormat: NumberFormat
  /**
   * Access the realFormat.
   * @return the realFormat.
   */
  /** The format used for the real part.  */
  val realFormat: NumberFormat

  /**
   * Create an instance with the default imaginary character, 'i', and the
   * default number format for both real and imaginary parts.
   */
  constructor() {
    this.imaginaryCharacter = DEFAULT_IMAGINARY_CHARACTER
    this.imaginaryFormat = CompositeFormat.getDefaultNumberFormat()
    this.realFormat = imaginaryFormat
  }

  /**
   * Create an instance with a custom number format for both real and
   * imaginary parts.
   * @param format the custom format for both real and imaginary parts.
   */
  constructor(format: NumberFormat) {
    this.imaginaryCharacter = DEFAULT_IMAGINARY_CHARACTER
    this.imaginaryFormat = format
    this.realFormat = format
  }

  /**
   * Create an instance with a custom number format for the real part and a
   * custom number format for the imaginary part.
   * @param realFormat the custom format for the real part.
   * @param imaginaryFormat the custom format for the imaginary part.
   */
  constructor(realFormat: NumberFormat, imaginaryFormat: NumberFormat) {
    this.imaginaryCharacter = DEFAULT_IMAGINARY_CHARACTER
    this.imaginaryFormat = imaginaryFormat
    this.realFormat = realFormat
  }
  /**
   * Create an instance with a custom imaginary character, and a custom number
   * format for both real and imaginary parts.
   * @param imaginaryCharacter The custom imaginary character.
   * @param format the custom format for both real and imaginary parts.
   * @throws NoDataException if `imaginaryCharacter` is an
   * empty string.
   */
  @JvmOverloads
  constructor(imaginaryCharacter: String, format: NumberFormat = CompositeFormat.getDefaultNumberFormat()) : this(imaginaryCharacter, format, format)

  /**
   * Create an instance with a custom imaginary character, a custom number
   * format for the real part, and a custom number format for the imaginary
   * part.
   *
   * @param imaginaryCharacter The custom imaginary character.
   * @param realFormat the custom format for the real part.
   * @param imaginaryFormat the custom format for the imaginary part.
   * @throws NoDataException if `imaginaryCharacter` is an
   * empty string.
   */
  constructor(
    imaginaryCharacter: String,
    realFormat: NumberFormat,
    imaginaryFormat: NumberFormat
  ) {
    if (imaginaryCharacter.length == 0) {
      throw NoDataException()
    }
    this.imaginaryCharacter = imaginaryCharacter
    this.imaginaryFormat = imaginaryFormat
    this.realFormat = realFormat
  }

  /**
   * This method calls [.format].
   *
   * @param c ComplexDouble object to format.
   * @return A formatted number in the form "Re(c) + Im(c)i".
   */
  fun format(c: ComplexDouble): String {
    return format(c, StringBuffer(), FieldPosition(0)).toString()
  }

  /**
   * This method calls [.format].
   *
   * @param c Double object to format.
   * @return A formatted number.
   */
  fun format(c: Double): String {
    return format(ComplexDouble(c, 0), StringBuffer(), FieldPosition(0)).toString()
  }

  /**
   * Formats a [ComplexDouble] object to produce a string.
   *
   * @param complex the object to format.
   * @param toAppendTo where the text is to be appended
   * @param pos On input: an alignment field, if desired. On output: the
   * offsets of the alignment field
   * @return the value passed in as toAppendTo.
   */
  fun format(
    complex: ComplexDouble,
    toAppendTo: StringBuffer,
    pos: FieldPosition
  ): StringBuffer {
    pos.setBeginIndex(0)
    pos.setEndIndex(0)
    // format real
    val re: Double = complex.re
    CompositeFormat.formatDouble(re, this.realFormat, toAppendTo, pos)
    // format sign and imaginary
    val im: Double = complex.im
    val imAppendTo: StringBuffer?
    if (im < 0.0) {
      toAppendTo.append(" - ")
      imAppendTo = formatImaginary(-im, StringBuffer(), pos)
      toAppendTo.append(imAppendTo)
      toAppendTo.append(this.imaginaryCharacter)
    } else if (im > 0.0 || im.isNaN()) {
      toAppendTo.append(" + ")
      imAppendTo = formatImaginary(im, StringBuffer(), pos)
      toAppendTo.append(imAppendTo)
      toAppendTo.append(this.imaginaryCharacter)
    }
    return toAppendTo
  }

  /**
   * Format the absolute value of the imaginary part.
   *
   * @param absIm Absolute value of the imaginary part of a complex number.
   * @param toAppendTo where the text is to be appended.
   * @param pos On input: an alignment field, if desired. On output: the
   * offsets of the alignment field.
   * @return the value passed in as toAppendTo.
   */
  private fun formatImaginary(
    absIm: Double,
    toAppendTo: StringBuffer,
    pos: FieldPosition
  ): StringBuffer {
    pos.setBeginIndex(0)
    pos.setEndIndex(0)
    CompositeFormat.formatDouble(absIm, this.imaginaryFormat, toAppendTo, pos)
    if (toAppendTo.toString() == "1") {
      // Remove the character "1" if it is the only one.
      toAppendTo.setLength(0)
    }
    return toAppendTo
  }

  /**
   * Formats a object to produce a string.  `obj` must be either a
   * [ComplexDouble] object or a [Number] object.  Any other type of
   * object will result in an [IllegalArgumentException] being thrown.
   *
   * @param obj the object to format.
   * @param toAppendTo where the text is to be appended
   * @param pos On input: an alignment field, if desired. On output: the
   * offsets of the alignment field
   * @return the value passed in as toAppendTo.
   * @see java.text.Format.format
   * @throws MathIllegalArgumentException is `obj` is not a valid type.
   */
  @Throws(MathIllegalArgumentException::class)
  fun format(
    obj: Any,
    toAppendTo: StringBuffer,
    pos: FieldPosition
  ): StringBuffer {
    var ret: StringBuffer
    if (obj is ComplexDouble) {
      ret = format(obj, toAppendTo, pos)
    } else if (obj is Number) {
      ret = format(
        ComplexDouble(obj.toDouble(), 0.0),
        toAppendTo, pos
      )
    } else {
      throw MathIllegalArgumentException(
        LocalizedFormats.CANNOT_FORMAT_INSTANCE_AS_COMPLEX,
        obj.javaClass.getName()
      )
    }
    return ret
  }

  /**
   * Parses a string to produce a [Complex] object.
   *
   * @param source the string to parse.
   * @return the parsed [Complex] object.
   * @throws MathParseException if the beginning of the specified string
   * cannot be parsed.
   */
  @Throws(MathParseException::class)
  fun parse(source: String): ComplexDouble? {
    val parsePosition = ParsePosition(0)
    val result: ComplexDouble? = parse(source, parsePosition)
    if (parsePosition.getIndex() == 0) {
      throw MathParseException(
        source,
        parsePosition.getErrorIndex(),
        ComplexDouble::class.java
      )
    }
    return result
  }

  /**
   * Parses a string to produce a [ComplexDouble] object.
   *
   * @param source the string to parse
   * @param pos input/ouput parsing parameter.
   * @return the parsed [ComplexDouble] object.
   */
  fun parse(source: String, pos: ParsePosition): ComplexDouble? {
    val initialIndex = pos.getIndex()
    // parse whitespace
    CompositeFormat.parseAndIgnoreWhitespace(source, pos)
    // parse real
    val re = CompositeFormat.parseNumber(source, this.realFormat, pos)
    if (re == null) {
      // invalid real number
      // set index back to initial, error index should already be set
      pos.setIndex(initialIndex)
      return null
    }
    // parse sign
    val startIndex = pos.getIndex()
    val c = CompositeFormat.parseNextCharacter(source, pos)
    var sign = 0
    when (c) {
      0.toChar() ->             // no sign
        // return real only complex number
        return ComplexDouble(re.toDouble(), 0.0)

      '-' -> sign = -1
      '+' -> sign = 1
      else -> {
        // invalid sign
        // set index back to initial, error index should be the last
        // character examined.
        pos.setIndex(initialIndex)
        pos.setErrorIndex(startIndex)
        return null
      }
    }
    // parse whitespace
    CompositeFormat.parseAndIgnoreWhitespace(source, pos)
    // parse imaginary
    val im = CompositeFormat.parseNumber(source, this.realFormat, pos)
    if (im == null) {
      // invalid imaginary number
      // set index back to initial, error index should already be set
      pos.setIndex(initialIndex)
      return null
    }
    // parse imaginary character
    if (!CompositeFormat.parseFixedstring(source, this.imaginaryCharacter, pos)) {
      return null
    }
    return ComplexDouble(re.toDouble(), im.toDouble()*sign)
  }

  companion object {
    /** The default imaginary character.  */
    private const val DEFAULT_IMAGINARY_CHARACTER = "i"
    val availableLocales: Array<Locale?>?
      /**
       * Get the set of locales for which complex formats are available.
       *
       * This is the same set as the [NumberFormat] set.
       * @return available complex format locales.
       */
      get() = NumberFormat.getAvailableLocales()
    val instance: ComplexFormat
      /**
       * Returns the default complex format for the current locale.
       * @return the default complex format.
       */
      get() = getInstance(Locale.getDefault())

    /**
     * Returns the default complex format for the given locale.
     * @param locale the specific locale used by the format.
     * @return the complex format specific to the given locale.
     */
    fun getInstance(locale: Locale?): ComplexFormat {
      val f = CompositeFormat.getDefaultNumberFormat(locale)
      return ComplexFormat(f)
    }

    /**
     * Returns the default complex format for the given locale.
     * @param locale the specific locale used by the format.
     * @param imaginaryCharacter Imaginary character.
     * @return the complex format specific to the given locale.
     * @throws NoDataException if `imaginaryCharacter` is an
     * empty string.
     */
    @Throws(NoDataException::class)
    fun getInstance(imaginaryCharacter: String, locale: Locale?): ComplexFormat {
      val f = CompositeFormat.getDefaultNumberFormat(locale)
      return ComplexFormat(imaginaryCharacter, f)
    }
  }
}