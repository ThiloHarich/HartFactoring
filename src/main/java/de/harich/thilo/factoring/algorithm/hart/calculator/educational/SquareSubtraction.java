package de.harich.thilo.factoring.algorithm.hart.calculator.educational;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

/**
 * Creates values for the 'fermat' equation x^2 - k*n = y^2.
 * More specific it creates (small) values for x^2 - 4 * n * multiplier(i)
 * This can help to create a square by y^2 and such find a divisor of n.
 * For an (increasing) 'i' getMultiplier(i) return a good multiple of i.
 * Good means that the result has a good chance to be a square, in order to find a divisor gcd(x+y, n) of n.
 * getFirstX returns the minimal x for the equation such that the created value is likely small and can be a square.
 *
 * In the simple form getMultiplier(i) returns just i or multiplier * i, where multiplier might be 3^3 * 5 * 7 = 315.
 */
public class SquareSubtraction {

    // by multiplying a number n with m for x^2 - m*n = y^2 mod m for any x, meaning there are many solutions,
    // but the number is growing
    // the multiplier  3*3*5*7 = 315 increases performance by a factor of 1.7
    private final long multiplier;

    // Only use when CPU supports fusedMultiplyAdd, otherwise Algorithm will slow down by a factor of ~ 500.
    protected boolean useFusedMultipleAdd = false;
    static final double ROUND_UP_DOUBLE = 0.9999999665;

    public SquareSubtraction(int multiplier) {
        this.multiplier = multiplier;
    }


    /**
     * Returns the value
     * ceil( sqrt (4 * n * multiplier * i)), where the parameter sqrt4N is sqrt(4 * n)
     */
    public long getFirstX(double sqrt4N, int i) {
        if (useFusedMultipleAdd)
            // since (long) Math.ceil(sqrt(4N) * sqrt(multiplier * i)) is identical to
            // (long) Math.fma(sqrt(4N), sqrt(multiplier * i), ROUND_UP_DOUBLE)
            // if the CPU supports fusedMultiplyAdd we use it together with a rounding trick
            return (long) Math.fma(sqrt4N, getSqrtMultiplier(i), ROUND_UP_DOUBLE);
        return (long) ceil (sqrt4N * getSqrtMultiplier(i));
    }

    protected double getSqrtMultiplier(int i) {
        return sqrt(multiplier * i);
    }

    /**
     * returns the value index * multiplier
     */
    public long getMultiplier(int index) {
        return (long) index * multiplier;
    }

    /**
     * switches on the usage of Math.fma in getFirstX
     * should be called if the CPU supports fusedMultiplyAdd. This is a feature supported by AVX
     * Might save around 4 % (my current PC does not support it, so I can not verify at the moment)
     */
    public void useFusedMultipleAdd(){
        useFusedMultipleAdd = true;
    }


    public void handleSolution(long greatestCommonDivisor, int multiplierIndex, long numberToFactorize, long x, long multiplier) {
    }

    public void initialize() {
    }

    public String getName() {
        return this.getClass().getSimpleName() + " " + multiplier;
    }

}
