package de.harich.thilo.factoring.calculator;


import de.harich.thilo.factoring.algorithm.trialdivision.TrialDivisionAlgorithm;
import de.harich.thilo.factoring.algorithm.hart.HartFactorization;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;

/**
 * A class for calculating the prime factorization of a long number.
 * Not optimized for speed. Uses lambdas to keep the number of lines in the code short.
 * TODO make an CLI interface to factorize a buch of number
 */
public class LemireHartRoughFactorisationCalculator extends LemireHartSmoothFactorisationCalculator {

    // TODO this might overflow WeakHashMap? or remove completely?
//    Map<Long, List<Factor>> factorizations = new HashMap<>();

    boolean useHartFactorisation = true;

    //    HartFactorizationAlgorithm algorithm = new HartFactorizationAlgorithm(new AdjustXModPow2Calculator());
    LemireTrialDivision smallFactorsAlgorithm = new LemireTrialDivision();
    HartFactorization biggerFactorsAlgorithm = new HartFactorization();

    public LemireHartRoughFactorisationCalculator() {
//        List<Factor> factorizationOf1 = new ArrayList<>(List.of());
//        factorizations.put(1L, factorizationOf1);
        smallFactorsAlgorithm = new LemireTrialDivision();
    }

    public LemireHartRoughFactorisationCalculator(TrialDivisionAlgorithm factorisationAlgorithm) {
        this();
        // TODO
//        smallFactorsAlgorithm = factorisationAlgorithm;
        useHartFactorisation = false;
    }

    @Override
    public long[] getSortedPrimeFactors(long number) {
        int maxPrimeFactor = (int) Math.cbrt(number);
        // TODO check for existing factorisation!?
        int[] factorIndices = smallFactorsAlgorithm.findPrimefactorIndices(number, maxPrimeFactor);
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(number);
        long[] sortedPrimeFactors = new long[numberBits + 2];
        long[] numberAndIndex = addSmallFactors(number, factorIndices, sortedPrimeFactors, smallFactorsAlgorithm, maxPrimeFactor);

        if (!useHartFactorisation){
//            return combineIdenticalFactors(sortedPrimeFactors);
            return sortedPrimeFactors;
        }
        long[] sortedBigPrimeFactors = getBiggerPrimeFactors(numberAndIndex[0], maxPrimeFactor);
        int index = (int) numberAndIndex[1];
        sortedPrimeFactors[index++] = sortedBigPrimeFactors[0];
        sortedPrimeFactors[index] = sortedBigPrimeFactors[1];
        return sortedPrimeFactors;
    }



}
