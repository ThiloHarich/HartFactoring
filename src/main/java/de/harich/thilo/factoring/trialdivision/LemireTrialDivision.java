package de.harich.thilo.factoring.trialdivision;

import de.harich.thilo.factoring.trialdivision.education.PrimeAvoidCastTrialDivision;

/**
 * Lemire is around 70% faster than ReciprocalTrialDivision on old SSE-2 vectorisation infrastructure.
 * It is around 3 times faster than the classical PrimeArrayRoundTrialDivision.
 * In this implementation we use arrays. Lemire does the calculation completely in long arrays.
 * We do not need to convert double data to long, compared to ReciprocalTrialDivision
 * were we are using double values for multiplying with the reciprocal value to do the division.
 * This might be the main speed advantage.
 */
public class LemireTrialDivision extends PrimeAvoidCastTrialDivision {

    static long[] modularInverse;
    static long[] limits;

    private static void ensureLemireDataExists() {
        if (modularInverse == null || modularInverse.length != primes.length) {
            modularInverse = new long[primes.length];
            limits = new long[primes.length];
            for (int i = 0; i < primes.length; i++) {
                long p = primes[i];
                // Berechne die modulare Inverse für ungerade p (Newton-Verfahren)
                long inverse = modularInverse(p);
                modularInverse[i] = inverse;
                // Limit = (2^64 - 1) / p (Unsigned)
                limits[i] = Long.divideUnsigned(-1L, p);
            }
        }
    }

    private static long modularInverse(long n) {
        long inverse = n; // Initialer Schätzwert
        for (int i = 0; i < 5; i++) { // 5 Iterationen reichen für 64-bit
            inverse *= 2 - n * inverse;
        }
        return inverse;
    }

    public LemireTrialDivision() {
        super(0);
    }
    public LemireTrialDivision(int maxPrimeFactor) {
        super(maxPrimeFactor);
        ensureLemireDataExists();
    }

    // TODO return a array of primes without exponents with a simple loop like findSingleFactor
    public long[] findFactors(long numberToFactorize, int limit) {
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(numberToFactorize);
        long[] primeFactors = new long[numberBits];
        ensurePrimesExist(limit);
        ensureLemireDataExists();
        int factorIndex = 0;
        while (numberToFactorize % 2 == 0) {
            primeFactors[factorIndex++] = 2;
            numberToFactorize /= 2;
        }
        for (int i = 1; i < primes.length; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            if (factorFound(numberToFactorize, i)) {
                primeFactors[factorIndex++] = primes[i];
//                numberToFactorize /= primes[i];
            }
        }

        return primeFactors;
    }

    public int findSingleFactor(long numberToFactorize, int maxPrimeFactor) {
        // Lemire can not handle even numbers
        if (numberToFactorize % 2 == 0) return 2;
        ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();
        // adjusting the loop end to fit in the vectorisation lanes seems not to help
//        int loopEndForVectorisation = primes.length & ~15;
//        for (int i = 1; i < loopEndForVectorisation; i++) {
        for (int i = 1; i < primes.length; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            if (factorFound (numberToFactorize, i))    return primes[i];
            // unrolling seems to not work here
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
        }
        return -1;
    }

    protected boolean factorFound(long numberToFactorize, int i) {
        // 1. Hole vorberechnete Inverse und Limit
        long inv = modularInverse[i];
        long limit = limits[i];

        // 2. Multipliziere number * inverse (Überlauf ist beabsichtigt!)
        long product = numberToFactorize * inv;

        // 3. Wenn das Produkt (unsigned) kleiner oder gleich dem Limit ist,
        // dann ist numberToFactorize restlos durch primes[i] teilbar.
        // the calculation is done completely in long -> might be the main speedup 50%
        return  Long.compareUnsigned(product, limit) <= 0;
    }
}

