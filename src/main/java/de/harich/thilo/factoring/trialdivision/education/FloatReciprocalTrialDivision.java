package de.harich.thilo.factoring.trialdivision.education;

/**
 * We try to make use of Vectorisation here as well
 */
public class FloatReciprocalTrialDivision extends PrimeAvoidCastTrialDivision {

    static float[] reciprocals = {.5F};

    // The allowed discriminator bit size is d <= 53 - bitLength(N/p), thus d<=23 would be safe
    // for any integer N and p>=2. d=10 is the value that performs best, determined by experiment.
    private static final float DISCRIMINATOR = 1.0F/(1<<10);

    public FloatReciprocalTrialDivision() {
        super(0);
    }
    public FloatReciprocalTrialDivision(int maxPrimeFactor) {
        super(maxPrimeFactor);
        ensureReciprocalsExist();
    }


    public int findSingleFactor(long numberToFactorize, int maxPrimeFactor) {
        ensurePrimesExist(maxPrimeFactor);
        ensureReciprocalsExist();
        // adjusting the loop end to fit in the vectorisation lanes seems not to help
//        int loopEndForVectorisation = primes.length & ~15;
        for (int i = 0; i < primes.length; i++) {
            // loop unrolling is helping here for better vectorisation
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            if (factorFound (numberToFactorize, i))    return primes[i];
            if (factorFound (numberToFactorize, ++i))  return primes[i];
            if (factorFound (numberToFactorize, ++i))  return primes[i];
            if (factorFound (numberToFactorize, ++i))  return primes[i];
        }
        return -1;
    }

    private static void ensureReciprocalsExist() {
        if (reciprocals.length != primes.length) {
            reciprocals = new float[primes.length];
            for (int i = 0; i < primes.length; i++) {
                reciprocals[i] = 1.0F / primes[i];
            }
        }
    }

    public float getReciprocalFloat(int i) {
        return reciprocals[i];
    }
    protected boolean factorFound(long numberToFactorize, int i) {
        int prime = primes[i];
        float pInverse = getReciprocalFloat(i);
        // in contrast to rounding and converting to long, we try to skip the conversion to long, and just
        // add a value. just converting to long gives wrong values
//        long numberDivPrime = (long) ((numberToFactorize * pInverse) + DISCRIMINATOR);
        float numberDivPrime = (numberToFactorize * pInverse) + DISCRIMINATOR;

        // type conversions is a bottleneck in vectorized calculations on old SSE or AVX architectures
        // it looks like a double number multiplied by a int value can be compared with a long
        // without expensive type conversion
        // this little change makes the algorithm 3 times !!!  faster on old SSE-2 architecture
        return ((long) numberDivPrime * prime) == numberToFactorize;
    }

}

