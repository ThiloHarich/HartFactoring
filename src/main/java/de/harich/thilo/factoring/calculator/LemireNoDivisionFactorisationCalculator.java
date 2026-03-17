package de.harich.thilo.factoring.calculator;

import de.harich.thilo.factoring.algorithm.trialdivision.LemireUnaryTrialDivision;
import de.harich.thilo.math.MillerRabin;

public class LemireNoDivisionFactorisationCalculator implements FactorisationCalculator {

    LemireUnaryTrialDivision factorisationAlgorithm = new LemireUnaryTrialDivision();

    @Override
    public long[] getSortedPrimeFactors(long number) {
        if (MillerRabin.isProbablePrime(number)) {
            return new long[]{number};
        }
        long[] factorsBelowSqrt = factorisationAlgorithm.findAllPrimeFactors(number, (int) Math.sqrt(number));
        return factorsBelowSqrt;
    }
}
