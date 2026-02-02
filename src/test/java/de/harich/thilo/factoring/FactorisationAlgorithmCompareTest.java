package de.harich.thilo.factoring;


import de.harich.thilo.factoring.hart.HartFactorization;
import de.harich.thilo.factoring.hart.calculator.Mod32TableSquareAdjuster;
import de.harich.thilo.factoring.hart.calculator.MultiplierArraySquareSubtraction;
import de.harich.thilo.factoring.hart.calculator.educational.Mod32SquareAdjuster;
import de.harich.thilo.factoring.hart.calculator.educational.SquareAdjuster;
import de.harich.thilo.factoring.hart.calculator.educational.SquareSubtraction;
import de.harich.thilo.factoring.hart.calculator.educational.SqrtArraySquareSubtraction;


import de.harich.thilo.factoring.trialdivision.education.*;
import de.harich.thilo.factoring.trialdivision.LemireTrialDivision;
import de.harich.thilo.math.SmallPrimes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactorisationAlgorithmCompareTest {


    @Test
    public void compareTrialDivisionPerformanceFor41Bit(){
        final int bits = 41	;
        int maxPrime = (int) (1L << (bits /2));
        final int numPrimes = 10000;

        boolean readFromFile = true;
        final long start = System.currentTimeMillis();
        long[] semiprimes = SmallPrimes.makeSemiPrimesList(bits, numPrimes, readFromFile);
        final long lap1 = System.currentTimeMillis();
        System.out.println("time for making Primes : " + (lap1 - start));

        // TODO find out if we can use fma if the call is slower than doing "*" and "+"
        boolean useFusedMultipleAdd = false;
        FactorisationAlgorithm[] algorithms = {


                new LemireTrialDivision(),
//                new FloatReciprocalTrialDivisionAlgorithm(),
                new ReciprocalArrayTrialDivision(),
                new PrimeReciprocalTrialDivision(maxPrime),
                new PrimeArrayTrialDivision(maxPrime)
//                new ScalarTrialDivisionAlgorithm(),
//                new VectorizedTrialDivisionAlgorithm(maxPrime)
        };
        logTimings(lap1, algorithms, semiprimes);
    }
    /**
     * We should see something like
     * #Name of the algorithm                                                        :	absolute time 	 relative to best 	 relative to algorithm above
     * MultiplierArraySquareSubtraction filter/Mod32TableSquareAdjuster             :    	54818200	 1.0000000000000182	 0.025526713591779916
     * MultiplierArraySquareSubtraction/Mod32TableSquareAdjuster                    :    	56766500	 1.0355411159067798	 1.035541115906761
     * MultiplierArraySquareSubtraction/Mod32SquareAdjuster                         :    	78655500	 1.434842807680687	 1.385597139157778
     * SqrtArraySquareSubtraction 315/Mod32TableSquareAdjuster                      :    	84624700	 1.5437336505029633	 1.0758904335996846
     * SqrtArraySquareSubtraction 315/Mod32SquareAdjuster                           :    	83550200	 1.5241324961418201	 0.9873027614868944
     * SquareSubtraction 315/Mod32SquareAdjuster                                    :    	143241400	 2.6130263306712482	 1.7144351539553466
     * MontgomeryTrialDivisionAlgorithm                                             :    	250583100	 4.571166145550284	 1.7493762278224032
     * SquareSubtraction 1/Mod32SquareAdjuster                                      :    	352414900	 6.4287937217932445	 1.4063793607789192
     * SquareSubtraction 315/SquareAdjuster                                         :    	296751500	 5.413375484784349	 0.842051513712956
     * ReciprocalTrialDivisionAlgorithm                                             :    	382739300	 6.98197496451921	 1.2897636574709817
     * SquareSubtraction 1/SquareAdjuster                                           :    	500695400	 9.13374390257267	 1.3081891512055335
     * PrimeDivisionTrialDivisionAlgorithm                                          :    	767366800	 13.998394693733358	 1.5326020570590422
     */
    @Test
    public void comparePerformanceFor41Bit(){
        final int bits = 41	;
        final int numPrimes = 100000;

        boolean readFromFile = true;
        final long start = System.currentTimeMillis();
        long[] numbersToFactorize = SmallPrimes.makeSemiPrimesList(bits, numPrimes, readFromFile);
        final long lap1 = System.currentTimeMillis();
        System.out.println("time for making Primes : " + (lap1 - start));

        boolean useFusedMultipleAdd = false;
        FactorisationAlgorithm[] algorithms = {
                new HartFactorization(new MultiplierArraySquareSubtraction(true, 43), new Mod32TableSquareAdjuster()),
                new HartFactorization(new MultiplierArraySquareSubtraction(false, 43), new Mod32TableSquareAdjuster()),
                new HartFactorization(new MultiplierArraySquareSubtraction(false, 43), new Mod32SquareAdjuster()),
//                new HartFactorizationAlgorithm(new MultiplierSequenceCalculator(false), new FermatXAdjuster()),
                new HartFactorization(new SqrtArraySquareSubtraction(), new Mod32TableSquareAdjuster()),
                new HartFactorization(new SqrtArraySquareSubtraction(), new Mod32SquareAdjuster()),
//                new HartFactorizationAlgorithm(new SqrtArraySquareMultiplierSubtractor(), new SquareAdjuster()),
//                new HartFactorizationAlgorithm(new DifferenceOfSquaresCalculator(315), new FermatMod32Table()),
                new HartFactorization(new SquareSubtraction(315), new Mod32SquareAdjuster()),
                new LemireTrialDivision(),
                new HartFactorization(new SquareSubtraction(1), new Mod32SquareAdjuster()),
                new HartFactorization(new SquareSubtraction(315), new SquareAdjuster()),
//                new HartFactorizationAlgorithm(new DifferenceOfSquaresCalculator(1), new FermatMod32Table()),

                new ReciprocalArrayTrialDivision(),
                new HartFactorization(new SquareSubtraction(1), new SquareAdjuster()),
                new PrimeReciprocalTrialDivision()
        };
        logTimings(lap1, algorithms, numbersToFactorize);
    }

    public static void logTimings(long lap1, FactorisationAlgorithm[] algorithms, long[] numbersToFactorize) {
        final long lap2 = System.currentTimeMillis();
        System.out.println("time for initializing all algorithms : " + (lap2 - lap1));
        long overallMin = Long.MAX_VALUE;
        long minTime = Long.MAX_VALUE;

        System.out.println("Name of the algorithm                                                        :\tabsolute time \t relative to best \t relative to algorithm above");
        for (FactorisationAlgorithm algorithm : algorithms){
            long lastTime = minTime;
            minTime = factorize(algorithm, numbersToFactorize, false);
            double relativeTime = ((double) minTime) / overallMin;
            double relativeToLast = minTime / (lastTime + 0.0);
            final String name = String.format("%-75s", algorithm.getName());
            System.out.println(name + "  :    \t" +  minTime + " \t " + relativeTime + "\t " + relativeToLast);
            if (minTime < overallMin) {
                overallMin = minTime;
            }
        }
    }

    @Test
    public void improvePerformanceFor41Bit() {
        final int bits = 41;
        final int numPrimes = 10000;

        boolean readFromFile = true;
        final long start = System.currentTimeMillis();
        long[] semiprimes = SmallPrimes.makeSemiPrimesList(bits, numPrimes, readFromFile);
        final long lap1 = System.currentTimeMillis();
        System.out.println("time for making Primes : " + (lap1 - start));

        int maxPrimeFactor = (int) (1L << bits/2);
        List<FactorisationAlgorithm> algorithms = List.of(
                new LemireTrialDivision(),
//                new TrialDivisionAlgorithm(maxPrimeFactor)
                new ScalarTrialDivision()
        );

        final long lap2 = System.currentTimeMillis();
        
        System.out.println("time for initializing all algorithms : " + (lap2 - lap1));

        long[] minTime = new long[2];

        int index = 0;
        for (FactorisationAlgorithm algorithm : algorithms){
            minTime[index++] = factorize(algorithm, semiprimes, true);
        }
        double relation = (minTime[0] + 0.0)/minTime[1];
        if (relation < 1){
            relation = 1/relation;
        }
        System.out.println("relation : " + relation);

    }
    public static long factorize(FactorisationAlgorithm factorizer, long[] numbersToFactorize, boolean print) {


        long minTime = Long.MAX_VALUE;

        minTime = Math.min(minTime, factorizeIt(factorizer, numbersToFactorize, print, true));
//        for (int n = 1; n < 32; n+=2) {
//            for (int i = 0; i < 32; i++) {
//                String hit = String.format("%4d", ((Wheel120ArrayTrialDivision) factorizer).hits[n][i]);
//                System.out.print(hit);
//            }
//            System.out.println();
//        }
        minTime = Math.min(minTime, factorizeIt(factorizer, numbersToFactorize, print, false));
        minTime = Math.min(minTime, factorizeIt(factorizer, numbersToFactorize, print, false));
        minTime = Math.min(minTime, factorizeIt(factorizer, numbersToFactorize, print, false));

        return minTime;
    }

    protected static long factorizeIt(final FactorisationAlgorithm algorithm, final long[] numbersToFactorize, boolean print, boolean test) {
        algorithm.findSingleFactor(15);
        final long start = System.nanoTime();
        for (final Long number : numbersToFactorize) {
            long factor = algorithm.findSingleFactor(number);
            if (test){
                long semiprimeMaybe = factor * ((int) (number / factor));
                if (semiprimeMaybe != number)
                    System.out.println();
                assertEquals(semiprimeMaybe, number);
            }
        }

        long time = System.nanoTime() - start;
        final String name = String.format("%-75s", algorithm.getName());
        if (print) System.out.println("time : \t" + name + " \t" + time);
        return time;
    }
}
