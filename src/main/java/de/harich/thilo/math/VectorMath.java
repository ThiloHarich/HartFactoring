package de.harich.thilo.math;

/**
 * If you want to make use of vectorisation in java, first you have to use arrays.
 * More specifically arrays of primitive datatypes like 'int', 'long', 'float' and 'double'.
 * It is possible to switch data types, but in old SSE-2 oder AVX the support is bad,
 * meaning this is slow. So try to calculate in the same datatype.
 * Branches will slow down the execution. More precisely if different branches will
 * be executed in a not predictable way the calculation pipeline has to be rolled back.
 * One way to get around branches is to store different values of the branches in a (small)
 * lookup table.
 * You have to avoid Operations which can not be executed fast within a vectorisation environment.
 * Some operation can be executed fast in a single cpu environment and look pritty
 *
 */
public class VectorMath {

    // The allowed discriminator bit size is d <= 53 - bitLength(N/p), thus d<=23 would be safe
    // for any integer N and p>=2. d=10 is the value that performs best, determined by experiment.
    public static final double ROUND_DOUBLE = 1.0/(1<<10);

    /**
     * A replacement for {@link Math#round(double)} and such returns the closest long to the argument.
     * Handling of special cases (like NaN, negative infinity, ...) is not provided.
     * Simulates a rounding before conversion of doubles to longs.
     * There is no guarantee that this works.
     * Especially in loops over arrays, where JIT can make use of vectorisation (SSE, AVX)
     * this shows a speedup by a factor of 3 (when checking the dividability of number).
     * Math.round is unsing Double.doubleToRawLongBits which might not be done in a floating point routine
     * Additionally there is an 'if' which might slow down the vectorisation pipeline.
     */
    public static long round (double number) {
        return (long) ((number) + ROUND_DOUBLE);
    }
}
