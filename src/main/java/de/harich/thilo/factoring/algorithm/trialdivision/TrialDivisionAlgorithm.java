package de.harich.thilo.factoring.algorithm.trialdivision;

import de.harich.thilo.factoring.algorithm.FactorisationAlgorithm;

public interface TrialDivisionAlgorithm extends FactorisationAlgorithm {


    /**
     * finds all indices of (prime) factors dividing the number.
     * For performance reasons a factor is added to the list only once, even if it divides the number more
     * than once.
     * Should be used, when the number might have a prime factor bigger than getFactor(maxPrimeFactorIndex)
     */
    int[] findPrimefactorIndices(long number, int maxPrimeFactorIndex);

//    /**
//     * finds all prime factors dividing the number.
//     * A prime factor is added to the list x times if it divides the number x times.
//     * Should be used, when all prime factors are lower than getFactor(maxPrimeFactorIndex)
//     */
//    long[] findAllFactors(long number, int maxPrimeFactorIndex);

    boolean hasPrimeFactor(long number, int primeIndex);

    default int getPrimeFactor(int primeFactorIndex){
        return primeFactorIndex;
    }
}
