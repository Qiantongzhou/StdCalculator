import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsTest {

    private static final double EPS = 1e-9; // numeric tolerance

    // --- Helpers -------------------------------------------------------------
    private static double stdDevPop(double[] xs) {

        double mu = Statistics.mean(xs);
        double var = Statistics.variancePopulation(xs, mu);
        return Statistics.sqrt(var);
    }

    // --- Mean ---------------------------------------------------------------
    @Test @DisplayName("mean: classic dataset")
    void mean_classic() {
        double[] xs = {2,4,4,4,5,5,7,9}; // μ = 5
        assertEquals(5.0, Statistics.mean(xs), EPS);
    }

    @Test @DisplayName("mean: single element equals itself")
    void mean_single() {
        double[] xs = {42};
        assertEquals(42.0, Statistics.mean(xs), EPS);
    }

    @Test @DisplayName("mean: empty array throws")
    void mean_empty_throws() {
        assertThrows(IllegalArgumentException.class, () -> Statistics.mean(new double[]{}));
    }

    // --- Variance (population) ----------------------------------------------
    @Test @DisplayName("variance: classic dataset (population)")
    void variance_classic_population() {
        double[] xs = {2,4,4,4,5,5,7,9}; // σ²(pop) = 4
        double mu = Statistics.mean(xs);
        assertEquals(4.0, Statistics.variancePopulation(xs, mu), EPS);
    }

    @Test @DisplayName("variance: single element is zero")
    void variance_single_zero() {
        double[] xs = {123};
        double mu = Statistics.mean(xs);
        assertEquals(0.0, Statistics.variancePopulation(xs, mu), EPS);
    }

    @Test @DisplayName("variance: non-negativity")
    void variance_non_negative() {
        double[] xs = {-3, -1, 0, 1, 2, 10};
        double mu = Statistics.mean(xs);
        assertTrue(Statistics.variancePopulation(xs, mu) >= -1e-12, "variance should be ≥ 0 (within tolerance)");
    }

    // --- sqrt (Newton–Raphson) ----------------------------------------------
    @ParameterizedTest(name = "sqrt({0}) ≈ {1}")
    @CsvSource({
            "0, 0",
            "1, 1",
            "2, 1.4142135623730951",
            "3, 1.7320508075688772",
            "10, 3.1622776601683795",
            "1e-6, 0.001",
            "123456789, 11111.111060555555"
    })
    void sqrt_accuracy(double input, double expected) {
        assertEquals(expected, Statistics.sqrt(input), 1e-9);
    }

    @Test @DisplayName("sqrt: negative input throws")
    void sqrt_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> Statistics.sqrt(-1));
    }

    // --- End-to-end σ (population) ------------------------------------------
    @Test @DisplayName("std dev: classic dataset equals 2")
    void stddev_classic_equals_2() {
        double[] xs = {2,4,4,4,5,5,7,9}; // σ(pop) = 2
        assertEquals(2.0, stdDevPop(xs), EPS);
    }

    @Test @DisplayName("std dev: all equal values is 0")
    void stddev_all_equal_zero() {
        double[] xs = {-3, -3, -3, -3};
        assertEquals(0.0, stdDevPop(xs), EPS);
    }

    @Test @DisplayName("std dev: handles large magnitudes without NaN/Inf")
    void stddev_large_numbers() {
        double[] xs = {1_000_000_000d, 1_000_000_001d, 1_000_000_002d};
        double s = stdDevPop(xs);
        assertFalse(Double.isNaN(s) || Double.isInfinite(s), "std dev should be a finite number");
    }

    // --- Contract checks -----------------------------------------------------
    @Nested @DisplayName("API contracts")
    class Contracts {
        @Test @DisplayName("empty array throws consistently across methods")
        void empty_array_contract() {
            double[] empty = {};
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> Statistics.mean(empty)),
                    () -> assertThrows(IllegalArgumentException.class, () -> Statistics.variancePopulation(empty, 0))
            );
        }
    }
}
