package de.harich.thilo.factoring.trialdivision.education;

import de.harich.thilo.factoring.TrialDivisionAlgorithm;

/**
 * very basic algorithm, needs no initialization with some (prime) arrays.
 */
public class ScalarTrialDivision implements TrialDivisionAlgorithm {

    public ScalarTrialDivision() {
    }
    @Override
    public int [] findFactorIndices(long number, int maxPrimeFactorIndex) {
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
            if (factorFound(numberToFactorize, i)) {
                primeFactorIndices[factorIndex++] = i;
            }
        }
        primeFactorIndices[factorIndex] = -1;
        return primeFactorIndices;
    }

    public int findSingleFactor(long number, int maxPrimeFactor) {
        for (int factorIndex = 2; factorIndex <= maxPrimeFactor; factorIndex++) {
            if (factorFound (number, factorIndex)) return getFactor(factorIndex);
//            if (factorFound (number, ++factorIndex)) return getFactor(factorIndex);
        }
        return -1;
    }

    public boolean factorFound(long number, int factorIndex){
        return numberDivFactor(number, factorIndex) * getFactor(factorIndex) == number;
    }

    long numberDivFactor(long number, int factorIndex){
        return number / getFactor(factorIndex);
    }

    @Override
    public long findSingleFactor(long number) {
        return findSingleFactor(number, (int) Math.sqrt(number));
    }


}

