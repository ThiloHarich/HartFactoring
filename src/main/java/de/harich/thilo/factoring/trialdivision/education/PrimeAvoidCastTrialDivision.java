package de.harich.thilo.factoring.trialdivision.education;

public class PrimeAvoidCastTrialDivision extends PrimeArrayRoundTrialDivision {


    public PrimeAvoidCastTrialDivision() {
        super();
    }
    public PrimeAvoidCastTrialDivision(int maxPrimeFactor) {
        super(maxPrimeFactor);
    }


    protected boolean factorFound(long numberToFactorize, int i) {
        int prime = primes[i];
        double pInverse = getReciprocal(i);
        // in contrast to rounding and converting to long, we try to skip the conversion to long, and just
        // add a value. just converting to long gives wrong values
//        long numberDivPrime = (long) ((numberToFactorize * pInverse) + DISCRIMINATOR);
        double numberDivPrime = (numberToFactorize * pInverse) + DISCRIMINATOR;

        // type conversions is a bottleneck in vectorized calculations on old SSE or AVX architectures
        // it looks like a double number multiplied by a int value can be compared with a long
        // without expensive type conversion
        // this little change makes the algorithm 3 times !!!  faster on old SSE-2 architecture
        return ((long) numberDivPrime * prime) == numberToFactorize;
    }

}

