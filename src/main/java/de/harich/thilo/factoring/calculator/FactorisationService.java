package de.harich.thilo.factoring.calculator;


import de.harich.thilo.factoring.algorithm.trialdivision.LemireIntTrialDivision;
import de.harich.thilo.factoring.algorithm.trialdivision.TrialDivisionAlgorithm;
import de.harich.thilo.factoring.algorithm.hart.HartFactorization;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;

import java.util.Arrays;
import java.util.stream.IntStream;

import static de.harich.thilo.math.VectorMath.isPerfectSquare;

/**
 * A class for calculating the prime factorization of a long number.
 * Not optimized for speed. Uses lambdas to keep the number of lines in the code short.
 */
public class FactorisationService {

    public static final double INV_LOG_2 = 1.0 / Math.log(2);
    private long useTrialDivisionBelowBits = 1L;


    // TODO this might overflow WeakHashMap? or remove completely?
//    Map<Long, List<Factor>> factorizations = new HashMap<>();

    boolean useHartFactorisation = true;

    //    HartFactorizationAlgorithm algorithm = new HartFactorizationAlgorithm(new AdjustXModPow2Calculator());
    LemireIntTrialDivision integerFactorisationAlgorithm = new LemireIntTrialDivision();
    LemireTrialDivision smallFactorsAlgorithm = new LemireTrialDivision();
    HartFactorization biggerFactorsAlgorithm = new HartFactorization();
    private double lemireExponent = -1.0;

    public FactorisationService(int useTrialDivisionBits) {
        if (useTrialDivisionBits > 0){
            useTrialDivisionBelowBits = 1L << useTrialDivisionBits;
        }
    }

    public FactorisationService(double lemireExponent ) {
        if (lemireExponent > 0){
            this.lemireExponent = lemireExponent;
        }
    }


    TrialDivisionAlgorithm getTrialDivisionAlgorithm(long number){
        if (number <= Integer.MAX_VALUE)
            return integerFactorisationAlgorithm;
        if(number < useTrialDivisionBelowBits)
            return smallFactorsAlgorithm;
        return null;
    }

    public long[] getSortedPrimeFactors(long number) {

        TrialDivisionAlgorithm trialDivisionAlgorithm = getTrialDivisionAlgorithm(number);
        if (trialDivisionAlgorithm != null) {
            long[] markedFactors = trialDivisionAlgorithm.findAllPrimeFactors(number, (int) (Math.sqrt(number) + 1));
            return Arrays.stream(markedFactors).filter(factor -> factor != 0).map(Math::abs).toArray();
        }

        // TODO check for existing factorisation!?
        if (lemireExponent < 0)
            lemireExponent = getLemireExponent(Math.log(number) * INV_LOG_2);
        int maxPrimeFactor = (int) Math.pow(number, lemireExponent) + 1;
        long[] primeFactors = smallFactorsAlgorithm.findAllPrimeFactors(number, maxPrimeFactor);
        boolean isNumberFactorized = primeFactors[0] < 0 && - primeFactors[0] != number;
        if (isNumberFactorized){
            primeFactors[0] = - primeFactors[0];
            return Arrays.stream(primeFactors).filter(factor -> factor != 0).map(Math::abs).toArray();
        }
        int index = IntStream.range(0, primeFactors.length)
                .filter(i -> primeFactors[i] < 0)
                .findFirst()
                .orElse(-1); // -1, falls kein negativer Wert gefunden wurde
        long numberWithoutSmallFactors = -primeFactors[index];
        long[] sortedBigPrimeFactors = getBiggerPrimeFactors(numberWithoutSmallFactors, maxPrimeFactor);
        primeFactors[index++] = sortedBigPrimeFactors[0];
        primeFactors[index] = sortedBigPrimeFactors[1];
        return Arrays.stream(primeFactors).filter(factor -> factor != 0).map(Math::abs).toArray();
    }

    // TODO delete
    public static double getLemireExponent(double bits) {
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


    public static int addFactor2(long[] smallPrimeFactorList, int trailingZeros) {
        int index = 0;
        for (int i = 0; i < trailingZeros; i++) {
            smallPrimeFactorList[index++] = 2;
        }
        return index;
    }

    long[] getBiggerPrimeFactors(long number, int maxLowerPrimeFactor) {
        // TODO it can only be two factors List overdose?
        long[] bigPrimeFactorList = new long[2];
        // check for squares
        if (number > 1 && isPerfectSquare(number)){
            long sqrt = (long) Math.sqrt(number);
            bigPrimeFactorList[0] = sqrt;
            bigPrimeFactorList[1] = sqrt;
            return bigPrimeFactorList;
        }

        if (maxLowerPrimeFactor >= Math.cbrt(number)) {
            // if maxLowerPrimeFactor > n^1/3 numberDivFactor is either a prime or has two different prime factors
            // each >= n^1/3
            addPrimeFactorsAboveCubicRoot(number, bigPrimeFactorList);
        }
        return bigPrimeFactorList;
    }
    /**
     * no primes below n^1/3 exist -> only 2 factors exist.
     * Both are >= n^1/3 -> both mut be prime
     */
    public void addPrimeFactorsAboveCubicRoot(long numberToFactorize, long[] bigPrimeFactorList) {
        int index = 0;
        // there is no proof that we can stop at n^1/3. Depends also on the amount of trial division
        int stopAt = (int) (Math.cbrt(numberToFactorize));
        long primeFactorA = biggerFactorsAlgorithm.findSingleFactor(numberToFactorize, stopAt);
        if (primeFactorA > 1){
            bigPrimeFactorList[index++] = primeFactorA;
        }
        long primeFactorB = numberToFactorize / Math.abs(primeFactorA);
        if (primeFactorB != 1){
            bigPrimeFactorList[index] = primeFactorB;
        }
        if (primeFactorB > 0 && primeFactorB < primeFactorA){
            bigPrimeFactorList[0] = primeFactorB;
            bigPrimeFactorList[1] = primeFactorA;
        }
    }
}
