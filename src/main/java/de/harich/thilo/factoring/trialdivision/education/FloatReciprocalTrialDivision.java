package de.harich.thilo.factoring.trialdivision.education;

/**
 * This is slower than the double implementation ReciprocalArrayTrialDivision.
 * Not clear why.
 *
 */
public class FloatReciprocalTrialDivision extends ReciprocalArrayTrialDivision {

    static float[] reciprocals = {.5F};

    public FloatReciprocalTrialDivision() {
        super(0);
    }
    public FloatReciprocalTrialDivision(int maxPrimeFactor) {
        super(maxPrimeFactor);
        ensureReciprocalsExist();
    }

     protected static void ensureReciprocalsExist() {
        if (reciprocals.length != primes.length) {
            reciprocals = new float[primes.length];
            for (int i = 0; i < primes.length; i++) {
                reciprocals[i] = 1.0F / primes[i];
            }
        }
    }

    public float getReciprocalFloat(int i) {
        return reciprocals[i];
    }

    public int numberDivFactor(int numberToFactorize, int factorIndex) {
        float pInverse = getReciprocalFloat(factorIndex);
//        return (int) ((numberToFactorize * pInverse) + DISCRIMINATOR);
        return (int) (numberToFactorize * pInverse);
    }

    boolean factorFound(int number, int factorIndex){
        return numberDivFactor(number, factorIndex) * getFactor(factorIndex) == number;
    }

    public int findSingleFactor(long number, int maxPrimeFactor) {
        final int numberInt = (int) number;
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        ensureReciprocalsExist();
        for (int primeIndex = 0; primeIndex < maxPrimeFactorIndex; primeIndex++) {

            if (factorFound (numberInt, primeIndex))    return getFactor(primeIndex);
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);
            if (factorFound (number, ++primeIndex))  return getFactor(primeIndex);

        }
        return -1;
    }

}

