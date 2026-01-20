package de.harich.thilo.factoring.trialdivision;

import de.harich.thilo.factoring.Factor;
import de.harich.thilo.factoring.FactorisationAlgorithm;
import de.harich.thilo.math.SmallPrimes;

import java.util.ArrayList;
import java.util.List;

import static de.harich.thilo.factoring.Factor.createFactor;
import static de.harich.thilo.factoring.Factor.createPrimeFactor;

public class TrialDivisionAlgorithm implements FactorisationAlgorithm {

    // The allowed discriminator bit size is d <= 53 - bitLength(N/p), thus d<=23 would be safe
    // for any integer N and p>=2. d=10 is the value that performs best, determined by experiment.
    private static final double DISCRIMINATOR = 1.0/(1<<10);
    public List<Factor> findFactors(long numberToFactorize, int limit) {
        List<Factor> primeFactors = new ArrayList<>();

        int[] primes = SmallPrimes.generatePrimes(limit);
        double[] reciprocals = getReciprocals(primes);
        long q;
        // TODO try to use Factorisation here as well
        for (int i=0; i < limit && numberToFactorize > 1; i++) {
            double pInverse = reciprocals[i];
            int p = primes[i];
            // TODO use Fused Multiply Add
            while ((q = (long) (numberToFactorize * pInverse + DISCRIMINATOR)) * p == numberToFactorize) {
                primeFactors.add(createPrimeFactor(p));
                numberToFactorize = q; // avoiding a division here by storing q benefits the int version but not the long version
            }
            if (p*(long)p > numberToFactorize) {
                break;
            }
        }
        if (numberToFactorize>1) {
            // either N is prime, or we could not find all factors with p<=pLimit -> add the rest to the result
            primeFactors.add(createFactor(numberToFactorize));
        }
        return primeFactors;
    }

    private static double[] getReciprocals(int[] primes) {
        double[] reciprocals = new double[primes.length];
        for (int i = 0; i< primes.length; i++) {
            reciprocals[i] = 1.0/ primes[i];
        }
        return reciprocals;
    }

    public int findSingleFactor(long numberToFactorize, int maxPrimeFactor) {
        // TODO move to constructor
        int[] primes = SmallPrimes.generatePrimes(maxPrimeFactor);
        double[] reciprocals = getReciprocals(primes);

        for (int i=0; i < primes.length && numberToFactorize > 1; i++) {
            double pInverse = reciprocals[i];
            int p = primes[i];
            if (((int)((numberToFactorize * pInverse) + DISCRIMINATOR) * p) == numberToFactorize) {
                return p;
            }
        }
        return -1;
    }

    @Override
    public long findSingleFactor(long numberToFactorize) {
        return findSingleFactor(numberToFactorize, (int) Math.sqrt(numberToFactorize));
    }
}

