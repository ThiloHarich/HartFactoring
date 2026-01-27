package de.harich.thilo.factoring.trialdivision;

import de.harich.thilo.factoring.FactorisationAlgorithm;
import de.harich.thilo.factoring.trialdivision.education.*;
import de.harich.thilo.factoring.trialdivision.jml.TDiv31Barrett;
import de.harich.thilo.math.SmallPrimes;
import org.junit.jupiter.api.Test;

import static de.harich.thilo.factoring.FactorisationComparisonTest.logTimings;

public class TrialDivisionComparisonTest {

    /**
     * Run on a Intel(R) Celeron(R) N5100 @ 1.10GHz (1.11 GHz) we see the following timings
     * Name of the algorithm                                                        :	absolute time 	 relative to best 	 relative to algorithm above
     * LemireIntTrialDivision                                                       :    	10183200 	 1.1040647562854389E-12	 1.1040647562854389E-12
     * LemireTrialDivision                                                          :    	12385100 	 1.2162286903920183	 1.2162286903920183
     * ReciprocalArrayTrialDivision                                                 :    	20601600 	 2.0230968654254067	 1.6634181395386392
     * PrimeReciprocalAvoidRoundTrialDivision                                       :    	40194000 	 3.9470893235917983	 1.9510135135135136
     * PrimeReciprocalRoundTrialDivision                                            :    	108008700 	 10.606557860004713	 2.6871846544260336
     * ReciprocalArrayRoundTrialDivision                                            :    	118304400 	 11.617605467829366	 1.0953228767682603
     * PrimeArrayTrialDivision                                                      :    	220417500 	 21.64520975724723	 1.863138649111952
     * WheelReciprocalTrialDivision                                                 :    	227142200 	 22.305581742477806	 1.0305089205711888
     * WheelTrialDivision                                                           :    	276406900 	 27.1434224998036	 1.2168892438305168
     * ScalarReciprocalTrialDivision                                                :    	680833100 	 66.85846295859848	 2.4631552251409063
     * ScalarTrialDivision                                                          :    	834465100 	 81.94527260586064	 1.2256529537121506
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
        FactorisationAlgorithm[] algorithms = {

                new LemireIntTrialDivision(),
                new LemireIntInheritTrialDivision(),
                new LemireTrialDivision(),
                new LemireInheritTrialDivision(),
                new TDiv31Barrett(maxPrime),
                new ReciprocalArrayTrialDivision(maxPrime),
//                new FloatReciprocalTrialDivision(maxPrime),
//                new PrimeReciprocalTrialDivision(maxPrime),
//                new PrimeArrayTrialDivision(maxPrime),
//                new WheelReciprocalTrialDivision(),
//                new WheelTrialDivision(),
//                new ScalarReciprocalTrialDivision(),
//                new ScalarTrialDivision(),
        };


        // warmup
        logTimings(lap1, algorithms, semiprimes);
        System.out.println(" -------- real results after warmup phase ----------------------");
        logTimings(lap1, algorithms, semiprimes);
    }

    @Test
    public void comparePerformanceFor41Bit(){
        final int bits = 41	;
        int maxPrime = (int) (1L << (bits /2));
        final int numPrimes = 10000;

        boolean readFromFile = true;
        final long start = System.currentTimeMillis();
        long[] semiprimes = SmallPrimes.makeSemiPrimesList(bits, numPrimes, readFromFile);
        final long lap1 = System.currentTimeMillis();
        System.out.println("time for making Primes : " + (lap1 - start));

        boolean useFusedMultipleAdd = false;
        FactorisationAlgorithm[] algorithms = {

//                new LemireIntTrialDivision(),
                new LemireTrialDivision(),
                new ReciprocalArrayTrialDivision(maxPrime),
//                new FloatReciprocalAvoidTrialDivision(maxPrime),
                new PrimeReciprocalTrialDivision(maxPrime),
                new PrimeArrayTrialDivision(maxPrime),

        };


        // warmup
        logTimings(lap1, algorithms, semiprimes);
//        System.out.println(" -------- real results after warmup phase ----------------------");
//        logTimings(lap1, algorithms, semiprimes);
    }

}
