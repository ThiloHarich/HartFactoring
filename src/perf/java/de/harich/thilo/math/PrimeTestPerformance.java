package de.harich.thilo.math;

import de.harich.thilo.factoring.algorithm.ecm.util.BPSWTest;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;
import de.harich.thilo.factoring.service.FactorisationService;

import java.math.BigInteger;
import java.util.Random;

import static de.harich.thilo.factoring.algorithm.FactorisationAlgorithm.NO_FACTOR_FOUND;
import static de.harich.thilo.factoring.service.FactorisationType.TRIAL_DIVISION_AND_HART;

/**
 * When running this test you should see a comparison of different Primability Tests.
 * The first RabinMiller is the fastest above 46 bits.
 * The second is the BigInteger.isPropablePrime implementation 
 * BPSW is a implementation taken from Tillmann Neumann 
 * Lemire and Lemire Hart are the factorization algorithms provided by this project.
 * Both have certain ranges, where they perform better than the specialized primability checks.
 * This should indicate that they perform very well in this ranges, since other factorization algorithms usually 
 * first chek if the number is a prime, bevor the algorithm itself is applied.
 * The relation of the runtimes to the Primability Tests might indicate that 
 * up to 45 Bits the Lemire Hart Algorithm provided by this project might be one of the fastest algorithms out there.
 * When running this Test you should get something like this:
 * -----------------------------------------------------------
 * Bits  | R-Miller     | BigInt       | BPSW         | Lemire 1/2   | L-Hart       | Fastest         | LH-Slowdown
 * --------------------------------------------------------------------------------------------------------------
 * 61    | 1053200      | 617100       | 32025000     | n/a          | 52222500     | BigInt          | 84,63       x
 * 60    | 227400       | 412000       | 3005800      | n/a          | 73568000     | R-Miller        | 323,52      x
 * 59    | 349000       | 12242600     | 2264400      | n/a          | 7412600      | R-Miller        | 21,24       x
 * 58    | 131000       | 2582500      | 1641900      | n/a          | 28234600     | R-Miller        | 215,53      x
 * 57    | 5199800      | 208400       | 1079400      | n/a          | 4003500      | BigInt          | 19,21       x
 * 56    | 100500       | 392500       | 1381700      | n/a          | 4953300      | R-Miller        | 49,29       x
 * 55    | 98400        | 216000       | 266500       | n/a          | 2214100      | R-Miller        | 22,50       x
 * 54    | 63500        | 114100       | 264800       | n/a          | 1663400      | R-Miller        | 26,20       x
 * 53    | 85300        | 2353100      | 276300       | n/a          | 1605900      | R-Miller        | 18,83       x
 * 52    | 93000        | 111000       | 355600       | n/a          | 1104500      | R-Miller        | 11,88       x
 * 51    | 89200        | 188100       | 231100       | n/a          | 1454200      | R-Miller        | 16,30       x
 * 50    | 63000        | 108800       | 220900       | n/a          | 704000       | R-Miller        | 11,17       x
 * 49    | 81200        | 138200       | 204200       | n/a          | 962100       | R-Miller        | 11,85       x
 * 48    | 76700        | 142900       | 194000       | n/a          | 482400       | R-Miller        | 6,29        x
 * 47    | 54900        | 92700        | 197600       | n/a          | 410000       | R-Miller        | 7,47        x
 * 46    | 135300       | 184400       | 250900       | n/a          | 333100       | R-Miller        | 2,46        x
 * 45    | 101600       | 113600       | 342200       | n/a          | 739400       | R-Miller        | 7,28        x
 * 44    | 52800        | 85300        | 300000       | n/a          | 212000       | R-Miller        | 4,02        x
 * 43    | 48000        | 90400        | 640700       | n/a          | 364700       | R-Miller        | 7,60        x
 * 42    | 63200        | 84300        | 339900       | n/a          | 159200       | R-Miller        | 2,52        x
 * 41    | 50600        | 75200        | 172800       | n/a          | 118100       | R-Miller        | 2,33        x
 * 40    | 62800        | 89200        | 3267000      | n/a          | 102600       | R-Miller        | 1,63        x
 * 39    | 86900        | 93500        | 91795500     | 2072100      | 89100        | R-Miller        | 1,03        x
 * 38    | 93600        | 88400        | 176100       | 1674200      | 96000        | BigInt          | 1,09        x
 * 37    | 50600        | 188300       | 261700       | 1273700      | 69900        | R-Miller        | 1,38        x
 * 36    | 927600       | 237700       | 159800       | 1608900      | 763500       | BPSW            | 4,78        x
 * 35    | 101300       | 95400        | 219500       | 543300       | 51700        | L-Hart          | 1,00        x
 * 34    | 42300        | 90600        | 179900       | 426200       | 400000       | R-Miller        | 9,46        x
 * 33    | 36100        | 53200        | 10120300     | 376600       | 51500        | R-Miller        | 1,43        x
 * 32    | 132900       | 82200        | 202100       | 242900       | 33900        | L-Hart          | 1,00        x
 * 31    | 14400        | 55100        | 183300       | 157600       | 148700       | R-Miller        | 10,33       x
 * 30    | 25800        | 75900        | 238400       | 127800       | 239700       | R-Miller        | 9,29        x
 * 29    | 14600        | 86900        | 185500       | 94900        | 135800       | R-Miller        | 9,30        x
 * 28    | 13200        | 52300        | 119300       | 70900        | 92100        | R-Miller        | 6,98        x
 * 27    | 11300        | 53900        | 104300       | 54300        | 27800        | R-Miller        | 2,46        x
 * 26    | 14000        | 49800        | 2892600      | 53200        | 34100        | R-Miller        | 2,44        x
 * 25    | 19600        | 66800        | 107400       | 38600        | 38700        | R-Miller        | 1,97        x
 * 24    | 14200        | 49900        | 3798000      | 36200        | 7367600      | R-Miller        | 518,85      x
 * 23    | 12500        | 49700        | 148900       | 24100        | 81300        | R-Miller        | 6,50        x
 * 22    | 14700        | 43600        | 864600       | 24200        | 26900        | R-Miller        | 1,83        x
 * 21    | 24600        | 44100        | 116700       | 16200        | 23900        | Lemire          | 1,48        x
 * 20    | 15000        | 44900        | 11103400     | 17900        | 32600        | R-Miller        | 2,17        x
 */
