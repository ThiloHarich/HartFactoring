package de.harich.thilo.factoring.algorithm.trialdivision.education;

/**
 * Speedup over ScalarTrialDivision is 300%, which is since we have
 * to consider only 2 the cases 1 and 5 mod 6 (2 out of 6 = 1/3)
 */
public class Wheel6TrialDivision extends ScalarTrialDivision {


    public Wheel6TrialDivision() {
    }


    @Override
    public int[] findFactorIndices(long number, int maxPrimeFactorIndex){
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(maxPrimeFactorIndex);
        int[] primeFactorIndices = new int[numberBits];
        int factorIndex = 0;
        if (number <= 3) primeFactorIndices[factorIndex++] = (int) number;
        if (number % 2 == 0) primeFactorIndices[factorIndex++] =  2;
        if (number % 3 == 0) primeFactorIndices[factorIndex++] =  3;

        for (int factor = 5; factor <= maxPrimeFactorIndex; factor += 6) {
            if (factorFound (number, factor)) primeFactorIndices[factorIndex++] = factor;
            if (factorFound (number, factor + 2)) primeFactorIndices[factorIndex++] = factor + 2;
        }
        primeFactorIndices[factorIndex] = -1;
        return primeFactorIndices;
    }
    @Override
    public int findSingleFactor(long number, int maxPrimeFactorIndex) {
        if (number <= 3) return (int) number;
        if (number % 2 == 0) return 2;
        if (number % 3 == 0) return 3;

        for (int factor = 5; factor <= maxPrimeFactorIndex; factor += 6) {
            if (factorFound (number, factor)) return factor;
            if (factorFound (number, factor + 2)) return factor + 2;
        }
        return -1;
    }

}

