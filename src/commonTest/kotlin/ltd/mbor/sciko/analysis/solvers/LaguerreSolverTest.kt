package ltd.mbor.sciko.analysis.solvers

import ltd.mbor.sciko.analysis.PolynomialFunction
import ltd.mbor.sciko.analysis.exception.NoBracketingException
import ltd.mbor.sciko.analysis.exception.NoDataException
import ltd.mbor.sciko.analysis.exception.NumberIsTooLargeException
import ltd.mbor.sciko.analysis.exception.TooManyEvaluationsException
import ltd.mbor.sciko.analysis.util.ComplexFormat
import ltd.mbor.sciko.linalg.FastMath
import ltd.mbor.sciko.linalg.Precision
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDouble
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.junit.Assert
import kotlin.test.Test


/**
 * Test cases for Laguerre solver.
 *
 *
 * Laguerre's method is very efficient in solving polynomials. Test runs
 * show that for a default absolute accuracy of 1E-6, it generally takes
 * less than 5 iterations to find one root, provided solveAll() is not
 * invoked, and 15 to 20 iterations to find all roots for quintic function.
 *
 */
class LaguerreSolverTest {
  /**
   * Test of solver for the linear function.
   */
  @Test
  fun testLinearFunction() {
    val min: Double
    val max: Double
    val expected: Double
    val result: Double
    val tolerance: Double
    // p(x) = 4x - 1
    val coefficients = mk.ndarray(mk[-1.0, 4.0])
    val f: PolynomialFunction = PolynomialFunction(coefficients)
    val solver = LaguerreSolver()
    min = 0.0
    max = 1.0
    expected = 0.25
    tolerance = FastMath.max(
      solver.absoluteAccuracy,
      FastMath.abs(expected*solver.relativeAccuracy)
    )
    result = solver.solve(100, f, min, max)
    Assert.assertEquals(expected, result, tolerance)
  }

  /**
   * Test of solver for the quadratic function.
   */
  @Test
  fun testQuadraticFunction() {
    var min: Double
    var max: Double
    var expected: Double
    var result: Double
    var tolerance: Double
    // p(x) = 2x^2 + 5x - 3 = (x+3)(2x-1)
    val coefficients = mk.ndarray(mk[-3.0, 5.0, 2.0])
    val f: PolynomialFunction = PolynomialFunction(coefficients)
    val solver = LaguerreSolver()
    min = 0.0
    max = 2.0
    expected = 0.5
    tolerance = FastMath.max(
      solver.absoluteAccuracy,
      FastMath.abs(expected*solver.relativeAccuracy)
    )
    result = solver.solve(100, f, min, max)
    Assert.assertEquals(expected, result, tolerance)
    min = -4.0
    max = -1.0
    expected = -3.0
    tolerance = FastMath.max(
      solver.absoluteAccuracy,
      FastMath.abs(expected*solver.relativeAccuracy)
    )
    result = solver.solve(100, f, min, max)
    Assert.assertEquals(expected, result, tolerance)
  }

  /**
   * Test of solver for the quintic function.
   */
  @Test
  fun testQuinticFunction() {
    var min: Double
    var max: Double
    var expected: Double
    var result: Double
    var tolerance: Double
    // p(x) = x^5 - x^4 - 12x^3 + x^2 - x - 12 = (x+1)(x+3)(x-4)(x^2-x+1)
    val coefficients = mk.ndarray(mk[-12.0, -1.0, 1.0, -12.0, -1.0, 1.0])
    val f: PolynomialFunction = PolynomialFunction(coefficients)
    val solver = LaguerreSolver()
    min = -2.0
    max = 2.0
    expected = -1.0
    tolerance = FastMath.max(
      solver.absoluteAccuracy,
      FastMath.abs(expected*solver.relativeAccuracy)
    )
    result = solver.solve(100, f, min, max)
    Assert.assertEquals(expected, result, tolerance)
    min = -5.0
    max = -2.5
    expected = -3.0
    tolerance = FastMath.max(
      solver.absoluteAccuracy,
      FastMath.abs(expected*solver.relativeAccuracy)
    )
    result = solver.solve(100, f, min, max)
    Assert.assertEquals(expected, result, tolerance)
    min = 3.0
    max = 6.0
    expected = 4.0
    tolerance = FastMath.max(
      solver.absoluteAccuracy,
      FastMath.abs(expected*solver.relativeAccuracy)
    )
    result = solver.solve(100, f, min, max)
    Assert.assertEquals(expected, result, tolerance)
  }

