package de.harich.thilo.factoring.algorithm.trialdivision;

import de.harich.thilo.math.SmallPrimes;

import java.util.Arrays;

import static de.harich.thilo.factoring.calculator.LemireHartSmoothFactorisationCalculator.addFactor2;

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

    public static final int END_OF_FACTOR_LIST = -1;
    public static final double LOG_2 = Math.log(2.0);
    public static int[] primes = {2};
    private static long[] modularInverse;
    private static long[] limitIfDividable;
    private static double[] primefactorLength;
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

//    @Override
//    public int[] findFactorIndices(long number, int maxPrimeFactor) {
//        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
//        ensureLemireDataExists();
//            return calculateFactorIndices(number, maxPrimeFactorIndex);
//    }

    @Override
    public int[] findPrimefactorIndices(long number, int maxPrimeFactor) {
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();
        calculatePrimeFactorLength();
        return calculateFactorIndicesAndStop(number, maxPrimeFactorIndex);
    }

    private void calculatePrimeFactorLength() {
        primefactorLength = new double[primes.length];
        for (int i = 0; i < primes.length; i++) {
            long prime = primes[i];
            primefactorLength [i] = Math.log(prime) / LOG_2;
        }
    }

    // TODO do sieving, by adding the log of the number to a counter, it threshold is reached
    // multiply the divisors together, reciprocal
    public long[] findAllPrimeFactors(long number, int maxPrimeFactor) {
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(number);
        long[] primeFactors = new long[numberBits];
        int trailingZeros = Long.numberOfTrailingZeros(number);
        number = number >> trailingZeros;
        int factorIndex = addFactor2(primeFactors, trailingZeros);
        // if is a power of 2 we can exit directly
        if (number == 1) {
            markAsFactorized(primeFactors);
            return primeFactors;
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
                    markAsFactorized(primeFactors);
                    return primeFactors;
                }
            }
        }
        // store the remaining number as last factor, and mark it
//        primeFactors[factorIndex[0]] = -number;
        primeFactors[factorIndex] = -number;
        return primeFactors;
    }
    private long divideByFactor(long number, long[] primeFactors, int[] factorIndex, int i){
        int primeFactor = getPrimeFactor(i);
        do {
            primeFactors[factorIndex[0]++] = primeFactor;
            // TODO we might speed this up by using reciprocals
            number = number / primeFactor;
        } while ((hasPrimeFactor(number, i)));
        return number;
    }

    private static void markAsFactorized(long[] primeFactors) {
        primeFactors[0] = - primeFactors[0];
    }

    public int[] calculateFactorIndices(long number, int maxPrimeFactorIndex) {
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(number);
        int[] primeFactorIndices = new int[numberBits];
        int factorIndex = 0;
        for (int i = 1; i < maxPrimeFactorIndex; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            if (hasPrimeFactor(number, i)) primeFactorIndices[factorIndex++] = i;
//            if (factorFound(number, ++i)) primeFactorIndices[factorIndex++] = i;

        }
        primeFactorIndices[factorIndex] = NO_FACTOR_FOUND;
        return primeFactorIndices;
    }

    public int[] calculateFactorIndicesAndStop(long number, int maxPrimeFactorIndex) {
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(number);
        double factorizedThreshold = numberBits - 4;
        int[] primeFactorIndices = new int[numberBits];
        int trailingZeros = Long.numberOfTrailingZeros(number);
//        number = number >> trailingZeros;
        int storeIndex = addFactorIndex2(primeFactorIndices, trailingZeros);


        if (number == 1) {
            // mark the end and return in case of a power of 2
            primeFactorIndices[storeIndex] = END_OF_FACTOR_LIST;
            return primeFactorIndices;
        }
        double factorSize = trailingZeros;
        for (int primeFactorIndex = 1; primeFactorIndex < maxPrimeFactorIndex; primeFactorIndex++) {
            if (hasPrimeFactor(number, primeFactorIndex)) {
                primeFactorIndices[storeIndex++] = primeFactorIndex;
                // no division anymore just adding the size
                // no unpredictable loop for dividing out multiple prime factors
                // speedup for smooth numbers?
                factorSize += primefactorLength[primeFactorIndex];
                if (factorSize >= factorizedThreshold){
                    return addMultiplePrimeFactors(number, storeIndex, primeFactorIndices);
                }
            }
        }
//        primeFactorIndices[storeIndex] = NO_FACTOR_FOUND;
        return primeFactorIndices;
    }

    private int addFactorIndex2(int[] primeFactorIndices, int trailingZeros) {
        int index = 0;
        for (int i = 0; i < trailingZeros; i++) {
            primeFactorIndices[index++] = 0;
        }
        return index;
    }

    private int[] addMultiplePrimeFactors(long number, int lastStoreIndex, int[] primeFactorIndices) {
        long productOfFactors = 1;
        for (int storedPrimeFactorIndex = 0; storedPrimeFactorIndex < lastStoreIndex; storedPrimeFactorIndex++) {
            int primeFactorIndex = primeFactorIndices[storedPrimeFactorIndex];
            productOfFactors *= getPrimeFactor(primeFactorIndex);
        }
        // happy case number has no prime factors with an exponent > 1 in its prime factorisation
        boolean isNumberFactorized = number == productOfFactors;
        if (isNumberFactorized){
            primeFactorIndices[lastStoreIndex] = END_OF_FACTOR_LIST;
            return primeFactorIndices;
        }
        // find the primes with exponent > 1 in its prime factorisation
        int numberWithoutSimpleFactors = (int) (number * (1L / productOfFactors));
        int[] multiplePrimeFactorIndices = new int[primeFactorIndices.length];
        int storedPrimeFactorIndex = 0;
        int multipleStoreIndex = 0;
        while (numberWithoutSimpleFactors > 1 && storedPrimeFactorIndex < lastStoreIndex) {
            do {
                multiplePrimeFactorIndices[multipleStoreIndex++] = primeFactorIndices[storedPrimeFactorIndex];
                numberWithoutSimpleFactors *= (int) (1.0 / getPrimeFactor(storedPrimeFactorIndex));
            } while (numberWithoutSimpleFactors > 1 && hasPrimeFactor(number, storedPrimeFactorIndex));
            storedPrimeFactorIndex++;
        }
        // copy the simple factors at the end
        while (primeFactorIndices[storedPrimeFactorIndex] > 0) {
            multiplePrimeFactorIndices[multipleStoreIndex++] = primeFactorIndices[storedPrimeFactorIndex++];
        }
        multiplePrimeFactorIndices[multipleStoreIndex] = END_OF_FACTOR_LIST;
        return multiplePrimeFactorIndices;
    }

    @Override
    public long findSingleFactor(long number) {
        return findSingleFactor(number, (int) Math.sqrt(number));
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

    public boolean hasPrimeFactor(long number, int primeIndex) {
        // multiply number and primeModularInverted (overflow can happen!)
        long product = number * modularInverse[primeIndex];
        // if product (unsigned) is lower than the limit,
        // than the number is dividable by primes[primeIndex].
        // the calculation is done completely in long -> reason for speedup over double remainders
        boolean isNumberDivideableByPrime = Long.compareUnsigned(product, limitIfDividable[primeIndex]) <= 0;
        return isNumberDivideableByPrime;
    }
}