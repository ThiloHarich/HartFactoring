package de.harich.thilo.factoring.algorithm.trialdivision;

import de.harich.thilo.math.SmallPrimes;

import java.util.Arrays;


/**
 * Lemire is around 60% faster than the fastest algorithm based on reciprocal values to determine
 * the dividability of a number (which is ReciprocalArrayTrialDivision)
 * on old SSE-2 vectorisation infrastructure.
 * It is around 3 times faster as if we use reciprocal values and rounding to convert
 * doubles into long values PrimeReciprocalTrialDivision.
 * In this implementation we use arrays. Lemire does the calculation completely in long arrays.
 * We do not need to convert double data to long, compared to ReciprocalTrialDivision
 * were we are using double values for multiplying with the reciprocal value to do the division.
 * This might be the main speed advantage.
 */
public class LemireTrialDivision implements TrialDivisionAlgorithm {

    public static int[] primes = {2};
    private static long[] modularInverse;
    private static long[] limitIfDividable;
//    static double[] reciprocals;

    public LemireTrialDivision() {
        ensurePrimesExist(0);
        ensureLemireDataExists();
    }
    public LemireTrialDivision(int maxPrimeFactor) {
        ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();
    }

    public static int ensurePrimesExist(int maxPrimeFactor) {
        int maxStoredPrime = primes[primes.length - 1];
        if (maxStoredPrime <= maxPrimeFactor) {
            int biggerLimit = 2 * maxPrimeFactor;
            primes = SmallPrimes.generatePrimes(biggerLimit);
        }
        // TODO check if calculating it by maxPrimeFactor / log (maxPrimeFactor) is faster
        return Math.abs(Arrays.binarySearch(primes, maxPrimeFactor));
    }

    protected void ensureLemireDataExists() {
        if (modularInverse == null || modularInverse.length != primes.length) {
            modularInverse = new long[primes.length];
            limitIfDividable = new long[primes.length];
//            reciprocals = new double[primes.length];
            for (int i = 0; i < primes.length; i++) {
                long prime = primes[i];
                // calculate modular Inverse by Newton
                modularInverse[i] = modularInverse(prime);
                // Limit = (2^64 - 1) / prime (Unsigned)
                limitIfDividable[i] = Long.divideUnsigned(-1L, prime);
//                reciprocals[i] = 1.0 / primes[i];
            }
        }
    }

    private static long modularInverse(long n) {
        // initial value
        long inverse = n;
        int iterationsFor64Bit = 5;
        for (int i = 0; i < iterationsFor64Bit; i++) {
            inverse *= 2 - n * inverse;
        }
        return inverse;
    }

    /**
     * Here we have a upper limit of the factors we want to find. This is needed if Trial division is used in
     * combination with other algorithms as a first step to filter out some small prime factors.
     * Since this might not find all prime factors, the result is a new class, which also stores the not found
     * or remaining factor.
     */
    public Decomposition findAllPrimeFactors(long number, int maxPrimeFactor) {
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(number);
        long[] primeFactors = new long[numberBits];
        int trailingZeros = Long.numberOfTrailingZeros(number);
        number = number >> trailingZeros;
        int factorIndex = addFactor2(primeFactors, trailingZeros);
        // if is a power of 2 we can exit directly
        if (number == 1) {
            return new Decomposition(primeFactors, factorIndex);
        }
        for (int primeFactorIndex = 1; primeFactorIndex <= maxPrimeFactorIndex; primeFactorIndex++) {
            if (hasPrimeFactor(number, primeFactorIndex)){
                int primeFactor = getPrimeFactor(primeFactorIndex);
                do {
                    primeFactors[factorIndex++] = primeFactor;
                    number = number / primeFactor;
//                    number = (long) (number * ((double) 1 / primeFactor) + ROUND_DOUBLE);
//                    number = (long) (number * reciprocals[primeFactorIndex] + ROUND_DOUBLE);

                } while ((hasPrimeFactor(number, primeFactorIndex)));
                if (number == 1) {
                    return new Decomposition(primeFactors, factorIndex);
                }
            }
        }
        // store the remaining number as last factor, and mark it
        return new Decomposition(primeFactors, factorIndex, number);
    }

    public static int addFactor2(long[] primeFactorList, int trailingZeros) {
        int index = 0;
        for (; index < trailingZeros; index++) {
            primeFactorList[index] = 2;
        }
        return index;
    }

    @Override
    public long findSingleFactor(long number) {
        return findSingleFactor(number, (int) Math.sqrt(number) + 1);
    }

    public int findSingleFactor(long number, int maxPrimeFactor) {
        // Lemire can not handle even numbers
        if (number % 2 == 0) return 2;
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();
        for (int primeIndex = 1; primeIndex <= maxPrimeFactorIndex; primeIndex++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays

            if (hasPrimeFactor(number, primeIndex))    return getPrimeFactor(primeIndex);
            // unrolling 4 times for Lemire long seems to be optimal
            if (hasPrimeFactor(number, ++primeIndex))  return getPrimeFactor(primeIndex);
            if (hasPrimeFactor(number, ++primeIndex))  return getPrimeFactor(primeIndex);
            if (hasPrimeFactor(number, ++primeIndex))  return getPrimeFactor(primeIndex);
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
        }
        return NO_FACTOR_FOUND;
    }

    @Override
    public int getPrimeFactor(int primeFactorIndex) {
        return primes[primeFactorIndex];
    }

    @Override
    public long[] findAllPrimeFactors(long number) {
        // TODO add a performance Test here.
        int maxPrimeFactor = (int) (Math.sqrt(number) + 1);
        return findAllPrimeFactors(number, maxPrimeFactor).sortedPrimeFactors;

    }

    public boolean hasPrimeFactor(long number, int primeIndex) {
        // multiply number and primeModularInverted (overflow can happen!)
        long product = number * modularInverse[primeIndex];
        // if product (unsigned) is lower than the limit,
        // than the number is dividable by primes[primeIndex].
        // the calculation is done completely in long -> reason for speedup over double remainders
        return Long.compareUnsigned(product, limitIfDividable[primeIndex]) <= 0;
    }
}