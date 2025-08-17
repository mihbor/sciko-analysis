package ltd.mbor.sciko.analysis.util

import ltd.mbor.sciko.analysis.exception.MaxCountExceededException
import ltd.mbor.sciko.analysis.exception.NotStrictlyPositiveException
import ltd.mbor.sciko.analysis.exception.util.LocalizedFormats
import ltd.mbor.sciko.linalg.MathUnsupportedOperationException


/**
 * Provides a sequence of integers.
 *
 * @since 3.6
 */
object IntegerSequence {
  /**
   * Creates a sequence `[start .. end]`.
   * It calls [range(start, end, 1)][.range].
   *
   * @param start First value of the range.
   * @param end Last value of the range.
   * @return a range.
   */
  fun range(
    start: Int,
    end: Int
  ): Range {
    return range(start, end, 1)
  }

  /**
   * Creates a sequence \( a_i, i < 0 <= n \)
   * where \( a_i = start + i * step \)
   * and \( n \) is such that \( a_n <= max \) and \( a_{n+1} > max \).
   *
   * @param start First value of the range.
   * @param max Last value of the range that satisfies the above
   * construction rule.
   * @param step Increment.
   * @return a range.
   */
  fun range(
    start: Int,
    max: Int,
    step: Int
  ): Range {
    return Range(start, max, step)
  }

  /**
   * Generates a sequence of integers.
   */
  class Range(
    /** First value.  */
    private val start: Int,
    /** Final value.  */
    private val max: Int,
    /** Increment.  */
    private val step: Int
  ) : Iterable<Int?> {
    /** Number of integers contained in this range.  */
    private val size: Int

    /**
     * Creates a sequence \( a_i, i < 0 <= n \)
     * where \( a_i = start + i * step \)
     * and \( n \) is such that \( a_n <= max \) and \( a_{n+1} > max \).
     *
     * @param start First value of the range.
     * @param max Last value of the range that satisfies the above
     * construction rule.
     * @param step Increment.
     */
    init {
      val s = (max - start)/step + 1
      this.size = if (s < 0) 0 else s
    }

    /**
     * Gets the number of elements contained in the range.
     *
     * @return the size of the range.
     */
    fun size(): Int {
      return size
    }

    /** {@inheritDoc}  */
    override fun iterator(): MutableIterator<Int?> {
      return Incrementor.Companion.create()
        .withStart(start)
        .withMaximalCount(max + (if (step > 0) 1 else -1))
        .withIncrement(step)
    }
  }

