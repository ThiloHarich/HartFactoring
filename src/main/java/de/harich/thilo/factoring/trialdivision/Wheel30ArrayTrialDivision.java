package de.harich.thilo.factoring.trialdivision;

import de.harich.thilo.factoring.TrialDivisionAlgorithm;

import static de.harich.thilo.math.VectorMath.ROUND_DOUBLE;

/**
 * 30er-Wheel (2 * 3 * 5).
 * This seems to be the best wheel. Maybe du to the fact that
 * we have 8 candidates for residues mod 30 : 1, 7, 11, 13, 17, 19, 23, 29.
 * 8 fits perfectly in the vector pipeline which are also a power of 2.
 * The 210 = (2 * 3 * 5 * 8) wheel has 48 candidates, so do not fit nicely in a
 * vector pipeline.
 * The Offsets (differences) are: 6, 4, 2, 4, 2, 4, 6, 2.
 */
public class Wheel30ArrayTrialDivision implements TrialDivisionAlgorithm {

    // Die Offsets, um von einem Kandidaten zum nächsten zu springen
    // Wir starten bei 7, der nächste ist 11 (+4), dann 13 (+2), etc.
    private static final int[] OFFSETS =
            {4, 2, 4, 2,
             4, 6, 2, 6};

    public Wheel30ArrayTrialDivision() {
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

        int factor = 7;
        int offsetIndex = 0;

        while (factor <= maxPrimeFactorIndex) {
            if (factorFound(number, factor)) {
                primeFactorIndices[factorIndex++] = factor;
            }
            // Sprung zum nächsten Kandidaten
            factor += OFFSETS[offsetIndex];

            // Index im Array rotieren (0-7)
            offsetIndex = (offsetIndex + 1) & 7; // Schneller als % 8
        }

        primeFactorIndices[factorIndex] = -1;
        return primeFactorIndices;
    }
    @Override
    public long findSingleFactor(long number) {
        return findSingleFactor(number, (int) Math.sqrt(number));
    }

    public int findSingleFactor(long number, int maxPrimeFactorIndex) {
        if (number % 2 == 0) return 2;
        if (number % 3 == 0) return 3;
        if (number % 5 == 0) return 5;

        int factor = 7;
        int offsetIndex = 0;

        while (factor <= maxPrimeFactorIndex) {
            if (factorFound(number, factor)) return factor;
            factor += OFFSETS[offsetIndex];
            offsetIndex = (offsetIndex + 1) & 7;
        }
//        for (int offsetIndex = 0; offsetIndex < maxPrimeFactorIndex; offsetIndex++) {
//            if (factorFound(number, factor)) return factor;
//            factor += OFFSETS[offsetIndex & 7];
//        }
        return -1;
    }

    public boolean factorFound(long number, int factor){
        double pInverse = 1.0 / factor;
        long numberDivFactor = (long) ((number * pInverse) + ROUND_DOUBLE);
        return numberDivFactor * factor == number;
    }

}
