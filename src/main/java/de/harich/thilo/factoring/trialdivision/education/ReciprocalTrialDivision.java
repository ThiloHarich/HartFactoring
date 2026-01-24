package de.harich.thilo.factoring.trialdivision.education;

/**
 * We try to make use of Vectorisation here as well
 */
public class ReciprocalTrialDivision extends PrimeAvoidCastTrialDivision {

    static double[] reciprocals = {.5};

    public ReciprocalTrialDivision() {
        super(0);
    }
    public ReciprocalTrialDivision(int maxPrimeFactor) {
        super(maxPrimeFactor);
        ensureReciprocalsExist();
    }


    public int findSingleFactor(long numberToFactorize, int maxPrimeFactor) {
        ensurePrimesExist(maxPrimeFactor);
        ensureReciprocalsExist();
        // adjusting the loop end to fit in the vectorisation lanes seems not to help
        int loopEndForVectorisation = primes.length & ~15;
        for (int i = 0; i < loopEndForVectorisation; i++) {
//        for (int i = 0; i < primes.length; i++) {
            // loop unrolling som,times (?) helps here (for better vectorisation!?)
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            if (factorFound (numberToFactorize, i))    return primes[i];
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
        }
        return -1;
    }


    private static void ensureReciprocalsExist() {
        if (reciprocals.length != primes.length) {
            reciprocals = new double[primes.length];
            for (int i = 0; i < primes.length; i++) {
                reciprocals[i] = 1.0 / primes[i];
            }
        }
    }

    public double getReciprocal(int i) {
        return reciprocals[i];
    }

}

