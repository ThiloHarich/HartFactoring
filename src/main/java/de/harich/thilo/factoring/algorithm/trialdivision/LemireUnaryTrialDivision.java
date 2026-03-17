package de.harich.thilo.factoring.algorithm.trialdivision;

import de.harich.thilo.math.MillerRabin;
import de.harich.thilo.math.SmallPrimes;

import java.math.BigInteger;
import java.util.Arrays;

import static de.harich.thilo.factoring.calculator.LemireHartSmoothFactorisationCalculator.*;

/**
 * Is a faster? variant of trial division. We still check if the number is dividable by a factor with the Lemire
 * check. But instead of dividing out the factor and factor^i we just collect the factor index and reduce
 * the size of the number by the log of the factor. So we can approximate if we have factored the number
 * completely. In a second step we multiply together the factors and check if this is already the number
 * to be factorized.
 */
public class LemireUnaryTrialDivision implements TrialDivisionAlgorithm {

    public static final int END_OF_FACTOR_LIST = -1;
    public static final double LOG_2 = Math.log(2.0);
    public static int[] primes = {2};
    private static long[] modularInverse;
    private static long[] limitIfDividable;
    private static double[] primefactorLength;
//    static double[] reciprocals;

    public LemireUnaryTrialDivision() {
        ensurePrimesExist(0);
        ensureLemireDataExists();
    }
    public LemireUnaryTrialDivision(int maxPrimeFactor) {
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

    @Override
    public int[] findPrimefactorIndices(long number, int maxPrimeFactor) {
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();
        ensurePrimeFactorLengthExists();
        return calculatePrimefactorIndices(number, maxPrimeFactorIndex);
    }

    public void ensurePrimeFactorLengthExists() {
        if (primefactorLength == null || primefactorLength.length < primes.length) {
            primefactorLength = new double[primes.length];
            for (int i = 0; i < primes.length; i++) {
                long prime = primes[i];
                primefactorLength[i] = Math.log(prime) * INV_LOG_2;
            }
        }
    }

    @Override
    public long[] findAllPrimeFactors(long number, int maxPrimeFactor) {
        if (MillerRabin.isProbablePrime(number)) {
            return new long[]{number};
        }
        int[] primeFactorIndices = findPrimefactorIndices(number, maxPrimeFactor);
        return Arrays.stream(primeFactorIndices)
                .takeWhile(index -> index != -1) // Nimm alles, bis die -1 kommt
//                .filter(index -> index > 0)      // Nur positive Werte
                .map(index -> getPrimeFactor(index)) // Umwandeln
                .asLongStream().toArray();
    }

    public int[] calculatePrimefactorIndices(long number, int maxPrimeFactorIndex) {
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(number);
        // For a prime p > 2 with prob 1/p^i we forget a factor p^i. We only count a factor p
        // even if we do not count any  size in this case, the values over all numbers is reduced by
        // 1/(p^i-1)/p^i = 1/(1 - 1/p^i).
        // for a given i we have the reduced size < 1/zeta(i) (zeta also considers p=2).
        // the product of all Zeta values is a small constant smaller 3
        // handling p=2 (which gives the biggest term) correctly should reduce the constant even more
        int sizeOfFactorsNotFoundBySieving = 3;
        double factorizedThreshold = numberBits - sizeOfFactorsNotFoundBySieving;
        int[] primeFactorIndices = new int[numberBits];
        int times2DividesNumber = Long.numberOfTrailingZeros(number);
//        number = number >> times2DividesNumber;
        int storeIndex = addFactorIndexFor2(primeFactorIndices, times2DividesNumber);

        if (number == 1) {
            // mark the end and return in case of a power of 2
            primeFactorIndices[storeIndex] = END_OF_FACTOR_LIST;
            return primeFactorIndices;
        }
        double factorSize = times2DividesNumber;
        int checkForLastPrimeIndex = (int) Math.cbrt(number);
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
                long remaining = number * modularInverse[primeFactorIndex];

                // if number divided by factors is a prime itself we can stop early
//                if (primeFactorIndex > checkForLastPrimeIndex && MillerRabin.isProbablePrime(remaining)) {
//                    // This is only a hint, it's safer to let addMultiplePrimeFactors handle it
//                    // but we can already stop the search
//                    return addMultiplePrimeFactors(number, storeIndex, primeFactorIndices);
//                }
            }
        }
        // Threshold not reached, can happen if we have many or big prime Factors multiple times
        return addMultiplePrimeFactors(number, storeIndex, primeFactorIndices);
    }

    private int addFactorIndexFor2(int[] primeFactorIndices, int trailingZeros) {
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
        // happy case number has no prime factors with an exponent > 1 in its prime factorization
        boolean isNumberFactorized = number == productOfFactors;
        if (isNumberFactorized){
            primeFactorIndices[lastStoreIndex] = END_OF_FACTOR_LIST;
            return primeFactorIndices;
        }
        // find the primes with exponent > 1 in its prime factorization
        int numberWithoutSimpleFactors = (int) (number * (1.0 / productOfFactors));
        int[] multiplePrimeFactorIndices = new int[primeFactorIndices.length];
        int storedPrimeFactorIndex = 0;
        int multipleStoreIndex = 0;
        while (numberWithoutSimpleFactors > 1 && storedPrimeFactorIndex < lastStoreIndex) {
            int primeFactorIndex = primeFactorIndices[storedPrimeFactorIndex];
            while (numberWithoutSimpleFactors > 1 && hasPrimeFactor(number, primeFactorIndex)) {
                multiplePrimeFactorIndices[multipleStoreIndex++] = primeFactorIndex;
                double oneDivPrime = 1.0 / getPrimeFactor(primeFactorIndex);
                numberWithoutSimpleFactors = (int) (numberWithoutSimpleFactors * oneDivPrime);
            }
            storedPrimeFactorIndex++;
        }
        // copy the simple factors at the end
        storedPrimeFactorIndex = 0;
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