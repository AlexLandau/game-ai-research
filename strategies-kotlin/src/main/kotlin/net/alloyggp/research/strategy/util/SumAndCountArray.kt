package net.alloyggp.research.strategy.util

class SumAndCountArray(private val sums: DoubleArray, private var count: Long) {
    companion object {
        fun create(size: Int): SumAndCountArray {
            val sums = DoubleArray(size, { _ -> 0.0 })
            return SumAndCountArray(sums, 0L)
        }
    }

    fun getAverage(roleIndex: Int): Double {
        val sum = sums[roleIndex]
        if (count == 0L) {
            // TODO: Consider making a parameter?
            return -1.0
        }
        return sum / count
    }

    fun getCount(): Long {
        return count
    }

    fun addValues(outcomes: List<Double>) {
        if (outcomes.size != sums.size) {
            error("These should be the same size")
        }
        outcomes.forEachIndexed { index, value ->
            sums[index] += value
        }
        count += 1L
    }

    override fun toString(): String {
        return "$sums/$count"
    }
}
