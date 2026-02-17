package de.harich.thilo.factoring;

import de.harich.thilo.math.SmallPrimes;

import static java.lang.Math.pow;

public class TestData {

    /**
     * Try to create numbers to be factorized, which causes the different algorithms to run long.
     * This should be semiprimes, consisting out of two primes. If one would have a split in factors,
     * this should be easier to find (at least by trial factorisation).
     * Hart can factorize two factors of similar size fast.
     * On the other side this is the worst case for Trial factorisation.
     * So we need to balance the size relation of the semi primes.
     * We can start by an exponenten of the lower semiprime at lets say .25 go to .5 by a defined step.
     * lets say .01.
     */
    public static long[] makeSemiprimeList(int bits, int numPrimes, double lowerSemiprimeExponent) {
        long[] numbers = new long[numPrimes];

        double logSmallerPrime = bits * lowerSemiprimeExponent;
        final int targetSmallerPrime = (int) (pow(2.0, logSmallerPrime));
        final int targetBiggerPrime = (int) pow(2.0, bits) / targetSmallerPrime;

        int[] smallerPrimes = SmallPrimes.generatePrimes (targetSmallerPrime, numPrimes);
        int[]  biggerPrimes = SmallPrimes.generatePrimes (targetBiggerPrime, numPrimes);

        for (int i=0; i < numPrimes; i++)
        {
            long semiprime = (long)smallerPrimes[i] * (long)biggerPrimes[i];
            numbers[i] = semiprime;
        }
        System.out.println("created " + numbers.length + " semi primes for exponent of smaller semi prime " + lowerSemiprimeExponent);

        return numbers;
    }
}
