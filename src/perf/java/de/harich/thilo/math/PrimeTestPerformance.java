package de.harich.thilo.math;

import de.harich.thilo.factoring.algorithm.ecm.util.BPSWTest;
import de.harich.thilo.factoring.algorithm.hart.HartFactorization;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;
import de.harich.thilo.factoring.service.FactorisationService;

import java.math.BigInteger;
import java.util.Random;

import static de.harich.thilo.factoring.algorithm.FactorisationAlgorithm.NO_FACTOR_FOUND;
import static de.harich.thilo.factoring.service.FactorisationType.TRIAL_DIVISION_AND_HART;

public class PrimeTestPerformance {

    public static void main(String[] args) {
        System.out.println("Miller-Rabin Primality Test Performance Benchmark");
        System.out.println("-------------------------------------------------");

        comparePrimeTests();
//        benchmark(10, "Small Numbers (up to 10 bits)");
//        benchmark(30, "Medium Numbers (up to 30 bits)");
//        benchmark(60, "Large Numbers (up to 60 bits)");
//        benchmarkPrimes(60, "Large Primes (60 bits)");
    }

    /**
     * for values below 26 bits LemireTrialDivision detects primes fastest.
     * from 26 up to 31 Bits, Rabin Miller is the fastest
     * From 32 - 45 Bits Hart Factorisation is the fastest
     * at 45 bit and bigger Rabin Miller is the fastest again
     *
     */
    public static void comparePrimeTests(){
        int bitsStart = 20;
        int bitsEnd = 50;
//        int numPrimesPerBit = 20;
        long [][] somePrimes = new long[bitsEnd-bitsStart][];
        Random random = new Random();
        long overallInstructionsPerTest = 1_000_000;

        for (int bits = bitsStart; bits < bitsEnd; bits++){
            long instructionsForTest = (long) Math.pow(2, bits * .35);
            int primesForBits = (int) (overallInstructionsPerTest / instructionsForTest);
            somePrimes[bits-bitsStart] = new long[primesForBits];
            System.out.println("create " + primesForBits + " primes for " + bits + " bits");
            for (int i = 0; i < primesForBits; i++) {
                somePrimes[bits-bitsStart][i] = BigInteger.probablePrime(bits, random).longValue();
            }
        }
        FactorisationService lemireHartcalculator = new FactorisationService(TRIAL_DIVISION_AND_HART);

        for (int bits = bitsEnd-1; bits >= bitsStart; bits--){
            System.out.println("run test for " + bits + " bits");
            long instructionsForTest = (long) Math.pow(2, bits * .35);
            int primesForBits = (int) (overallInstructionsPerTest / instructionsForTest);


            long start = System.nanoTime();
            long end = performRabinMillerLong(primesForBits, somePrimes, bits, bitsStart);
            System.out.println("time rabin miller : " + (end - start));

            start = System.nanoTime();
            end = performBigInteger(primesForBits, somePrimes, bits, bitsStart);
            System.out.println("time BigInteger   : " + (end - start));

            start = System.nanoTime();
            end = performBPSW(primesForBits, somePrimes, bits, bitsStart);
            System.out.println("time BPSW         : " + (end - start));

            HartFactorization hartFactorization = new HartFactorization();

            start = System.nanoTime();
            end = performHart(primesForBits, somePrimes, bits, bitsStart, hartFactorization);
            System.out.println("time hart         : " + (end - start));

            start = System.nanoTime();
            end = performLemireTrialDivision(primesForBits, somePrimes, bits, bitsStart, .5);
            System.out.println("time lemire       : " + (end - start));

            start = System.nanoTime();
            end = perfromLemireHart(primesForBits, somePrimes, bits, bitsStart, lemireHartcalculator);
            System.out.println("time lemire hart  : " + (end - start));

            start = System.nanoTime();
            end = performLemireTrialDivision(primesForBits, somePrimes, bits, bitsStart, .35);
            System.out.println("time lemire n^1/3 : " + (end - start));

        }
    }

    private static long performBPSW(int primesForBits, long[][] somePrimes, int bits, int bitsStart) {
        BPSWTest bpswTest = new BPSWTest();
        for (int i = 0; i < primesForBits; i++) {
            long number = somePrimes[bits - bitsStart][i];
            BigInteger numberBig = BigInteger.valueOf(number);
            boolean isPrime = bpswTest.isProbablePrime(numberBig);
            if (!isPrime){
                System.out.println("BigInteger.isProbablePrime not working for prime " + number);
            }
        }
        return System.nanoTime();
    }

