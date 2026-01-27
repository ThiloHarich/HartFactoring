package de.harich.thilo.factoring;

public interface TrialDivisionAlgorithm extends FactorisationAlgorithm {

    int[] findFactors(long number, int maxPrimeFactorIndex);

    boolean factorFound(long number, int factorIndex);

    default int getFactor(int factorIndex){
        return factorIndex;
    }
}
