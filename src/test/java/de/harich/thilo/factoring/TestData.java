package de.harich.thilo.factoring;

import de.harich.thilo.math.SmallPrimes;

import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

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
    public static List<Long> makeSemiprimeList(int bits) {
        double smallerExponent = 0.3;
        final int numPrimes = (int) pow(2.0, bits * smallerExponent) / bits;
        double biggerExponent = .5;
        double stepExponent = .024999;
        return DoubleStream
                .iterate(smallerExponent, e -> e < biggerExponent, e -> e + stepExponent)
                .mapToObj(e -> makeSemiprimeList(bits, numPrimes, e)) // Erzeugt Stream<long[]>
                .flatMapToLong(LongStream::of)                                         // Macht daraus einen einzigen LongStream
                .boxed()
                .toList();
    }

    public static long[] makePrimesOfSameSizeList(int bits, int numNumbers, double biggestPrimeExponent) {
        long[] numbers = new long[numNumbers];

        double logBiggestPrime = bits * biggestPrimeExponent;
        final int targetPrime = (int) (pow(2.0, logBiggestPrime));
        final int targetProduct = (int) (pow(2.0, bits));
        int numPrimes = (int) (1/biggestPrimeExponent);

        int[] targetPrimes = SmallPrimes.generatePrimes (targetPrime, numNumbers * numPrimes);


        for (int i=0; i < numNumbers; i++)
        {
            long product = 1;

            for (int j = i*numPrimes;j < (i+1)*numPrimes -1; j++) {
                product *= targetPrimes[j];
            }
            long lastFactor = targetProduct / product;
            numbers[i] =  product * lastFactor;
        }
        System.out.println("created " + numbers.length + " products of " + numPrimes + " primes of size n^" + biggestPrimeExponent);

        return numbers;
    }

    public static long[] makeSemiprimeList(int bits, int numSemiPrimes, double lowerSemiprimeExponent) {
        long[] numbers = new long[numSemiPrimes];

        double logSmallerPrime = bits * lowerSemiprimeExponent;
        final int targetSmallerPrime = (int) (pow(2.0, logSmallerPrime));
        final int targetBiggerPrime = (int) pow(2.0, bits) / targetSmallerPrime;

        int[] smallerPrimes = SmallPrimes.generatePrimes (targetSmallerPrime, numSemiPrimes);
        int[]  biggerPrimes = SmallPrimes.generatePrimes (targetBiggerPrime, numSemiPrimes);

        for (int i=0; i < numSemiPrimes; i++)
        {
            long semiprime = (long)smallerPrimes[i] * (long)biggerPrimes[i];
            numbers[i] = semiprime;
        }
        System.out.println("created " + numbers.length + " semi primes for exponent of smaller semi prime " + lowerSemiprimeExponent);

        return numbers;
    }


}
