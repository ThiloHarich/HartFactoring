package de.harich.thilo.factoring.hart;


import de.harich.thilo.factoring.FactorisationAlgorithm;
import de.harich.thilo.factoring.hart.calculator.Mod32TableSquareAdjuster;
import de.harich.thilo.factoring.hart.calculator.MultiplierArraySquareSubtraction;
import de.harich.thilo.factoring.hart.calculator.educational.Mod32SquareAdjuster;
import de.harich.thilo.factoring.hart.calculator.educational.SquareAdjuster;
import de.harich.thilo.factoring.hart.calculator.educational.SquareSubtraction;
import de.harich.thilo.factoring.hart.calculator.educational.SqrtArraySquareSubtraction;

import de.harich.thilo.factoring.trialdivision.TrialDivisionAlgorithm;
import de.harich.thilo.math.SmallPrimes;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HartFactorisationComparisonTest {

    /**
     * We should see something like
     * Name of the algorithm                                                        :	absolute time 	 relative to best 	 relative to algorithm above
     * MultiplierArraySquareMultiplierSubtractor filter/Mod32TableSquareAdjuster    :    	43288700	 1.000000000000023	 0.020157871777265274
     * MultiplierArraySquareMultiplierSubtractor/Mod32TableSquareAdjuster           :    	48261400	 1.1148729345071835	 1.1148729345071577
     * MultiplierArraySquareMultiplierSubtractor/Mod32SquareAdjuster                :    	48229300	 1.1141314014974142	 0.9993348721752788
     * SqrtArraySquareMultiplierSubtractor 315/Mod32SquareAdjuster                  :    	70153200	 1.620589206883127	 1.454576367477861
     * SquareMultiplierSubtractor 315/Mod32SquareAdjuster                           :    	137257000	 3.170735087909851	 1.9565322750779721
     * SquareMultiplierSubtractor 315/SquareAdjuster                                :    	289841000	 6.695534862446936	 2.1116664359559074
     * SquareMultiplierSubtractor 1/Mod32SquareAdjuster                             :    	225341000	 5.205538627863743	 0.7774641958867103
     * SquareMultiplierSubtractor 1/SquareAdjuster                                  :    	504768300	 11.66050955561178	 2.240019792226004
     */
    @Test
    public void comparePerformanceFor41Bit(){
        final int bits = 41	;
        final int numPrimes = 10000;

        boolean readFromFile = true;
        final long start = System.currentTimeMillis();
        long[] semiprimes = SmallPrimes.makeSemiPrimesList(bits, numPrimes, readFromFile);
        final long lap1 = System.currentTimeMillis();
        System.out.println("time for making Primes : " + (lap1 - start));

        boolean useFusedMultipleAdd = false;
        List<FactorisationAlgorithm> algorithms = List.of(
                new HartFactorizationAlgorithm(new MultiplierArraySquareSubtraction(true, 43), new Mod32TableSquareAdjuster()),
                new HartFactorizationAlgorithm(new MultiplierArraySquareSubtraction(false, 43), new Mod32TableSquareAdjuster()),
                new HartFactorizationAlgorithm(new MultiplierArraySquareSubtraction(false, 43), new Mod32SquareAdjuster()),
//                new HartFactorizationAlgorithm(new MultiplierSequenceCalculator(false), new FermatXAdjuster()),
                new HartFactorizationAlgorithm(new SqrtArraySquareSubtraction(), new Mod32TableSquareAdjuster()),
                new HartFactorizationAlgorithm(new SqrtArraySquareSubtraction(), new Mod32SquareAdjuster()),
//                new HartFactorizationAlgorithm(new SqrtArraySquareMultiplierSubtractor(), new SquareAdjuster()),
//                new HartFactorizationAlgorithm(new DifferenceOfSquaresCalculator(315), new FermatMod32Table()),
                new HartFactorizationAlgorithm(new SquareSubtraction(315), new Mod32SquareAdjuster()),
                new HartFactorizationAlgorithm(new SquareSubtraction(315), new SquareAdjuster()),
//                new HartFactorizationAlgorithm(new DifferenceOfSquaresCalculator(1), new FermatMod32Table()),
                new HartFactorizationAlgorithm(new SquareSubtraction(1), new Mod32SquareAdjuster()),
                new HartFactorizationAlgorithm(new SquareSubtraction(1), new SquareAdjuster()),
                new TrialDivisionAlgorithm()
        );
        final long lap2 = System.currentTimeMillis();
        System.out.println("time for initializing all algorithms : " + (lap2 - lap1));
        long overallMin = Integer.MAX_VALUE;
        long minTime = Integer.MAX_VALUE;

        System.out.println("Name of the algorithm                                                        :\tabsolute time \t relative to best \t relative to algorithm above");
        for (FactorisationAlgorithm algorithm : algorithms){
            long lastTime = minTime;
            minTime = factorize(algorithm, semiprimes);
            if (minTime < overallMin)
                overallMin = minTime;
            double relativeTime = minTime / (overallMin - 0.000001);
            double relativeToLast = minTime / (lastTime + 0.0);
            final String name = String.format("%-75s", algorithm.getName());
            System.out.println(name + "  :    \t" +  minTime + "\t " + relativeTime + "\t " + relativeToLast);
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


    }
    public static long factorize(FactorisationAlgorithm factorizer, long[] semiprimes) {


        long minTime = Integer.MAX_VALUE;

        minTime = Math.min(minTime, factorizeIt(factorizer, semiprimes));
        minTime = Math.min(minTime, factorizeIt(factorizer, semiprimes));
        minTime = Math.min(minTime, factorizeIt(factorizer, semiprimes));

        return minTime;
    }

    protected static long factorizeIt(final FactorisationAlgorithm factorizer1, final long[] semiprimes) {
        final long start = System.nanoTime();
        for (final Long semiprime : semiprimes) {
            factorizer1.findSingleFactor(semiprime);
        }

        return System.nanoTime() - start;
    }
}
