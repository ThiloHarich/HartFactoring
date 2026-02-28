package de.harich.thilo.factoring.algorithm.trialdivision.prototype.array;

import de.harich.thilo.factoring.algorithm.trialdivision.baseline.ScalarTrialDivision;
import de.harich.thilo.math.SmallPrimes;

import java.util.Arrays;

/**
 * We do not iterate over all number anymore. We precalculate the primes, but still test dividability
 * by a real division, of the number to be checked by the prime factor.
 * So this is the baseline algorithm for using a array filled with primes.
 * It uses
 * - an array to store the primes
 * - divides the number by a factor to determine if a number is dividable ba a factor
 */
public class PrimeArrayTrialDivision extends ScalarTrialDivision {

    protected static int[] primes = {2};

    public PrimeArrayTrialDivision() {
        super();
    }

    public PrimeArrayTrialDivision(int maxPrimeFactor) {
        ensurePrimesExist(maxPrimeFactor);
    }
    @Override
    public int[] findPrimefactorIndices(long number, int maxPrimeFactor){
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        return super.findPrimefactorIndices(number, maxPrimeFactorIndex);
    }

    public int findSingleFactor(long number, int maxPrimeFactor) {
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        // usually a proper upper limit and unrollig the code helps the vectorisation, but I can not see speedup
        for (int primeIndex = 0; primeIndex < maxPrimeFactorIndex; primeIndex++) {
            if (hasPrimeFactor(number, primeIndex)) return getPrimeFactor(primeIndex);
//            if (factorFound (number, ++primeIndex)){return getFactor(primeIndex);}
        }
        return -1;
    }

    public static int ensurePrimesExist(int maxPrimeFactor) {
        int maxStoredPrime = primes[primes.length - 1];
        if (maxStoredPrime < maxPrimeFactor) {
            int biggerLimit = 2 * maxPrimeFactor;
            primes = SmallPrimes.generatePrimes(biggerLimit);
        }
        // TODO check if calculating it by maxPrimeFactor / log (maxPrimeFactor) is faster
        return Math.abs(Arrays.binarySearch(primes, maxPrimeFactor))+1;
    }

    @Override
    public int getPrimeFactor(int primeFactorIndex) {
        return primes[primeFactorIndex];
    }
}

