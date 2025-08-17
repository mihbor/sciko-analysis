package ltd.mbor.sciko.analysis.util

import ltd.mbor.sciko.analysis.exception.MathIllegalArgumentException
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats
import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDouble
import org.jetbrains.kotlinx.multik.ndarray.data.Dimension
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.jetbrains.kotlinx.multik.ndarray.operations.map

/**
 * Static implementations of common
 * {@link org.apache.commons.math3.complex.Complex} utilities functions.
 */
object ComplexUtils {
    /**
     * Creates a complex number from the given polar representation.
     *
     * The value returned is `r·e^(i·theta)`,
     * computed as `r·cos(theta) + r·sin(theta)i`
     *
     * If either `r` or `theta` is NaN, or
     * `theta` is infinite, Complex.NaN is returned.
     *
     * If `r` is infinite and `theta` is finite,
     * infinite or NaN values may be returned in parts of the result, following
     * the rules for double arithmetic.
     *
     * Examples:
     * polar2Complex(INFINITY, π/4) = INFINITY + INFINITY i
     * polar2Complex(INFINITY, 0) = INFINITY + NaN i
     * polar2Complex(INFINITY, -π/4) = INFINITY - INFINITY i
     * polar2Complex(INFINITY, 5π/4) = -INFINITY - INFINITY i
     *
     * @param r the modulus of the complex number to create
     * @param theta  the argument of the complex number to create
     * @return `r·e^(i·theta)`
     * @throws MathIllegalArgumentException if `r` is negative.
     */
    @Throws(MathIllegalArgumentException::class)
    fun polar2Complex(r: Double, theta: Double): ComplexDouble {
        if (r < 0) {
            throw MathIllegalArgumentException(LocalizedFormats.NEGATIVE_COMPLEX_MODULE, r)
        }
        return ComplexDouble(r * kotlin.math.cos(theta), r * kotlin.math.sin(theta))
    }

    /**
     * Convert an array of primitive doubles to an array of `Complex` objects.
     *
     * @param real Array of numbers to be converted to their `Complex`
     * equivalent.
     * @return an array of `Complex` objects.
     */
    fun <D: Dimension> convertToComplex(real: MultiArray<Double, D>): MultiArray<ComplexDouble, D> {
        return real.map{ ComplexDouble(it, 0.0) }
    }
}
