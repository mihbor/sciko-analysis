package ltd.mbor.sciko.analysis

import ltd.mbor.sciko.analysis.exception.NoDataException
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats
import ltd.mbor.sciko.analysis.exception.util.copyInto
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Immutable representation of a real polynomial function with real coefficients.
 *
 *
 * [Horner's Method](http://mathworld.wolfram.com/HornersMethod.html)
 * is used to evaluate the function.
 *
 */
class PolynomialFunction(c: MultiArray<Double, D1>): UnivariateFunction {
  /**
   * The coefficients of the polynomial, ordered by degree -- i.e.,
   * coefficients[0] is the constant term and coefficients[n] is the
   * coefficient of x^n where n is the degree of the polynomial.
   */
  private val coefficients: MultiArray<Double, D1>

  /**
   * Construct a polynomial with the given coefficients.  The first element
   * of the coefficients array is the constant term.  Higher degree
   * coefficients follow in sequence.  The degree of the resulting polynomial
   * is the index of the last non-null element of the array, or 0 if all elements
   * are null.
   *
   *
   * The constructor makes a copy of the input array and assigns the copy to
   * the coefficients property.
   *
   * @param c Polynomial coefficients.
   * @throws NoDataException if `c` is empty.
   */
  init {
    requireNotNull(c)
    var n = c .size
    if (n == 0) {
      throw NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY)
    }
    while ((n > 1) && (c[n - 1] == 0.0)) {
      --n
    }
    this.coefficients = mk.zeros(n)
    c.copyInto(this.coefficients, 0, 0, n)
  }

  /**
   * Compute the value of the function for the given argument.
   *
   *
   * The value returned is
   *
   *
   * `coefficients[n] * x^n + ... + coefficients[1] * x  + coefficients[0]`
   *
   *
   * @param x Argument for which the function value should be computed.
   * @return the value of the polynomial at the given point.
   * @see UnivariateFunction.value
   */
  override fun value(x: Double): Double {
    return Companion.evaluate(coefficients, x)
  }

  /**
   * Returns the degree of the polynomial.
   *
   * @return the degree of the polynomial.
   */
  fun degree(): Int {
    return coefficients.size - 1
  }

  /**
   * Returns a copy of the coefficients array.
   *
   *
   * Changes made to the returned copy will not affect the coefficients of
   * the polynomial.
   *
   * @return a fresh copy of the coefficients array.
   */
  fun getCoefficients(): MultiArray<Double, D1> {
    return coefficients.copy()
  }

  /**
   * Add a polynomial to the instance.
   *
   * @param p Polynomial to add.
   * @return a new polynomial which is the sum of the instance and `p`.
   */
  fun add(p: PolynomialFunction): PolynomialFunction {
    // identify the lowest degree polynomial
    val lowLength = min(coefficients.size, p.coefficients.size)
    val highLength = max(coefficients.size, p.coefficients.size)
    // build the coefficients array
    val newCoefficients = mk.zeros<Double>(highLength)
    for (i in 0..<lowLength) {
      newCoefficients[i] = coefficients[i] + p.coefficients[i]
    }
    (if (coefficients.size < p.coefficients.size) p.coefficients else coefficients).copyInto(
      newCoefficients,
      lowLength,
      lowLength,
      highLength - lowLength
    )
    return PolynomialFunction(newCoefficients)
  }

  /**
   * Subtract a polynomial from the instance.
   *
   * @param p Polynomial to subtract.
   * @return a new polynomial which is the instance minus `p`.
   */
  fun subtract(p: PolynomialFunction): PolynomialFunction {
    // identify the lowest degree polynomial
    val lowLength = min(coefficients.size, p.coefficients.size)
    val highLength = max(coefficients.size, p.coefficients.size)
    // build the coefficients array
    val newCoefficients = mk.zeros<Double>(highLength)
    for (i in 0..<lowLength) {
      newCoefficients[i] = coefficients[i] - p.coefficients[i]
    }
    if (coefficients.size < p.coefficients.size) {
      for (i in lowLength..<highLength) {
        newCoefficients[i] = -p.coefficients[i]
      }
    } else {
      coefficients.copyInto(newCoefficients, lowLength, lowLength, highLength - lowLength)
    }
    return PolynomialFunction(newCoefficients)
  }

  /**
   * Negate the instance.
   *
   * @return a new polynomial with all coefficients negated
   */
  fun negate(): PolynomialFunction {
    val newCoefficients = mk.zeros<Double>(coefficients.size)
    for (i in coefficients.indices) {
      newCoefficients[i] = -coefficients[i]
    }
    return PolynomialFunction(newCoefficients)
  }

  /**
   * Multiply the instance by a polynomial.
   *
   * @param p Polynomial to multiply by.
   * @return a new polynomial equal to this times `p`
   */
  fun multiply(p: PolynomialFunction): PolynomialFunction {
    val newCoefficients = mk.zeros<Double>(coefficients.size + p.coefficients.size - 1)
    for (i in newCoefficients.indices) {
      newCoefficients[i] = 0.0
      for (j in max(0, i + 1 - p.coefficients.size)..<min(coefficients.size, i + 1)) {
        newCoefficients[i] += coefficients[j]*p.coefficients[i - j]
      }
    }
    return PolynomialFunction(newCoefficients)
  }

  /**
   * Returns the derivative as a [PolynomialFunction].
   *
   * @return the derivative polynomial.
   */
  fun polynomialDerivative(): PolynomialFunction {
    return PolynomialFunction(Companion.differentiate(coefficients))
  }


  /**
   * Returns a string representation of the polynomial.
   *
   *
   * The representation is user oriented. Terms are displayed lowest
   * degrees first. The multiplications signs, coefficients equals to
   * one and null terms are not displayed (except if the polynomial is 0,
   * in which case the 0 constant term is displayed). Addition of terms
   * with negative coefficients are replaced by subtraction of terms
   * with positive coefficients except for the first displayed term
   * (i.e. we display `-3` for a constant negative polynomial,
   * but `1 - 3 x + x^2` if the negative coefficient is not
   * the first one displayed).
   *
   * @return a string representation of the polynomial.
   */
  override fun toString(): String {
    val s = StringBuilder()
    if (coefficients[0] == 0.0) {
      if (coefficients.size == 1) {
        return "0"
      }
    } else {
      s.append(toString(coefficients[0]))
    }
    for (i in 1..<coefficients.size) {
      if (coefficients[i] != 0.0) {
        if (s.length > 0) {
          if (coefficients[i] < 0) {
            s.append(" - ")
          } else {
            s.append(" + ")
          }
        } else {
          if (coefficients[i] < 0) {
            s.append("-")
          }
        }
        val absAi = abs(coefficients[i])
        if ((absAi - 1) != 0.0) {
          s.append(toString(absAi))
          s.append(' ')
        }
        s.append("x")
        if (i > 1) {
          s.append('^')
          s.append(i.toString())
        }
      }
    }
    return s.toString()
  }

  /** {@inheritDoc}  */
  override fun hashCode(): Int {
    val prime = 31
    var result = 1
    result = prime*result + coefficients.toDoubleArray().contentHashCode()
    return result
  }

  /** {@inheritDoc}  */
  override fun equals(obj: Any?): Boolean {
    if (this === obj) {
      return true
    }
    if (obj !is PolynomialFunction) {
      return false
    }
    val other = obj
    if (!coefficients.toDoubleArray().contentEquals(other.coefficients.toDoubleArray())) {
      return false
    }
    return true
  }

  /**
   * Dedicated parametric polynomial class.
   *
   * @since 3.0
   */
  class Parametric : ParametricUnivariateFunction {
    /** {@inheritDoc}  */
    override fun gradient(x: Double, vararg parameters: Double): DoubleArray {
      val gradient = DoubleArray(parameters.size)
      var xn = 1.0
      for (i in parameters.indices) {
        gradient[i] = xn
        xn *= x
      }
      return gradient
    }

    /** {@inheritDoc}  */
    @Throws(NoDataException::class)
    override fun value(x: Double, vararg parameters: Double): Double {
      return evaluate(mk.ndarray(parameters), x)
    }
  }

  companion object {
    /**
     * Serialization identifier
     */
    private val serialVersionUID = -7726511984200295583L

    /**
     * Uses Horner's Method to evaluate the polynomial with the given coefficients at
     * the argument.
     *
     * @param coefficients Coefficients of the polynomial to evaluate.
     * @param argument Input value.
     * @return the value of the polynomial.
     * @throws NoDataException if `coefficients` is empty.
     */
    @Throws(NoDataException::class)
    protected fun evaluate(coefficients: MultiArray<Double, D1>, argument: Double): Double {
      requireNotNull(coefficients)
      val n = coefficients.size
      if (n == 0) {
        throw NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY)
      }
      var result = coefficients[n - 1]
      for (j in n - 2 downTo 0) {
        result = argument*result + coefficients[j]
      }
      return result
    }

    /**
     * Returns the coefficients of the derivative of the polynomial with the given coefficients.
     *
     * @param coefficients Coefficients of the polynomial to differentiate.
     * @return the coefficients of the derivative or `null` if coefficients has length 1.
     */
    @Throws(NoDataException::class)
    protected fun differentiate(coefficients: MultiArray<Double, D1>): MultiArray<Double, D1> {
      requireNotNull(coefficients)
      val n = coefficients.size
      if (n == 0) {
        throw NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY)
      }
      if (n == 1) {
        return mk.zeros(1)
      }
      val result = mk.zeros<Double>(n - 1)
      for (i in n - 1 downTo 1) {
        result[i - 1] = i*coefficients[i]
      }
      return result
    }

    /**
     * Creates a string representing a coefficient, removing ".0" endings.
     *
     * @param coeff Coefficient.
     * @return a string representation of `coeff`.
     */
    private fun toString(coeff: Double): String {
      val c = coeff.toString()
      if (c.endsWith(".0")) {
        return c.substring(0, c.length - 2)
      } else {
        return c
      }
    }
  }
}
