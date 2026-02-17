package de.harich.thilo.factoring.calculator;

import de.harich.thilo.factoring.FactorisationRunner;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

import static de.harich.thilo.factoring.TestData.makeSemiprimeList;
import static java.lang.Math.pow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactorisationCalculatorTest {

    @Test
    public void testFactorisationCorrectness(){
        LemireHartSmoothFactorisationCalculator factorization = new LemireHartSmoothFactorisationCalculator(new LemireTrialDivision());
//        int fromIndex = (int) 1L << 20;
        int fromIndex = 235133;
        int length = 1000;
        for (int i = fromIndex; i < fromIndex + length; i++) {
            // when testing trial division only we need sqrt as limit
//            List<Factor> factors = factorization.getSortedPrimeFactorsWithExponent(i, (int) Math.sqrt(i));
//            long[] factors = factorization.getSortedPrimeFactorsWithExponent(i, true);
            long[] factors = factorization.getSortedPrimeFactors(i);
            String factorString = FactorisationRunner.toString(factors);
            Long product = Arrays.stream(factors).filter(f -> f > 0).reduce(1L, (a, b) -> a * b);
            String csv = FactorisationRunner.toCsvString(factors);
            assertEquals(product, i);
            System.out.println(i + " : " + factorString + "\t csv : " + csv);
        }
    }


    @Test
    public void comparePerformance(){
        // TODO  cross over should be at prime (n^1/3) ~ n^(1/3) * log(n^1/3)
        // ~ n^(1/3) * const * numberBits(n^1/3)
        FactorisationCalculator[] calculators = {
//                new FactorisationCalculatorLemireInt(),
                new LemireFactorisationCalculator(),
                new HartFactorisationCalculator(),
                new LemireHartSmoothFactorisationCalculator(new LemireTrialDivision()),
                new LemireHartRoughFactorisationCalculator(new LemireTrialDivision())
        } ;


        for (int bits = 30; bits < 50; bits++) {
            System.out.println ("Bits : " + bits);
            System.out.println();

            final long start = System.nanoTime();
//        long[] numbersToFactorize = SmallPrimes.makeSemiPrimesList(bits, numPrimes, readFromFile, .25);
            //
            double smallerExponent = 0.3;
            final int numPrimes = (int) pow(2.0, bits * smallerExponent) / bits;
            double biggerExponent = .5;
            double stepExponent = .024999;
            int finalBits = bits;
            List<Long> numbersToFactorize = DoubleStream
                    .iterate(smallerExponent, e -> e < biggerExponent, e -> e + stepExponent)
                    .mapToObj(e -> makeSemiprimeList(finalBits, numPrimes, e)) // Erzeugt Stream<long[]>
                    .flatMapToLong(LongStream::of)                                         // Macht daraus einen einzigen LongStream
                    .boxed()
                    .toList();
            long lap1 = System.nanoTime();
            System.out.println("time for making Primes : " + (lap1 - start));
            System.out.println("Name of the factorization                                                        :\tabsolute time \t relative to best ");

            long minTime = Long.MAX_VALUE;
            for (FactorisationCalculator calculator : calculators) {

                // two times warmup
                doFactorisation(calculator, numbersToFactorize);
                doFactorisation(calculator, numbersToFactorize);
                lap1 = System.nanoTime();
                // now do the real job, and measure performance
                int loop = 100000 / numbersToFactorize.size();
                for (int i = 0; i < loop; i++) {
                    doFactorisation(calculator, numbersToFactorize);
                }

                final long lap2 = System.nanoTime();
                long time = lap2 - lap1;
                double relativeTime = ((double) time/ minTime);
                if (time < minTime)
                    minTime = time;
                final String name = String.format("%-80s", calculator.getClass().getSimpleName());
                System.out.println (name + " :    \t" + time + " \t " + relativeTime);
            }
        }
    }

    private static void doFactorisation(FactorisationCalculator calculator, List<Long> numbersToFactorize) {
        for (long number : numbersToFactorize) {
            calculator.getSortedPrimeFactors(number);
        }
    }
}