  /**
   * Test of solver for the quintic function using
   * [solveAllComplex][LaguerreSolver.solveAllComplex].
   */
  @Test
  fun testQuinticFunction2() {
    // p(x) = x^5 + 4x^3 + x^2 + 4 = (x+1)(x^2-x+1)(x^2+4)
    val coefficients = mk.ndarray(mk[4.0, 0.0, 1.0, 4.0, 0.0, 1.0])
    val solver = LaguerreSolver()
    val result = solver.solveAllComplex(coefficients, 0.0)
    for (expected in arrayOf<ComplexDouble>(
      ComplexDouble(0, -2),
      ComplexDouble(0, 2),
      ComplexDouble(0.5, 0.5*FastMath.sqrt(3.0)),
      ComplexDouble(-1, 0),
      ComplexDouble(0.5, -0.5*FastMath.sqrt(3.0))
    )) {
      val tolerance = FastMath.max(
        solver.absoluteAccuracy,
        FastMath.abs(expected.abs()*solver.relativeAccuracy)
      )
      assertContains(result, expected, tolerance)
    }
  }

  /**
   * Test of parameters for the solver.
   */
  @Test
  fun testParameters() {
    val coefficients = mk.ndarray(mk[-3.0, 5.0, 2.0])
    val f: PolynomialFunction = PolynomialFunction(coefficients)
    val solver = LaguerreSolver()
    try {
      // bad interval
      solver.solve(100, f, 1.0, -1.0)
      Assert.fail("Expecting NumberIsTooLargeException - bad interval")
    } catch (ex: NumberIsTooLargeException) {
      // expected
    }
    try {
      // no bracketing
      solver.solve(100, f, 2.0, 3.0)
      Assert.fail("Expecting NoBracketingException - no bracketing")
    } catch (ex: NoBracketingException) {
      // expected
    }
  }

  @Test(expected = NoDataException::class)
  fun testEmptyCoefficients() {
    val coefficients = mk.zeros<Double>(0)
    val solver = LaguerreSolver()
    solver.solveComplex(coefficients, 0.0)
  }


  @Test
  fun testTooManyEvaluations() {
    val coefficients = mk.ndarray(mk[1.0, 0.0, 0.0, 1.0]) // x^3 + 1 (cube roots of unity)
    val tol = 1e-12
    val solver = LaguerreSolver(tol)
    // No evaluations limit -> solveAllComplex should get all roots
    val expected: Array<ComplexDouble> = arrayOf<ComplexDouble>(
      ComplexDouble(0.5, FastMath.sqrt(3.0)/2),
      ComplexDouble(-1, 0), ComplexDouble(0.5, -FastMath.sqrt(3.0)/2)
    )
    val roots: MultiArray<ComplexDouble, D1> = solver.solveAllComplex(coefficients, 0.0)
    for (expectedRoot in expected) {
      val tolerance = FastMath.max(
        solver.absoluteAccuracy,
        FastMath.abs(expectedRoot.abs()*solver.relativeAccuracy)
      )
      assertContains(roots, expectedRoot, tolerance)
    }
    // Iterations limit too low -> throw TME
    try {
      solver.solveAllComplex(coefficients, 1000.0, 2)
      Assert.fail("Expecting TooManyEvaluationsException")
    } catch (ex: TooManyEvaluationsException) {
      // expected
    }
  }

  /**
   * Fails iff values does not contain a number within epsilon of z.
   *
   * @param msg  message to return with failure
   * @param values complex array to search
   * @param z  value sought
   * @param epsilon  tolerance
   */
  fun assertContains(
    msg: String?,
    values: MultiArray<ComplexDouble, D1>,
    z: ComplexDouble,
    epsilon: Double
  ) {
    for (value in values) {
      if (Precision.equals(value.re, z.re, epsilon) &&
        Precision.equals(value.im, z.im, epsilon)
      ) {
        return
      }
    }
    Assert.fail(msg + " Unable to find " + (ComplexFormat()).format(z))
  }

  /**
   * Fails iff values does not contain a number within epsilon of z.
   *
   * @param values complex array to search
   * @param z  value sought
   * @param epsilon  tolerance
   */
  fun assertContains(
    values: MultiArray<ComplexDouble, D1>,
    z: ComplexDouble,
    epsilon: Double
  ) {
    assertContains(null, values, z, epsilon)
  }
}
