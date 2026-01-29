package de.harich.thilo.factoring.trialdivision.education;

/**
 * 210er-Wheel (2 * 3 * 5 * 7)
 * is slower than the 30 wheel
 */
public class Wheel210ArrayTrialDivision extends ScalarTrialDivision {

    // Offsets für die 48 Kandidaten mod 210
    // Der erste Kandidat nach der 7 ist die 11.
    private static final int[] OFFSETS = {
            2, 4, 2, 4, 6, 2, 6, 4,
            2, 4, 6, 6, 2, 6, 4, 2,
            6, 4, 6, 8, 4, 2, 4, 2,
            4, 8, 6, 4, 6, 2, 4, 6,


            2, 6, 6, 4, 2, 4, 6, 2,
            6, 4, 2, 4, 2, 10, 2, 10
    };

    public Wheel210ArrayTrialDivision() {
    }

    @Override
    public int[] findFactors(long number, int maxPrimeFactorIndex) {
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(maxPrimeFactorIndex);
        int[] primeFactorIndices = new int[numberBits];
        int factorIndex = 0;

        // Basis-Primzahlen prüfen
        if (number % 2 == 0) primeFactorIndices[factorIndex++] = 2;
        if (number % 3 == 0) primeFactorIndices[factorIndex++] = 3;
        if (number % 5 == 0) primeFactorIndices[factorIndex++] = 5;
        if (number % 7 == 0) primeFactorIndices[factorIndex++] = 7;

        int factor = 11; // Erster Kandidat nach der 7
        int offsetIndex = 0;

        while (factor <= maxPrimeFactorIndex) {
            if (factorFound(number, factor)) {
                primeFactorIndices[factorIndex++] = factor;
            }
            factor += OFFSETS[offsetIndex];
            offsetIndex = (offsetIndex + 1) % 48;
        }

        primeFactorIndices[factorIndex] = -1;
        return primeFactorIndices;
    }

    @Override
    public int findSingleFactor(long number, int maxPrimeFactorIndex) {
        if (number % 2 == 0) return 2;
        if (number % 3 == 0) return 3;
        if (number % 5 == 0) return 5;
        if (number % 7 == 0) return 7;

        int factor = 11;

        while (factor <= maxPrimeFactorIndex) {
            for (int offsetIndex = 0; offsetIndex < 48; offsetIndex++) {
                if (factorFound(number, factor)) return factor;
                factor += OFFSETS[offsetIndex];
            }
        }

        return -1;
    }
}