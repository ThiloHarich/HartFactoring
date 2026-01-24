package de.harich.thilo.factoring.trialdivision;

import de.harich.thilo.factoring.FactorisationAlgorithm;
import de.harich.thilo.factoring.trialdivision.education.*;
import de.harich.thilo.math.SmallPrimes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.harich.thilo.factoring.FactorisationComparisonTest.logTimings;

public class TrialDivisionComparisonTest {

    /**
     * For small inputs < 31 bit you should see the following results
     *
     * Name of the algorithm                                                        :	absolute time 	 relative to best 	 relative to algorithm above
     * FastIntLemireTrialDivision                                                   :    	8083900 	 1.0000000000001237	 0.0037643592822199498
     * MontgomeryTrialDivision                                                      :    	10325200 	 1.2772547903859865	 1.2772547903858287
     * ReciprocalTrialDivision                                                      :    	21700900 	 2.6844592337860047	 2.1017413706272032
     * PrimeAvoidCastTrialDivision                                                  :    	39420100 	 4.876371553335009	 1.8165191305429729
     * PrimeArrayRoundTrialDivision                                                 :    	98904200 	 12.234713442770474	 2.508978921920543
     * WheelTrialDivision                                                           :    	279124700 	 34.52847016910582	 2.8221723647731847
     * ScalarTrialDivision                                                          :    	840686300 	 103.99513848515	 3.0118663808684794
     */
    @Test
    public void comparePerformanceFor31Bit(){
        final int bits = 31	;
        int maxPrime = (int) (1L << (bits /2));
        final int numPrimes = 100000;

        boolean readFromFile = true;
        final long start = System.currentTimeMillis();
        long[] semiprimes = SmallPrimes.makeSemiPrimesList(bits, numPrimes, readFromFile);
        final long lap1 = System.currentTimeMillis();
        System.out.println("time for making Primes : " + (lap1 - start));

        boolean useFusedMultipleAdd = false;
        List<FactorisationAlgorithm> algorithms = List.of(

                new LemireIntTrialDivision(),
                new LemireTrialDivision(),
                // sometimes Reciprocal slows down by a factor of 3, maybe JIT is not working here
                new ReciprocalTrialDivision(maxPrime),
                new PrimeAvoidCastTrialDivision(maxPrime),
                new PrimeArrayRoundTrialDivision(maxPrime)
//                new WheelTrialDivision(),
//                new ScalarTrialDivision()

        );
        // warmup
        logTimings(lap1, algorithms, semiprimes);
        System.out.println(" -------- real results after warmup phase ----------------------");
        logTimings(lap1, algorithms, semiprimes);
    }
}
