package de.harich.thilo.factoring;

/**
 * basic Interface for a factorisation algorithm.
 * Only one method need to find a single factor of a number.
 * The factor not have to be a prime factor.
 */
public interface FactorisationAlgorithm {
    long findSingleFactor(long numberToFactorize);

    default String getName(){
        return this.getClass().getSimpleName();

    }
}
