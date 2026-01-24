package de.harich.thilo.factoring.trialdivision.education;

import de.harich.thilo.factoring.FactorisationAlgorithm;

/**
 * very basic algorithm, needs no initialization with some (prime) arrays.
 */
public class ScalarTrialDivision implements FactorisationAlgorithm {


    public ScalarTrialDivision() {
    }

    public int findSingleFactor(long numberToFactorize, int maxPrimeFactor) {
        for (int i = 2; i <= maxPrimeFactor; i++) {
            if (factorFound (numberToFactorize, i)){
                return i;
            }
        }
        return -1;
    }

    protected boolean factorFound(long number, int index) {
        long numberDivPrime = number / index;
        return numberDivPrime * index == number;
//        return number % index == 0;
    }

    @Override
    public long findSingleFactor(long numberToFactorize) {
        return findSingleFactor(numberToFactorize, (int) Math.sqrt(numberToFactorize));
    }

}

