package de.harich.thilo.factoring.calculator;

import de.harich.thilo.factoring.algorithm.trialdivision.LemireIntTrialDivision;

public class FactorisationCalculatorLemireInt implements FactorisationCalculator {

    LemireIntTrialDivision factorisationAlgorithm = new LemireIntTrialDivision();

    @Override
    public long[] getSortedPrimeFactors(long number) {
        return factorisationAlgorithm.findAllFactors(number, (int) Math.sqrt(number));
    }

//    @Override
//    public long[][] getSortedPrimeFactors(long[] numbers) {
//        long[][] sortedPrimeFactors = new long[numbers.length][];
//        for (int i = 0; i < numbers.length; i++) {
//            sortedPrimeFactors[i] = getSortedPrimeFactors(numbers[i]);
//        }
//        return sortedPrimeFactors;
//    }
}
