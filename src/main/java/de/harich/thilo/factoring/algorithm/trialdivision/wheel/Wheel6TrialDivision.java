package de.harich.thilo.factoring.algorithm.trialdivision.wheel;

import de.harich.thilo.factoring.algorithm.trialdivision.baseline.ScalarTrialDivision;

/**
 * Speedup over ScalarTrialDivision is 300%, which is since we have
 * to consider only 2 the cases 1 and 5 mod 6 (2 out of 6 = 1/3)
 */
public class Wheel6TrialDivision extends ScalarTrialDivision {


    public Wheel6TrialDivision() {
    }


    @Override
    public long[] findAllPrimeFactors(long number, int maxPrimeFactorIndex){
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(maxPrimeFactorIndex);
        long[] primeFactors = new long[numberBits];
        int factorIndex = 0;
        if (number <= 3) primeFactors[factorIndex++] = (int) number;
        if (number % 2 == 0) primeFactors[factorIndex++] =  2;
        if (number % 3 == 0) primeFactors[factorIndex++] =  3;

        for (int factor = 5; factor <= maxPrimeFactorIndex; factor += 6) {
            if (hasPrimeFactor(number, factor)) primeFactors[factorIndex++] = factor;
            if (hasPrimeFactor(number, factor + 2)) primeFactors[factorIndex++] = factor + 2;
        }
        primeFactors[factorIndex] = -1;
        return primeFactors;
    }
    @Override
    public int findSingleFactor(long number, int maxPrimeFactorIndex) {
        if (number <= 3) return (int) number;
        if (number % 2 == 0) return 2;
        if (number % 3 == 0) return 3;

        for (int factor = 5; factor <= maxPrimeFactorIndex; factor += 6) {
            if (hasPrimeFactor(number, factor)) return factor;
            if (hasPrimeFactor(number, factor + 2)) return factor + 2;
        }
        return -1;
    }

}

