package de.harich.thilo.factoring.service;


import de.harich.thilo.factoring.algorithm.ecm.TinyEcm64;
import de.harich.thilo.factoring.algorithm.trialdivision.Decomposition;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireIntTrialDivision;
import de.harich.thilo.factoring.algorithm.hart.HartFactorization;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static de.harich.thilo.factoring.algorithm.FactorisationAlgorithm.NO_FACTOR_FOUND;
import static de.harich.thilo.factoring.service.FactorisationType.*;
import static de.harich.thilo.math.VectorMath.isPerfectSquare;

/**
 * A class for calculating the prime factorization of a long number.
 * Not optimized for speed. Uses lambdas to keep the number of lines in the code short.
 */
public class FactorisationService {

    // since the initialization of the algorithms uses time and space we want to do it only once!!!
    private static final LemireIntTrialDivision integerFactorisationAlgorithm = new LemireIntTrialDivision();
    private static final LemireTrialDivision smallFactorsAlgorithm = new LemireTrialDivision();
    private static final HartFactorization biggerFactorsAlgorithm = new HartFactorization();
    private static final TinyEcm64 ellipticCurveMethod = new TinyEcm64(true);

    public final FactorisationType factorisationType;

    // theoretically we only have to do trial division up to n^1/3, but than hart has to
    // search longer than n^1/3. This seems to be the optimal/good value (combination)
    private double trialDivisionExponent = .35;

    public FactorisationService(FactorisationType factorisationType) {
        this.factorisationType = factorisationType;
    }
    public FactorisationService(FactorisationType factorisationType, double trialDivisionExponent) {
        this.factorisationType = factorisationType;
        this.trialDivisionExponent = trialDivisionExponent;
    }

    LemireTrialDivision getTrialDivisionAlgorithm(long number){
        if (number <= Integer.MAX_VALUE)
            return integerFactorisationAlgorithm;
        return smallFactorsAlgorithm;
    }

    public long[] getSortedPrimeFactors(long number) {

        // first do trial division and return factors if number is completely factorized
        LemireTrialDivision trialDivisionAlgorithm = getTrialDivisionAlgorithm(number);
        int maxPrimeFactor = stopTrialDivisionAt(number);
        Decomposition decomposition = trialDivisionAlgorithm.findAllPrimeFactors(number, maxPrimeFactor);
        if (decomposition.onlyHasPrimeFactors()){
            return decomposition.getSortedPrimeFactorsTrimmed();
        }

        if (factorisationType == ELLIPTIC_CURVE_METHOD) {
            return getSortedPrimeFactorsByECM(number);
        }
        // hart
        addHartPrimeFactors(decomposition);

        return decomposition.getSortedPrimeFactorsTrimmed();
    }

    private int stopTrialDivisionAt(long number) {
        if (factorisationType == ELLIPTIC_CURVE_METHOD)
            return 1 << 10;
        if (factorisationType == TRIAL_DIVISION_AND_HART)
            return (int) Math.pow(number, trialDivisionExponent) + 1;
        // default search for all factors
        return (int) Math.sqrt(number) + 1;
    }

    void addHartPrimeFactors(Decomposition decomposition) {
        long number = decomposition.factor;
        // check for squares
        if (number > 1 && isPerfectSquare(number)){
            long sqrt = (long) Math.sqrt(number);
            decomposition.addPrimeFactor(sqrt);
            decomposition.addPrimeFactor(sqrt);
            decomposition.factor = 1;
            return;
        }

        // there is no proof that we can stop at n^1/3. Depends also on the amount of trial division
        int stopAt = (int) (Math.cbrt(number));
        long primeFactorA = biggerFactorsAlgorithm.findSingleFactor (number, stopAt);
        long primeFactorB = number / Math.abs(primeFactorA);

        if (primeFactorA != NO_FACTOR_FOUND) {
            decomposition.addPrimeFactor(Math.min(primeFactorA, primeFactorB));
        }
        decomposition.addPrimeFactor (Math.max(primeFactorA, primeFactorB));
    }


    public long[] getSortedPrimeFactorsByECM(long number) {
        List<Long> primeFactors = new ArrayList<>();
        List<Long> uncategorizedFactors = new ArrayList<>();
        List<Long> compositeFactors = new ArrayList<>();

        uncategorizedFactors.add(number);
        while (true) {
            // resolve untested factors
            while (!uncategorizedFactors.isEmpty()) {
                long uncategorizedFactor = uncategorizedFactors.removeFirst();
                // TODO can we do primability check with array
                int bitCount = Long.bitCount(uncategorizedFactor);
                if (BigInteger.valueOf(uncategorizedFactor).isProbablePrime(bitCount)) {
                    // The uncategorizedFactor is probable prime. In exceptional cases this prediction may be wrong and uncategorizedFactor composite
                    // -> then we would falsely predict uncategorizedFactor to be prime. BPSW is known to be exact for arguments <= 64 bit.
                    //LOG.debug(uncategorizedFactor + " is probable prime.");
                    primeFactors.add(uncategorizedFactor);
                } else {
                    compositeFactors.add(uncategorizedFactor);
                }
            }

            // factor composite factors; iteration needs to be fail-safe against element addition and removal
            while (true) {
                if (compositeFactors.isEmpty()) {
                    if (uncategorizedFactors.isEmpty()) {
                        // TODO avoid streams and use Decomposition
                        return primeFactors.stream().mapToLong(i -> i).toArray();
                    }
                    // else there are still untested factors
                    break;
                }
                long compositeFactor = compositeFactors.removeFirst();

//                long factor1 = ellipticCurveMethod.findSingleFactor(BigInteger.valueOf(compositeFactor)).longValue();
//                if (factor1 > 1 && factor1 < compositeFactor) {
//                    // We found a factor, but here we cannot know if it is prime or composite
//                    uncategorizedFactors.add(factor1);
//                    uncategorizedFactors.add(compositeFactor / factor1);
//                }
            }
        }
    }

    public String getName(){
        String name = factorisationType.getName();
        if (trialDivisionExponent > .35){
            name += trialDivisionExponent;
        }
        return name;
    }

}
