package de.harich.thilo.factoring.algorithm.trialdivision;

import de.harich.thilo.factoring.algorithm.FactorisationAlgorithm;

public interface TrialDivisionAlgorithm extends FactorisationAlgorithm {

    /**
     * finds all prime factors dividing the number.
     * A prime factor is added to the list x times if it divides the number x times.
     */
    long[] findAllPrimeFactors(long number);

    boolean hasPrimeFactor(long number, int primeIndex);

    default int getPrimeFactor(int primeFactorIndex){
        return primeFactorIndex;
    }
}
