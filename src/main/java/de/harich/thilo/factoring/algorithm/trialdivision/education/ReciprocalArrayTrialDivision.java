package de.harich.thilo.factoring.algorithm.trialdivision.education;

/**
 * We try to make even more use of Vectorisation here, by precalculating the reciprocals in an array.
 * So we use here:
 * - an array to store the primes comes from PrimeReciprocalTrialDivision already
 * - an array to store the reciprocals of the primes
 * - fetch the precalculated reciprocal of factor to determine if a number is dividable ba a factor
 * this new array improves performance by factor of 2 over PrimeReciprocalTrialDivision
 */
public class ReciprocalArrayTrialDivision extends PrimeReciprocalTrialDivision {

    static double[] reciprocals = {.5};

    public ReciprocalArrayTrialDivision() {
        super(0);
    }
    public ReciprocalArrayTrialDivision(int maxPrimeFactor) {
        super(maxPrimeFactor);
        ensureReciprocalsExist();
    }

    // instead of calculating
    protected static void ensureReciprocalsExist() {
        if (reciprocals.length != primes.length) {
            reciprocals = new double[primes.length];
            for (int i = 0; i < primes.length; i++) {
                reciprocals[i] = 1.0 / primes[i];
            }
        }
    }

    public double getReciprocal(int i) {
        return reciprocals[i];
    }

    public int findSingleFactor(long number, int maxPrimeFactor) {
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        ensureReciprocalsExist();
//        final int i1 = (maxPrimeFactorIndex + 16) & ~15;
//        for (int i = 0; i < i1; i++) {
        for (int primeIndex = 0; primeIndex < maxPrimeFactorIndex; primeIndex++) {
            if (hasPrimeFactor(number, primeIndex))    return getPrimeFactor(primeIndex);
            // here unrolling helps
            // TODO how to support different AVX ? For SSE-2 4 or 8 statements are optimal
            if (hasPrimeFactor(number, ++primeIndex))  return getPrimeFactor(primeIndex);
            if (hasPrimeFactor(number, ++primeIndex))  return getPrimeFactor(primeIndex);
            if (hasPrimeFactor(number, ++primeIndex))  return getPrimeFactor(primeIndex);
//            if (factorFound (numberToFactorize, ++primeIndex))  return getFactor(primeIndex);
//            if (factorFound (numberToFactorize, ++primeIndex))  return getFactor(primeIndex);
//            if (factorFound (numberToFactorize, ++primeIndex))  return getFactor(primeIndex);
//            if (factorFound (numberToFactorize, ++primeIndex))  return getFactor(primeIndex);


        }
        return -1;
    }

    @Override
    public int[] findPrimefactorIndices(long number, int maxPrimeFactor){
        // will also be called in the super method
        ensurePrimesExist(maxPrimeFactor);
        // this line is new
        ensureReciprocalsExist();
        return super.findPrimefactorIndices(number, maxPrimeFactor);
    }
}

