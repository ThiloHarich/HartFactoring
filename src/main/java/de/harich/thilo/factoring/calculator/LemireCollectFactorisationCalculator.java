package de.harich.thilo.factoring.calculator;

import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;

import java.util.Arrays;

public class LemireCollectFactorisationCalculator implements FactorisationCalculator {

    LemireTrialDivision factorisationAlgorithm = new LemireTrialDivision();

    @Override
    public long[] getSortedPrimeFactors(long number) {
        int[] primeFactorIndices = factorisationAlgorithm.findPrimefactorIndices(number, (int) Math.sqrt(number));
        return Arrays.stream(primeFactorIndices)
                .takeWhile(index -> index != -1) // Nimm alles, bis die -1 kommt
//                .filter(index -> index > 0)      // Nur positive Werte
                .map(index -> factorisationAlgorithm.getPrimeFactor(index)) // Umwandeln
                .asLongStream().toArray();
    }
}
