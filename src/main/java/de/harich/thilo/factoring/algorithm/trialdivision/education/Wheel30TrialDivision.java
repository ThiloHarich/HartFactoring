package de.harich.thilo.factoring.algorithm.trialdivision.education;

/**
 * Speedup over ScalarTrialDivision is 375%, which is since we have
 * to consider only 8 cases out of 30 (8/30 = 26.67% vs 100%)
 * Speedup over 6er-Wheel: We check 8/30 (0.266) instead of 2/6 (0.333) numbers.
 */
public class Wheel30TrialDivision extends ScalarTrialDivision {

    public Wheel30TrialDivision() {
    }

    @Override
    public int[] findFactorIndices(long number, int maxPrimeFactorIndex){
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(maxPrimeFactorIndex);
        int[] primeFactorIndices = new int[numberBits];
        int factorIndex = 0;

        // Basis-Primzahlen des Wheels (2, 3, 5) separat prüfen
        if (number % 2 == 0) primeFactorIndices[factorIndex++] = 2;
        if (number % 3 == 0) primeFactorIndices[factorIndex++] = 3;
        if (number % 5 == 0) primeFactorIndices[factorIndex++] = 5;

        // Wir starten bei 7 (der erste Kandidat nach der 1 im ersten 30er Block)
        // Die Abstände (Increments) zwischen den 8 Kandidaten sind:
        // 4, 2, 4, 2, 4, 6, 2, 6 (Summe = 30)
        for (int factor = 7; factor <= maxPrimeFactorIndex; ) {
            if (factorFound(number, factor)) primeFactorIndices[factorIndex++] = factor; // 7
            factor += 4;
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) primeFactorIndices[factorIndex++] = factor; // 11
            factor += 2;
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) primeFactorIndices[factorIndex++] = factor; // 13
            factor += 4;
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) primeFactorIndices[factorIndex++] = factor; // 17
            factor += 2;
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) primeFactorIndices[factorIndex++] = factor; // 19
            factor += 4;
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) primeFactorIndices[factorIndex++] = factor; // 23
            factor += 6;
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) primeFactorIndices[factorIndex++] = factor; // 29
            factor += 2;
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) primeFactorIndices[factorIndex++] = factor; // 31 (Start nächster Block)
            factor += 6;
        }

        primeFactorIndices[factorIndex] = -1;
        return primeFactorIndices;
    }

    @Override
    public int findSingleFactor(long number, int maxPrimeFactorIndex) {
        if (number % 2 == 0) return 2;
        if (number % 3 == 0) return 3;
        if (number % 5 == 0) return 5;

        for (int factor = 7; factor <= maxPrimeFactorIndex; ) {
            if (factorFound(number, factor)) return factor;
            factor += 4; // 11
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) return factor;
            factor += 2; // 13
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) return factor;
            factor += 4; // 17
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) return factor;
            factor += 2; // 19
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) return factor;
            factor += 4; // 23
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) return factor;
            factor += 6; // 29
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) return factor;
            factor += 2; // 31
            if (factor <= maxPrimeFactorIndex && factorFound(number, factor)) return factor;
            factor += 6; // Nächster Zyklus
        }
        return -1;
    }
    public long numberDivFactor(long numberToFactorize, int factorIndex) {
        double pInverse = 1.0 / factorIndex;
//        return (long) ((numberToFactorize * pInverse) + ROUND_DOUBLE);
        return (long) (numberToFactorize * pInverse);
    }
}
