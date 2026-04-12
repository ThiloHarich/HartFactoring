package de.harich.thilo.factoring.service;

import de.harich.thilo.factoring.TestData;
import de.harich.thilo.math.MillerRabin;

import java.util.Random;

import static de.harich.thilo.factoring.service.FactorisationType.TRIAL_DIVISION_AND_HART;
import static de.harich.thilo.factoring.service.FactorisationType.TRIAL_DIVISION_ONLY;

public class FactorisationServiceComparison {

    // a Value which steers the estimated time the test will take
    // we might do around 1 million operations a second?
    public static final long RUNNING_TIME = 100_000_000;

    public static void main(String[] args) {
        int bits = 45;
        FactorisationService[] calculators = {
                new FactorisationService(TRIAL_DIVISION_AND_HART, 0.36),
                // use lemire up to exponent and switch to hart
                new FactorisationService(TRIAL_DIVISION_AND_HART, 0.35),


                // always use Lemire
                new FactorisationService(TRIAL_DIVISION_ONLY),
        };
//        System.out.println("--- Scenario A: Hard Semiprimes with Different Exponents ---");
//        compareSemiprimes(bits, calculators);

//        System.out.println("\n--- Scenario B: Numbers with Multiple Primes of Same Size ---");
//        compareProductOfSmallPrimes(bits, calculators);
//
        System.out.println("\n--- Scenario C: Successive Numbers ---");
        compareSuccessiveNumbers(bits, calculators);

//        System.out.println("\n--- Scenario D: Primes ---");
//        comparePrimes(bits);
    }

    public static void compareSemiprimes(int bits, FactorisationService[] calculators) {
        double[] exponents = {0.5, 0.3, 0.4, 0.45};


        for (double exponent : exponents) {
            System.out.println("\nExponent: " + exponent);
            int numPrimes = (int) Math.pow(2.0, bits * exponent * .6);
            long[] numbers = TestData.makeSemiprimeList(bits, numPrimes, exponent);
            logTimings(calculators, numbers, 0.5);
        }
    }

    public static void compareProductOfSmallPrimes(int bits, FactorisationService[] calculators) {
        double[] exponents = {0.2, 0.25, 0.3, 0.35, .4};

        for (double exponent : exponents) {
            System.out.println("\nPrime Exponent: " + exponent);
            int numPrimes = (int) Math.pow(2.0, bits * exponent * .6);
            long[] numbers = TestData.makePrimesOfSameSizeList(bits, numPrimes, exponent);
            logTimings(calculators, numbers, 0.5);
        }
    }

    public static void compareSuccessiveNumbers(int bits, FactorisationService[] calculators) {
        Random random = new Random();
        long base = random.nextLong(1L << bits);
//        base = 23880836102162L;
        double hartAlgorithmExponent = 0.3;
        double expectedTimePerNumber = Math.pow(base, hartAlgorithmExponent);
        int successiveCount = (int) (RUNNING_TIME / expectedTimePerNumber);
            long[] numbers = new long[successiveCount];
            for (int i = 0; i < successiveCount; i++) {
                numbers[i] = base + i;
            }
            logTimings(calculators, numbers, hartAlgorithmExponent);
    }

    public static void logTimings(FactorisationService[] calculators, long[] numbersToFactorize, double exponentOfTime) {
        long overallMin = Long.MAX_VALUE;
        long[] times = new long[calculators.length];

        System.out.println("Name of the calculator                                                       :\tabsolute time \t relative to best \t relative to algorithm above");
        for (int i = 0; i < calculators.length; i++) {

//            measure(calculators[i], numbersToFactorize, false, exponentOfTime);
            measure(calculators[i], numbersToFactorize, true, exponentOfTime);

            long lastTime = (i == 0) ? Long.MAX_VALUE : times[i - 1];
            times[i] = measure(calculators[i], numbersToFactorize, false, exponentOfTime);

            if (times[i] < overallMin) {
                overallMin = times[i];
            }

            double relativeTime = overallMin == Long.MAX_VALUE ? 1 : ((double) times[i]) / overallMin;
            double relativeToLast = times[i] / (lastTime + 0.0);
            final String name = String.format("%-75s", calculators[i].getName());
            System.out.println(name + "  :    \t" + times[i] + " \t " + relativeTime + "\t " + relativeToLast);
        }
    }

    protected static long measure(final FactorisationService calculator, final long[] numbersToFactorize, boolean test, double exponentOfTime) {
        // Warmup: perform one factorization
//        calculator.getSortedPrimeFactors(15);

        final long start = System.nanoTime();
//        double totalFactorisations = RUNNING_TIME / Math.pow(numbersToFactorize[0], 0.4);
        double totalFactorisations = RUNNING_TIME / Math.pow(numbersToFactorize[0], exponentOfTime);

        for (int i = 0; i < totalFactorisations; ) {
            for (int j = 0; j < numbersToFactorize.length && i < totalFactorisations; j++, i++) {
                long number = numbersToFactorize[j];
                long[] factors = calculator.getSortedPrimeFactors(number);
                if (test) {
                    long product = 1;
                    for (long factor : factors) {
                        product *= factor;
                        if (!MillerRabin.isPrime(factor)) {
                            System.out.println(calculator.getClass() + " did not composite factor " + factor + " of number " + number + " into prime factors");
                            calculator.getSortedPrimeFactors(number);
                        }
                    }

                    if (product != number) {
                        // For NoDivisionCalculator, it only finds factors below sqrt, so we check if the remaining part is prime/1
                        // but to keep it simple and consistent with Hart comparison's "semiprimeMaybe" check:
                        // The calculators should return all prime factors for a complete check.
                        // However, LemireFactorisationCalculator's implementation in this project seems to only return factors below sqrt.
                        System.out.println("number was " + number + " but product of factors is " + product);
                    }
                }
            }
        }

        return System.nanoTime() - start;
    }
}
