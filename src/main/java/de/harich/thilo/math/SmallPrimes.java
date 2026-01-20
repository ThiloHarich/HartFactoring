package de.harich.thilo.math;


import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Random;

public class SmallPrimes {

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
        BitSet primes = new BitSet();
        for (int i = 2; i <= limit; i++) {
            if (!isComposite[i]) primes.set(i);
        }

        return primes;
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
    public static long[] makeSemiPrimesList(int bits, int numPrimes, boolean readFromFile) {
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
            final int smallFactorBitsMin = (int) Math.ceil(bits * .37);
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
}
