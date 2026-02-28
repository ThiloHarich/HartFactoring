package de.harich.thilo.factoring.algorithm.trialdivision.baseline;

import de.harich.thilo.factoring.algorithm.trialdivision.TrialDivisionAlgorithm;

/**
 * very basic algorithm, needs no initialization with some (prime) arrays.
 */
public class ScalarTrialDivision implements TrialDivisionAlgorithm {

    public ScalarTrialDivision() {
    }
    @Override
    public int [] findPrimefactorIndices(long number, int maxPrimeFactorIndex) {
        return addFactorsFoundIndices(number, maxPrimeFactorIndex);
    }

    protected int[] addFactorsFoundIndices(long numberToFactorize, int maxPrimeFactorIndex) {
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(numberToFactorize);
        int[] primeFactorIndices = new int[numberBits];
        int factorIndex = 0;
        for (int i = 1; i < maxPrimeFactorIndex; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            if (hasPrimeFactor(numberToFactorize, i)) {
                primeFactorIndices[factorIndex++] = i;
            }
        }
        primeFactorIndices[factorIndex] = -1;
        return primeFactorIndices;
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
        return findSingleFactor(number, (int) Math.sqrt(number));
    }


}