public class PrimeTestPerformance {

    public static void main(String[] args) {
        System.out.println("Primality Test Performance Benchmark (Times in nanoseconds)");
        System.out.println("-----------------------------------------------------------");

        comparePrimeTests();
    }

    public static void comparePrimeTests(){
        int bitsStart = 20;
        int bitsEnd = 62;
        long [] somePrimes = new long[bitsEnd-bitsStart];
        Random random = new Random();

        for (int bits = bitsStart; bits < bitsEnd; bits++){
            somePrimes[bits-bitsStart] = BigInteger.probablePrime(bits, random).longValue();
        }
        FactorisationService lemireHartcalculator = new FactorisationService(TRIAL_DIVISION_AND_HART, 0.35);

        System.out.printf("%-5s | %-12s | %-12s | %-12s | %-12s | %-12s | %-15s | %-12s%n", 
                "Bits", "R-Miller", "BigInt", "BPSW", "Lemire 1/2", "L-Hart", "Fastest", "LH-Slowdown");
        System.out.println("-".repeat(110));

        for (int bits = bitsEnd-1; bits >= bitsStart; bits--){
            long prime = somePrimes[bits - bitsStart];
            
            long start = System.nanoTime();
            performRabinMillerLong(prime);
            long tRM = System.nanoTime() - start;

            start = System.nanoTime();
            performBigInteger(prime, bits);
            long tBI = System.nanoTime() - start;

            start = System.nanoTime();
            performBPSW(prime);
            long tBPSW = System.nanoTime() - start;

            Long tLemire = null;
            if (bits < 40) {
                start = System.nanoTime();
                performLemireTrialDivision(prime, .5);
                tLemire = System.nanoTime() - start;
            }

            start = System.nanoTime();
            performLemireHart(prime, lemireHartcalculator);
            long tLH = System.nanoTime() - start;

            // Determine fastest among full primality tests
            long minTime = Math.min(Math.min(tRM, tBI), Math.min(tBPSW, tLH));
            if (tLemire != null) minTime = Math.min(minTime, tLemire);
            
            String fastest = "";
            if (minTime == tRM) fastest = "R-Miller";
            else if (minTime == tBI) fastest = "BigInt";
            else if (minTime == tBPSW) fastest = "BPSW";
            else if (tLemire != null && minTime == tLemire) fastest = "Lemire";
            else if (minTime == tLH) fastest = "L-Hart";

            double slowdown = (double) tLH / minTime;

            System.out.printf("%-5d | %-12d | %-12d | %-12d | %-12s | %-12d | %-15s | %-12.2fx%n", 
                    bits, tRM, tBI, tBPSW, (tLemire == null ? "n/a" : tLemire.toString()), tLH, fastest, slowdown);
        }
    }

    private static void performBPSW(long number) {
        BPSWTest bpswTest = new BPSWTest();
        if (!bpswTest.isProbablePrime(number)) {
            System.err.println("BPSW failed for prime " + number);
        }
    }

    private static void performBigInteger(long number, int bits) {
        if (!BigInteger.valueOf(number).isProbablePrime(bits)) {
            System.err.println("BigInt failed for prime " + number);
        }
    }

    private static void performLemireHart(long number, FactorisationService lemireHartcalculator) {
        long[] factors = lemireHartcalculator.getSortedPrimeFactors(number);
        if (factors.length > 1) {
            System.err.println("LemireHart failed for prime " + number);
        }
    }

    private static void performLemireTrialDivision(long number, double exponent) {
        LemireTrialDivision lemireTrialDivision = new LemireTrialDivision();
        int limit = (int) (Math.pow(number, exponent) + 1);
        if (lemireTrialDivision.findSingleFactor(number, limit) != NO_FACTOR_FOUND) {
            System.err.println("TrialDivision found factor for prime " + number);
        }
    }

    private static void performRabinMillerLong(long prime) {
        if (!MillerRabin.isPrime(prime)) {
            System.err.println("R-Miller failed for prime " + prime);
        }
    }
}
