package de.harich.thilo.factoring.algorithm.trialdivision.education;

import static de.harich.thilo.math.VectorMath.ROUND_DOUBLE;

/**
 * Features:
 * - uses an array to store the primes (already provided by PrimeArrayTrialDivision)
 * - calculates the reciprocal of factor to determine if a number is dividable ba a factor
 * - instead of rounding we add some values to the result which simulates rounding
 *
 *  Using the reciprocal speeds up the algorithm by a factor of 2, when using array/vectorisation.
 *  We avoid Math.round here since it is unsing Double.doubleToRawLongBits which might not be done in a floating point routine
 *  and there is an 'if' which slows down the vectorisation pipeline.
 */
public class PrimeReciprocalTrialDivision extends PrimeArrayTrialDivision{

    public PrimeReciprocalTrialDivision() {
        super();
    }
    public PrimeReciprocalTrialDivision(int maxPrimeFactor) {
        ensurePrimesExist(maxPrimeFactor);
    }

    public double getReciprocal(int i) {
        return 1.0 / primes[i];
    }

    public long numberDivFactor(long numberToFactorize, int factorIndex) {
        // for bigger numbers (> 31 bit?) we need to simulate rounding
        // TODO Math.fma should help, if provided by the cpu
        return (long) (numberToFactorize * getReciprocal(factorIndex) + ROUND_DOUBLE);
//        return VectorMath.round(numberToFactorize * getReciprocal(factorIndex));
        // in timings, we do not see any benefit when removing the additional '+' like below
        //        return (long) (numberToFactorize * getReciprocal(factorIndex));
    }

}