    private static long performBigInteger(int primesForBits, long[][] somePrimes, int bits, int bitsStart) {
        for (int i = 0; i < primesForBits; i++) {
            long number = somePrimes[bits - bitsStart][i];
            BigInteger numberBig = BigInteger.valueOf(number);
            int certaintyForLong = bits;
            boolean isPrime = numberBig.isProbablePrime(certaintyForLong);
            if (!isPrime){
                System.out.println("BigInteger.isProbablePrime not working for prime " + number);
            }
        }
        return System.nanoTime();
    }

    private static long perfromLemireHart(int primesForBits, long[][] somePrimes, int bits, int bitsStart, FactorisationService lemireHartcalculator) {

        for (int i = 0; i < primesForBits; i++) {
            long number = somePrimes[bits - bitsStart][i];
//            number = 523972434043057L;
            long[] factors = lemireHartcalculator.getSortedPrimeFactors(number);
            if (factors.length > 1){
                System.out.println("lemire hart calculator not working for prime " + number);
            }
        }
        return System.nanoTime();
    }

    private static long performLemireTrialDivision(int primesForBits, long[][] somePrimes, int bits, int bitsStart, double exponent) {
        LemireTrialDivision lemireTrialDivision = new LemireTrialDivision();
        for (int i = 0; i < primesForBits; i++) {
            long number = somePrimes[bits - bitsStart][i];
            int limit = (int) (Math.pow(number, exponent) + 1);
            if (lemireTrialDivision.findSingleFactor(number, limit) != NO_FACTOR_FOUND){
                System.out.println("lemire factorisation not working for prime " + number);
            }
        }
        return System.nanoTime();
    }

    private static long performHart(int primesForBits, long[][] somePrimes, int bits, int bitsStart, HartFactorization hartFactorization) {
        long end;
        for (int i = 0; i < primesForBits; i++) {
            long number = somePrimes[bits - bitsStart][i];
            int stopAt = (int) Math.cbrt(number) + 1;
            long factor = hartFactorization.findSingleFactor(number, stopAt);
            if (factor != HartFactorization.UNDEFINED){
                System.out.println("hart factorisation not working for prime " + number);
            }
        }
        end = System.nanoTime();
        return end;
    }

    private static long performRabinMillerLong(int primesForBits, long[][] somePrimes, int bits, int bitsStart) {
        for (int i = 0; i < primesForBits; i++) {
            long number = somePrimes[bits - bitsStart][i];
            boolean isPrime = MillerRabin.isPrime(number);
            if (!isPrime){
                System.out.println("rabin miller not working for prime " + number);
            }
        }
        return System.nanoTime();
    }

    private static void benchmark(int bits, String label) {
        Random random = new Random(42);
        int iterations = 100_000;
        long[] numbers = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            numbers[i] = Math.abs(random.nextLong()) % (1L << bits);
            if (numbers[i] < 2) numbers[i] = 2;
        }

        // Warmup
        for (int i = 0; i < 1000; i++) {
            MillerRabin.isPrime(numbers[i % iterations]);
        }

        long start = System.nanoTime();
        int count = 0;
        for (int i = 0; i < iterations; i++) {
            if (MillerRabin.isPrime(numbers[i])) {
                count++;
            }
        }
        long end = System.nanoTime();

        double avgNs = (double) (end - start) / iterations;
        System.out.printf("%-30s: %8.2f ns/op (%d/%d probable primes found)%n", label, avgNs, count, iterations);
    }

    private static void benchmarkPrimes(int bits, String label) {
        // Use some known large primes or just find some
        long[] primes = new long[1000];
        long n = (1L << bits) - 1;
        if (n % 2 == 0) n--;
        int found = 0;
        while (found < 1000) {
            if (MillerRabin.isPrime(n)) {
                primes[found++] = n;
            }
            n -= 2;
        }

        // Warmup
        for (int i = 0; i < 100; i++) {
            MillerRabin.isPrime(primes[i % found]);
        }

        long start = System.nanoTime();
        int iterations = 10_000;
        for (int i = 0; i < iterations; i++) {
            MillerRabin.isPrime(primes[i % found]);
        }
        long end = System.nanoTime();

        double avgNs = (double) (end - start) / iterations;
        System.out.printf("%-30s: %8.2f ns/op%n", label, avgNs);
    }
}
