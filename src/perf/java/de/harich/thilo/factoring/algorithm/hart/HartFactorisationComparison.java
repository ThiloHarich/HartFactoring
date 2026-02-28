package de.harich.thilo.factoring.algorithm.hart;

import de.harich.thilo.factoring.TestData;
import de.harich.thilo.factoring.algorithm.FactorisationAlgorithm;
import de.harich.thilo.factoring.algorithm.hart.calculator.Mod32TableSquareAdjuster;
import de.harich.thilo.factoring.algorithm.hart.calculator.MultiplierArraySquareSubtraction;
import de.harich.thilo.factoring.algorithm.hart.calculator.prototype.adjust.Mod32SquareAdjuster;
import de.harich.thilo.factoring.algorithm.hart.calculator.prototype.adjust.SquareAdjuster;
import de.harich.thilo.factoring.algorithm.hart.calculator.prototype.subtract.SqrtArraySquareSubtraction;
import de.harich.thilo.factoring.algorithm.hart.calculator.prototype.subtract.SquareSubtraction;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;

import static java.lang.Math.pow;


public class HartFactorisationComparison {


    // a Value which steers the estimated time the test will take
    public static final long RUNNING_TIME = (1L << 29);

    public static void main(String[] args) {
        comparePerformance();
    }

    /**
     * Input n^a
     * running lemire O(n^a / (a*log(n))
     * running hart O (n^1/3)
     * n^a / (a*log(n) = n^1/3  | log
     * a * n - log(a * log(n) = 1/3 * n
     * a = 1/3 + log(a)/n + log(n)/n
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

    public static void comparePerformance(){
        final int bits = 25 ;
        // here we try to find the point where Lemire and Hart have the same running time.
        // this is the point where we should switch form Lemire to Hart. Hart is independent of the size of the
        // factors
        double primeExponent = getPrimeExponent(bits);

        final int numPrimes = (int) pow(2.0, bits * primeExponent * .6);
        final long start = System.currentTimeMillis();
        long[] numbersToFactorize = TestData.makeSemiprimeList(bits, numPrimes, primeExponent);
        final long lap1 = System.currentTimeMillis();
        System.out.println("bits: " + bits + " primeExponent: " + primeExponent + " numPrimes: " + numPrimes);
        System.out.println("time for making Primes : " + (lap1 - start));

        FactorisationAlgorithm[] algorithms = {
                new LemireTrialDivision(),
                new HartFactorization(new MultiplierArraySquareSubtraction(true, 43), new Mod32TableSquareAdjuster()),
                new HartFactorization(new MultiplierArraySquareSubtraction(false, 43), new Mod32TableSquareAdjuster()),
                new HartFactorization(new MultiplierArraySquareSubtraction(false, 43), new Mod32SquareAdjuster()),
                new HartFactorization(new SqrtArraySquareSubtraction(), new Mod32TableSquareAdjuster()),
                new HartFactorization(new SqrtArraySquareSubtraction(), new Mod32SquareAdjuster()),
                new HartFactorization(new SquareSubtraction(315), new Mod32SquareAdjuster()),
                new HartFactorization(new SquareSubtraction(1), new Mod32SquareAdjuster()),
                new HartFactorization(new SquareSubtraction(315), new SquareAdjuster()),
                new HartFactorization(new SquareSubtraction(1), new SquareAdjuster())
        };
        logTimings(lap1, algorithms, numbersToFactorize);
    }

    private static double getPrimeExponent(int bits) {
        if (bits >= 50) return 0.39;
        if (bits >= 40) return interpolate(bits, 40, 50, 0.40, 0.39);
        if (bits >= 30) return interpolate(bits, 30, 40, 0.42, 0.40);
        if (bits >= 25) return interpolate(bits, 25, 30, 0.45, 0.42);
        if (bits >= 20) return interpolate(bits, 20, 25, 0.50, 0.45);
        return 0.50;
    }

    private static double interpolate(double x, double x1, double x2, double y1, double y2) {
        return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
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
            double relativeTime = overallMin == Long.MAX_VALUE ? 1 : ((double) minTime) / overallMin;
            double relativeToLast = minTime / (lastTime + 0.0);
            final String name = String.format("%-75s", algorithm.getName());
            System.out.println(name + "  :    \t" +  minTime + " \t " + relativeTime + "\t " + relativeToLast);
            if (minTime < overallMin) {
                overallMin = minTime;
            }
        }
    }

    public static long factorize(FactorisationAlgorithm factorizer, long[] numbersToFactorize, boolean print) {
        long minTime = Long.MAX_VALUE;

        minTime = Math.min(minTime, factorizeIt(factorizer, numbersToFactorize, print, true));
        minTime = Math.min(minTime, factorizeIt(factorizer, numbersToFactorize, print, false));
        minTime = Math.min(minTime, factorizeIt(factorizer, numbersToFactorize, print, false));
        minTime = Math.min(minTime, factorizeIt(factorizer, numbersToFactorize, print, false));

        return minTime;
    }

    protected static long factorizeIt(final FactorisationAlgorithm algorithm, final long[] numbersToFactorize, boolean print, boolean test) {
        algorithm.findSingleFactor(15);
        final long start = System.nanoTime();
        double totalFactorisations = RUNNING_TIME / Math.pow(numbersToFactorize[0], 0.4);

        for (int i = 0; i < totalFactorisations; ) {
            for (int j = 0; j < numbersToFactorize.length && i < totalFactorisations; j++, i++) {
                long number = numbersToFactorize[j];
                long factor = algorithm.findSingleFactor(number);
                if (test) {
                    long semiprimeMaybe = factor * (number / factor);
                    if (semiprimeMaybe != number) {
                        System.out.println("Error: " + number + " != " + factor + " * " + (number / factor));
                    }
                }
            }
        }

        long time = System.nanoTime() - start;
        final String name = String.format("%-75s", algorithm.getName());
        if (print) System.out.println("time : \t" + name + " \t" + time);
        return time;
    }
}