  /**
   * Utility that increments a counter until a maximum is reached, at
   * which point, the instance will by default throw a
   * [MaxCountExceededException].
   * However, the user is able to override this behaviour by defining a
   * custom [callback][MaxCountExceededCallback], in order to e.g.
   * select which exception must be thrown.
   */
  class Incrementor private constructor(
    start: Int,
    max: Int,
    step: Int,
    cb: MaxCountExceededCallback
  ) : MutableIterator<Int?> {
    /** Initial value the counter.  */
    private val init: Int
    /**
     * Gets the upper limit of the counter.
     *
     * @return the counter upper limit.
     */
    /** Upper limit for the counter.  */
    val maximalCount: Int

    /** Increment.  */
    private val increment: Int

    /** Function called at counter exhaustion.  */
    private val maxCountCallback: MaxCountExceededCallback
    /**
     * Gets the current count.
     *
     * @return the current count.
     */
    /** Current count.  */
    var count: Int = 0
      private set

    /**
     * Defines a method to be called at counter exhaustion.
     * The [trigger][.trigger] method should usually throw an exception.
     */
    interface MaxCountExceededCallback {
      /**
       * Function called when the maximal count has been reached.
       *
       * @param maximalCount Maximal count.
       * @throws MaxCountExceededException at counter exhaustion
       */
      @Throws(MaxCountExceededException::class)
      fun trigger(maximalCount: Int)
    }

    /**
     * Creates an incrementor.
     * The counter will be exhausted either when `max` is reached
     * or when `nTimes` increments have been performed.
     *
     * @param start Initial value.
     * @param max Maximal count.
     * @param step Increment.
     * @param cb Function to be called when the maximal count has been reached.
     */
    init {
      this.init = start
      this.maximalCount = max
      this.increment = step
      this.maxCountCallback = cb
      this.count = start
    }

    /**
     * Creates a new instance with a given initial value.
     * The counter is reset to the initial value.
     *
     * @param start Initial value of the counter.
     * @return a new instance.
     */
    fun withStart(start: Int): Incrementor {
      return Incrementor(
        start,
        this.maximalCount,
        this.increment,
        this.maxCountCallback
      )
    }

    /**
     * Creates a new instance with a given maximal count.
     * The counter is reset to the initial value.
     *
     * @param max Maximal count.
     * @return a new instance.
     */
    fun withMaximalCount(max: Int): Incrementor {
      return Incrementor(
        this.init,
        max,
        this.increment,
        this.maxCountCallback
      )
    }

    /**
     * Creates a new instance with a given increment.
     * The counter is reset to the initial value.
     *
     * @param step Increment.
     * @return a new instance.
     */
    fun withIncrement(step: Int): Incrementor {
      if (step == 0) {
        throw IllegalArgumentException(LocalizedFormats.ZERO_NOT_ALLOWED.sourceString)
      }
      return Incrementor(
        this.init,
        this.maximalCount,
        step,
        this.maxCountCallback
      )
    }

    /**
     * Creates a new instance with a given callback.
     * The counter is reset to the initial value.
     *
     * @param cb Callback to be called at counter exhaustion.
     * @return a new instance.
     */
    fun withCallback(cb: MaxCountExceededCallback): Incrementor {
      return Incrementor(
        this.init,
        this.maximalCount,
        this.increment,
        cb
      )
    }
    /**
     * Checks whether incrementing the counter several times is allowed.
     *
     * @param nTimes Number of increments.
     * @return `false` if calling [ increment(nTimes)][.increment] would call the [callback][MaxCountExceededCallback]
     * `true` otherwise.
     */
    /**
     * Checks whether incrementing the counter `nTimes` is allowed.
     *
     * @return `false` if calling [.increment]
     * will trigger a `MaxCountExceededException`,
     * `true` otherwise.
     */
    @JvmOverloads
    fun canIncrement(nTimes: Int = 1): Boolean {
      val finalCount = count + nTimes*increment
      return if (increment < 0) finalCount > maximalCount else finalCount < maximalCount
    }
    /**
     * Performs multiple increments.
     *
     * @param nTimes Number of increments.
     * @throws MaxCountExceededException at counter exhaustion.
     * @throws NotStrictlyPositiveException if `nTimes <= 0`.
     *
     * @see .increment
     */
    /**
     * Adds the increment value to the current iteration count.
     * At counter exhaustion, this method will call the
     * [trigger][MaxCountExceededCallback.trigger] method of the
     * callback object passed to the
     * [.withCallback] method.
     * If not explicitly set, a default callback is used that will throw
     * a `MaxCountExceededException`.
     *
     * @throws MaxCountExceededException at counter exhaustion, unless a
     * custom [callback][MaxCountExceededCallback] has been set.
     *
     * @see .increment
     */
    @JvmOverloads
    @Throws(MaxCountExceededException::class)
    fun increment(nTimes: Int = 1) {
      if (nTimes <= 0) {
        throw NotStrictlyPositiveException(nTimes)
      }
      if (!canIncrement(0)) {
        maxCountCallback.trigger(maximalCount)
      }
      count += nTimes*increment
    }

    /** {@inheritDoc}  */
    override fun hasNext(): Boolean {
      return canIncrement(0)
    }

    /** {@inheritDoc}  */
    override fun next(): Int {
      val value = count
      increment()
      return value
    }

    /**
     * Not applicable.
     *
     * @throws MathUnsupportedOperationException
     */
    override fun remove() {
      throw MathUnsupportedOperationException()
    }

    companion object {
      /** Default callback.  */
      private val CALLBACK
        : MaxCountExceededCallback = object : MaxCountExceededCallback {
        /** {@inheritDoc}  */
        @Throws(MaxCountExceededException::class)
        override fun trigger(max: Int) {
          throw MaxCountExceededException(max)
        }
      }

      /**
       * Factory method that creates a default instance.
       * The initial and maximal values are set to 0.
       * For the new instance to be useful, the maximal count must be set
       * by calling [withMaximalCount][.withMaximalCount].
       *
       * @return an new instance.
       */
      fun create(): Incrementor {
        return Incrementor(0, 0, 1, CALLBACK)
      }
    }
  }
}
