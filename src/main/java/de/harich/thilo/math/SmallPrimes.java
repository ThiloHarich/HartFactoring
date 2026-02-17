package de.harich.thilo.math;


import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static de.harich.thilo.factoring.algorithm.FactorisationAlgorithm.NO_FACTOR_FOUND;

// TODO add some more method for finding a prime of type long
// prime for number of Bits or a number using a wheel + some of the existing methods.
public class SmallPrimes {

    // for up to 26 bit we expect 100 kB memory
    private static final int GENERATE_ALL_LIMIT = (int) (1L << 24);

    // BitSet, HashSet and int[] should hve the same amount of memory !?
    private static BitSet primesSet;

    private final static Random rnd = new Random();

    private final static LemireTrialDivision lemireTrialDivision = new LemireTrialDivision();

    public static int[] generatePrimes(int limit) {
        boolean[] isComposite = getIsComposite(limit);

        int count = getCount(limit, isComposite);

        // Array mit Primzahlen füllen
        int[] primes = new int[count];
        int idx = 0;
        for (int i = 2; i <= limit; i++) {
            if (!isComposite[i]) primes[idx++] = i;
        }

        return primes;
    }

    private static int getCount(int limit, boolean[] isComposite) {
        int count = 0;
        for (int i = 2; i <= limit; i++) {
            if (!isComposite[i]) count++;
        }
        return count;
    }

    private static boolean[] getIsComposite(int limit) {
        boolean[] isComposite = new boolean[limit + 1];
        for (int i = 2; i * i <= limit; i++) {
            if (!isComposite[i]) {
                for (int j = i * i; j <= limit; j += i) {
                    isComposite[j] = true;
                }
            }
        }
        return isComposite;
    }

    public static BitSet generatePrimesSet(int limit) {
        boolean[] isComposite = getIsComposite(limit);

        // Array mit Primzahlen füllen
        primesSet = new BitSet();
        for (int i = 2; i <= limit; i++) {
            if (!isComposite[i]) primesSet.set(i);
        }

        return primesSet;
    }

    /**
     * Creates numbers, which are hard to factorize.
     * So-called semiprimes, which consist out of two Primes.
     * Here one Prime is between n^.37 and n^.5. ; n = 2^bits
     * More precisely iterate over the integers between (bits * .37) and bits/2,
     * and create a Prime, and create another prime with the remaining bits.
     * This might ensure the numbers are hard to factorize but do not have some kind of structure.
     *
     */
    public static long[] makeSemiPrimesList(int bits, int numPrimes, boolean readFromFile, double lowerSemiprimeExponent) {
        long[] semiPrimes = new long[numPrimes];
        final String file = "semiPrimes_" + (numPrimes / 1000) + "K_" + bits + "Bits.dat";
        final Path path = Paths.get(file);

        if (Files.exists(path) && readFromFile)
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream((file)));
                semiPrimes = (long[]) inputStream.readObject();

//			List<String> semiPrimeList = Files.readAllLines(path);
//			System.out.println("found " + semiPrimes.length + " semi primes in file "+ path);
                return semiPrimes;
            } catch (final IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        System.out.println("no semi primes file "+ path + " will create at least " + numPrimes + " semi primes");
        long start = System.currentTimeMillis();

        Random rnd = new Random();
        for (int i=0; i < numPrimes; )
        {
            final int smallFactorBitsMin = (int) Math.ceil(bits * lowerSemiprimeExponent);
//			final int smallFactorBitsMin = (int) (bits * .5);
            final int smallFactorBitsMax = (int) (bits * .5);
//			final int smallFactorBitsMax = 20;
//			final int smallFactorBitsMin = smallFactorBitsMax;
//			final int smallFactorBitsMin = 14;
            for(int bitsFirst = smallFactorBitsMin; bitsFirst <= smallFactorBitsMax && i< numPrimes; bitsFirst++) {
//						final int smallFactorBits = (bits / 3) - 1;

//			rnd = new Random();
                final BigInteger fact1 = BigInteger.probablePrime(bitsFirst, rnd);
                final int bigFactorBits = bits - bitsFirst;
//			rnd = new Random();
                final BigInteger fact2 = BigInteger.probablePrime(bigFactorBits, rnd);
                long semiprime = fact1.longValue() * fact2.longValue();
//                if (semiprime % 8 == 7) {
                semiPrimes[i] = semiprime;
                i++;
//                }

                if (i % 10000 == 0)
                    System.out.println((100.0 * i / numPrimes) + "% of semi prime creation done. Created " + i + " semi primes");
            }
        }
//		String semiprimeList = "";
//		for (Long semiPrime : semiPrimes) {
//			if (semiPrime != null)
//			semiprimeList += semiPrime + System.lineSeparator();
//		}
        long endCreation = System.currentTimeMillis();
//		final byte[] strToBytes = semiprimeList.getBytes();


        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(semiPrimes);
//			Files.write(path, semiPrimes);
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long endWrite = System.currentTimeMillis();
        System.out.println("wrote " + semiPrimes.length + " semi primes. Took " + (endCreation - start)/ 1000.0 + " sec to create numbers.");
        System.out.println("wrote " + semiPrimes.length + " semi primes. Took " + (endWrite - endCreation)/ 1000.0 + " sec to write numbers.");

        return semiPrimes;
    }

    public static int[] generatePrimes(int targetPrime, int numPrimes) {
        if (targetPrime < GENERATE_ALL_LIMIT){
            return generateSmallPrimes(targetPrime, numPrimes);
        }
        else{
            return generateBigPrimes(targetPrime, numPrimes);
        }
    }

    private static int[] generateBigPrimes(int targetPrime, int range) {
        Set<Integer> primes = new HashSet<>();

        while (primes.size() < range) {
            addBigPrime(targetPrime, getPrimeRange(range), primes);
        }
        return primes.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int getPrimeRange(int range) {
        int bitsOfRange = Long.SIZE - Long.numberOfLeadingZeros(range);
        return 2 * (bitsOfRange * range);
    }

    private static void addBigPrime(int targetPrime, int primeRange, Set<Integer> primes) {
        int numberToCheck = getNumberToCheck(targetPrime, primeRange);
        if (lemireTrialDivision.findSingleFactor(numberToCheck) == NO_FACTOR_FOUND){
            primes.add(numberToCheck);
        }
    }

    private static int[] generateSmallPrimes(int targetPrime, int numPrimes) {
        ensurePrimesSetExist();
        Set<Integer> primes = new HashSet<>();

        while (primes.size() < numPrimes) {
            addSmallPrime (targetPrime, getPrimeRange(numPrimes), primes);
        }
        return primes.stream().mapToInt(Integer::intValue).toArray();
    }

    private static void addSmallPrime(int targetPrime, int primeRange, Set<Integer> primes) {
        int numberToCheck = getNumberToCheck(targetPrime, primeRange);
        int prime = primesSet.nextSetBit(numberToCheck);
        primes.add(prime);
    }

    private static int getNumberToCheck(int targetPrime, int primeRange) {
        int origin = targetPrime - primeRange;
        return rnd.nextInt(Math.max(origin, 0), targetPrime + primeRange);
    }

    private static void ensurePrimesSetExist() {
        if (primesSet == null || primesSet.length() + 10 < GENERATE_ALL_LIMIT){
            generatePrimesSet(GENERATE_ALL_LIMIT);
        }
    }
}
