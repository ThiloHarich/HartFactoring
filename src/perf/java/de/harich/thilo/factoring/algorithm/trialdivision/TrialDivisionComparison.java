package de.harich.thilo.factoring.algorithm.trialdivision;

import de.harich.thilo.factoring.algorithm.FactorisationAlgorithm;
import de.harich.thilo.factoring.algorithm.trialdivision.baseline.ScalarTrialDivision;
import de.harich.thilo.factoring.algorithm.trialdivision.prototype.*;
import de.harich.thilo.factoring.algorithm.trialdivision.jml.TDiv31Barrett;
import de.harich.thilo.factoring.algorithm.trialdivision.prototype.array.*;
import de.harich.thilo.factoring.algorithm.trialdivision.wheel.Wheel30TrialDivision;
import de.harich.thilo.factoring.algorithm.trialdivision.wheel.Wheel6Reciprocal6TrialDivision;
import de.harich.thilo.factoring.algorithm.trialdivision.wheel.Wheel6TrialDivision;
import de.harich.thilo.math.SmallPrimes;


import static de.harich.thilo.factoring.algorithm.hart.HartFactorisationComparison.logTimings;

public class TrialDivisionComparison {


    public static void main(String[] args) {
        // since LemireIntTrialDivision only works for 31 bits
        compareSemiPrimePerformanceForBits(31);
    }

    /**
     * Run on a Intel(R) Celeron(R) N5100 @ 1.10GHz (1.11 GHz) we see the following timings
     Name of the algorithm                                                        :	absolute time 	 relative to best 	 relative to algorithm above
     LemireIntTrialDivision                                                       :    	8188100 	 1.0	             8.877555808528559E-13
     LemireIntInheritTrialDivision                                                :    	7770900 	 0.949048008695546	 0.949048008695546
     LemireTrialDivision                                                          :    	62707600 	 8.069541494550181	 8.069541494550181
     LemireInheritTrialDivision                                                   :    	11274500 	 1.4508615475684927	 0.1797947936135333
     TDiv31Barrett                                                                :    	17961700 	 2.3114053713212113	 1.593126080979201
     FloatReciprocalTrialDivision                                                 :    	95377600 	 12.273687732437685	 5.31005417081902
     PrimeReciprocalTrialDivision                                                 :    	100314900 	 12.90904528433	     1.0517658234218517
     Wheel30ArrayTrialDivision                                                    :    	88975100 	 11.449780591694656	 0.8869579693545027
     PrimeArrayTrialDivision                                                      :    	100266200 	 12.902778313966207	 1.1269017961204877
     Wheel30TrialDivision                                                         :    	222350500 	 28.613223693523274	 2.217601744157054
     Wheel6Reciprocal6TrialDivision                                               :    	280142900 	 36.050251579611114	 1.2599157636254472
     Wheel6TrialDivision                                                          :    	278606700 	 35.852565339922016	 0.9945163700382912
     ScalarReciprocalTrialDivision                                                :    	836059100 	 107.58845178808117	 3.0008578401021944
     ScalarTrialDivision                                                          :    	842262300 	 108.3867119638652	 1.0074195711762481     */


    public static void compareSemiPrimePerformanceForBits(int bits){
        final int numPrimes = 10000;
        int maxPrime = (int) (1L << (bits /2));
        boolean readFromFile = true;
        final long start = System.currentTimeMillis();
        long[] semiprimes = SmallPrimes.makeSemiPrimesList(bits, numPrimes, readFromFile, .45);
        final long lap1 = System.currentTimeMillis();
        System.out.println("time for making Primes : " + (lap1 - start));

        FactorisationAlgorithm[] algorithms = {

                // For 31 bits algorithms are ordered according to the performance, fastest algorithms first
                new LemireIntTrialDivision(),
                new LemireIntInheritTrialDivision(),
                // not sure why this is slow when LemireIntTrialDivision and or ReciprocalArrayTrialDivision test are is activated
                // It seems like JIT can not support all of them at the same time
                new LemireTrialDivision(),
                new LemireInheritTrialDivision(),
                new TDiv31Barrett(maxPrime),
//                new ReciprocalArrayTrialDivision(maxPrime),
                new FloatReciprocalTrialDivision(maxPrime),
                new PrimeReciprocalTrialDivision(maxPrime),
                new Wheel30ArrayTrialDivision(),
                new PrimeArrayTrialDivision(maxPrime),
                new Wheel30TrialDivision(),
                new Wheel6Reciprocal6TrialDivision(),
                new Wheel6TrialDivision(),
                new ScalarReciprocalTrialDivision(),
                new ScalarTrialDivision(),
        };

        logTimings(lap1, algorithms, semiprimes);
    }

}
