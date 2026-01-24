package de.harich.thilo.factoring.trialdivision.education;

import de.harich.thilo.factoring.FactorisationAlgorithm;
import de.harich.thilo.math.SmallPrimes;

public class PrimeArrayRoundTrialDivision implements FactorisationAlgorithm {

    public static int[] primes = {2};

    // The allowed discriminator bit size is d <= 53 - bitLength(N/p), thus d<=23 would be safe
    // for any integer N and p>=2. d=10 is the value that performs best, determined by experiment.
    static final double DISCRIMINATOR = 1.0/(1<<10);

    static int maxNumberOfVectorLanes = 8;
    public PrimeArrayRoundTrialDivision() {
    }

    public PrimeArrayRoundTrialDivision(int maxPrimeFactor) {
        ensurePrimesExist(maxPrimeFactor);
    }

    public int findSingleFactor(long numberToFactorize, int maxPrimeFactor) {
        ensurePrimesExist(maxPrimeFactor);
        // usually a proper upper limit and unrollig the code helps the vectorisation, but I can not see speedup
        for (int i = 0; i < primes.length; i++) {
            if (factorFound (numberToFactorize, i)){return primes[i];}
//            if (factorFound (numberToFactorize, ++i)){return primes[i];}
//            if (factorFound (numberToFactorize, ++i)){return primes[i];}
//            if (factorFound (numberToFactorize, ++i)){return primes[i];}
        }
        return -1;
    }

    public double getReciprocal(int i) {
        return 1.0/ primes[i];
    }

    public static void ensurePrimesExist(int maxPrimeFactor) {
        int maxStoredPrime = primes[primes.length - 1];
        if (maxStoredPrime < maxPrimeFactor) {
            int biggerLimit = 2 * maxPrimeFactor;
            primes = SmallPrimes.generatePrimes(biggerLimit);
        }
    }

    protected boolean factorFound(long numberToFactorize, int i) {
        int prime = primes[i];
        double pInverse = getReciprocal(i);
        long numberDivPrime = Math.round(numberToFactorize * pInverse);
        // here the 3 times faster code from PrimeDivisionTrialDivisionAlgorithm
//        long numberDivPrime = (long) (numberToFactorize * pInverse + DISCRIMINATOR);
        return (numberDivPrime * prime) == numberToFactorize;
    }

    @Override
    public long findSingleFactor(long numberToFactorize) {
        return findSingleFactor(numberToFactorize, (int) Math.sqrt(numberToFactorize));
    }
}

