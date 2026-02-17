package de.harich.thilo.factoring.calculator;

import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;

public class LemireFactorisationCalculator implements FactorisationCalculator {

    LemireTrialDivision factorisationAlgorithm = new LemireTrialDivision();

    @Override
    public long[] getSortedPrimeFactors(long number) {
        long[] factorsBelowSqrt = factorisationAlgorithm.findAllFactors(number, (int) Math.sqrt(number));
        return factorsBelowSqrt;

    }
}
