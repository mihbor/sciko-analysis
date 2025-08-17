package ltd.mbor.sciko.analysis.exception.util

import java.text.FieldPosition
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.*
import kotlin.Boolean
import kotlin.Char
import kotlin.Number
import kotlin.String
import kotlin.doubleArrayOf


/**
 * Base class for formatters of composite objects (complex numbers, vectors ...).
 *
 */
object CompositeFormat {
  /**
   * Create a default number format.  The default number format is based on
   * [NumberFormat.getInstance] with the only customizing that the
   * maximum number of fraction digits is set to 10.
   * @return the default number format.
   */
  fun getDefaultNumberFormat(): NumberFormat = getDefaultNumberFormat(Locale.getDefault())

  /**
   * Create a default number format.  The default number format is based on
   * [NumberFormat.getInstance] with the only
   * customizing that the maximum number of fraction digits is set to 10.
   * @param locale the specific locale used by the format.
   * @return the default number format specific to the given locale.
   */
  fun getDefaultNumberFormat(locale: Locale?): NumberFormat {
    val nf = NumberFormat.getInstance(locale)
    nf.setMaximumFractionDigits(10)
    return nf
  }

  /**
   * Parses `source` until a non-whitespace character is found.
   *
   * @param source the string to parse
   * @param pos input/output parsing parameter.  On output, `pos`
   * holds the index of the next non-whitespace character.
   */
  fun parseAndIgnoreWhitespace(
    source: String,
    pos: ParsePosition
  ) {
    parseNextCharacter(source, pos)
    pos.setIndex(pos.getIndex() - 1)
  }

  /**
   * Parses `source` until a non-whitespace character is found.
   *
   * @param source the string to parse
   * @param pos input/output parsing parameter.
   * @return the first non-whitespace character.
   */
  fun parseNextCharacter(
    source: String,
    pos: ParsePosition
  ): Char {
    var index = pos.getIndex()
    val n = source.length
    var ret = 0.toChar()
    if (index < n) {
      var c: Char
      do {
        c = source.get(index++)
      } while (Character.isWhitespace(c) && index < n)
      pos.setIndex(index)
      if (index < n) {
        ret = c
      }
    }
    return ret
  }

  /**
   * Parses `source` for special double values.  These values
   * include Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY.
   *
   * @param source the string to parse
   * @param value the special value to parse.
   * @param pos input/output parsing parameter.
   * @return the special number.
   */
  private fun parseNumber(
    source: String, value: Double,
    pos: ParsePosition
  ): Number? {
    var ret: Number? = null
    val sb = StringBuilder()
    sb.append('(')
    sb.append(value)
    sb.append(')')
    val n = sb.length
    val startIndex = pos.getIndex()
    val endIndex = startIndex + n
    if (endIndex < source.length &&
      source.substring(startIndex, endIndex).compareTo(sb.toString()) == 0
    ) {
      ret = value
      pos.setIndex(endIndex)
    }
    return ret
  }

  /**
   * Parses `source` for a number.  This method can parse normal,
   * numeric values as well as special values.  These special values include
   * Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY.
   *
   * @param source the string to parse
   * @param format the number format used to parse normal, numeric values.
   * @param pos input/output parsing parameter.
   * @return the parsed number.
   */
  fun parseNumber(
    source: String, format: NumberFormat,
    pos: ParsePosition
  ): Number? {
    val startIndex = pos.getIndex()
    var number = format.parse(source, pos)
    val endIndex = pos.getIndex()
    // check for error parsing number
    if (startIndex == endIndex) {
      // try parsing special numbers
      val special = doubleArrayOf(
        Double.Companion.NaN, Double.Companion.POSITIVE_INFINITY, Double.Companion.NEGATIVE_INFINITY
      )
      for (i in special.indices) {
        number = parseNumber(source, special[i], pos)
        if (number != null) {
          break
        }
      }
    }
    return number
  }

  /**
   * Parse `source` for an expected fixed string.
   * @param source the string to parse
   * @param expected expected string
   * @param pos input/output parsing parameter.
   * @return true if the expected string was there
   */
  fun parseFixedstring(
    source: String,
    expected: String,
    pos: ParsePosition
  ): Boolean {
    val startIndex = pos.getIndex()
    val endIndex = startIndex + expected.length
    if ((startIndex >= source.length) ||
      (endIndex > source.length) ||
      (source.substring(startIndex, endIndex).compareTo(expected) != 0)
    ) {
      // set index back to start, error index should be the start index
      pos.setIndex(startIndex)
      pos.setErrorIndex(startIndex)
      return false
    }
    // the string was here
    pos.setIndex(endIndex)
    return true
  }

  /**
   * Formats a double value to produce a string.  In general, the value is
   * formatted using the formatting rules of `format`.  There are
   * three exceptions to this:
   *
   *  1. NaN is formatted as '(NaN)'
   *  1. Positive infinity is formatted as '(Infinity)'
   *  1. Negative infinity is formatted as '(-Infinity)'
   *
   *
   * @param value the double to format.
   * @param format the format used.
   * @param toAppendTo where the text is to be appended
   * @param pos On input: an alignment field, if desired. On output: the
   * offsets of the alignment field
   * @return the value passed in as toAppendTo.
   */
  fun formatDouble(
    value: Double,
    format: NumberFormat,
    toAppendTo: StringBuffer,
    pos: FieldPosition?
  ): StringBuffer {
    if (value.isNaN() || value.isInfinite()) {
      toAppendTo.append('(')
      toAppendTo.append(value)
      toAppendTo.append(')')
    } else {
      format.format(value, toAppendTo, pos)
    }
    return toAppendTo
  }
}
