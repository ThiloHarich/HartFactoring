package de.harich.thilo.factoring.algorithm;

/**
 * basic Interface for a factorisation algorithm.
 * Only one method need to find a single factor of a number.
 * The factor not have to be a prime factor.
 */
public interface FactorisationAlgorithm {

    int NO_FACTOR_FOUND = -1;

    long findSingleFactor(long number);

    default String getName(){
        return this.getClass().getSimpleName();

    }
}
