package de.harich.thilo.factoring.algorithm.trialdivision.baseline;

import de.harich.thilo.factoring.algorithm.trialdivision.TrialDivisionAlgorithm;

/**
 * very basic algorithm, needs no initialization with some (prime) arrays.
 */
public class ScalarTrialDivision implements TrialDivisionAlgorithm {

    public ScalarTrialDivision() {
    }

    public long[] findAllPrimeFactors(long numberToFactorize, int maxPrimeFactor) {
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(numberToFactorize);
        long[] primeFactors = new long[numberBits];
        int factorIndex = 0;
        int primeFactorIndex = 1;
        int primeFactor;
        do {
            primeFactor = getPrimeFactor(primeFactorIndex);
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            // TODO is using a method with primeFactor not the index faster?
            if (hasPrimeFactor(numberToFactorize, primeFactorIndex)) {
                primeFactors[factorIndex++] = primeFactor;
            }
            primeFactorIndex++;
        } while (primeFactor <= maxPrimeFactor);
        primeFactors[factorIndex] = -1;
        return primeFactors;
    }

    public int findSingleFactor(long number, int maxPrimeFactor) {
        for (int factorIndex = 2; factorIndex <= maxPrimeFactor; factorIndex++) {
            if (hasPrimeFactor(number, factorIndex)) return getPrimeFactor(factorIndex);
//            if (factorFound (number, ++factorIndex)) return getFactor(factorIndex);
        }
        return -1;
    }

    public boolean hasPrimeFactor(long number, int primeIndex){
        return numberDivFactor(number, primeIndex) * getPrimeFactor(primeIndex) == number;
    }

    long numberDivFactor(long number, int factorIndex){
        return number / getPrimeFactor(factorIndex);
    }

    @Override
    public long findSingleFactor(long number) {
        return findSingleFactor(number, (int) Math.sqrt(number) + 1);
    }


}

