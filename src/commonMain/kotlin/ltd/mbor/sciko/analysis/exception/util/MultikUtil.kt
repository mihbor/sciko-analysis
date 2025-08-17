package ltd.mbor.sciko.analysis.exception.util

import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDouble
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.jetbrains.kotlinx.multik.ndarray.data.MutableMultiArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun <T> MultiArray<T, D1>.copyInto(destination: MutableMultiArray<T, D1>, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size): MultiArray<T, D1> {
  require(destination.size >= destinationOffset + (endIndex - startIndex))
  for (i in startIndex..<endIndex) {
    destination[destinationOffset + i - startIndex] = this[i]
  }
  return destination
}

fun ComplexDouble.pow(x: Double): ComplexDouble {
  val r = sqrt(this.re*this.re + this.im*this.im)
  val theta = atan2(this.im, this.re)
  val newR = r.pow(x)
  val newTheta = theta * x
  return ComplexDouble(
    re = newR * cos(newTheta),
    im = newR * sin(newTheta)
  )
}

fun sqrt(x: ComplexDouble) = x.pow(0.5)
