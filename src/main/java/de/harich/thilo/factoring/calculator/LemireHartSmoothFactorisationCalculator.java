package de.harich.thilo.factoring.calculator;


import de.harich.thilo.factoring.algorithm.trialdivision.TrialDivisionAlgorithm;
import de.harich.thilo.factoring.algorithm.hart.HartFactorization;
import de.harich.thilo.factoring.algorithm.trialdivision.LemireTrialDivision;

import java.util.stream.IntStream;

/**
 * A class for calculating the prime factorization of a long number.
 * Not optimized for speed. Uses lambdas to keep the number of lines in the code short.
 * TODO make an CLI interface to factorize a buch of number
 */
public class LemireHartSmoothFactorisationCalculator implements FactorisationCalculator {

    // TODO this might overflow WeakHashMap? or remove completely?
//    Map<Long, List<Factor>> factorizations = new HashMap<>();

    boolean useHartFactorisation = true;

    //    HartFactorizationAlgorithm algorithm = new HartFactorizationAlgorithm(new AdjustXModPow2Calculator());
    LemireTrialDivision smallFactorsAlgorithm = new LemireTrialDivision();
    HartFactorization biggerFactorsAlgorithm = new HartFactorization();

    public LemireHartSmoothFactorisationCalculator() {
//        List<Factor> factorizationOf1 = new ArrayList<>(List.of());
//        factorizations.put(1L, factorizationOf1);
        smallFactorsAlgorithm = new LemireTrialDivision();
    }

    public LemireHartSmoothFactorisationCalculator(TrialDivisionAlgorithm factorisationAlgorithm) {
        this();
        // TODO
//        smallFactorsAlgorithm = factorisationAlgorithm;
        useHartFactorisation = false;
    }

    @Override
    public long[] getSortedPrimeFactors(long number) {
        // TODO check for existing factorisation!?
        int maxPrimeFactor = (int) Math.cbrt(number);
        long[] primeFactors = smallFactorsAlgorithm.findAllPrimeFactors(number, maxPrimeFactor);
        boolean isNumberFactorized = primeFactors[0] < 0 && - primeFactors[0] != number;
        if (isNumberFactorized){
            primeFactors[0] = - primeFactors[0];
            return primeFactors;
        }
        int index = IntStream.range(0, primeFactors.length)
                .filter(i -> primeFactors[i] < 0)
                .findFirst()
                .orElse(-1); // -1, falls kein negativer Wert gefunden wurde
        long numberWithoutSmallFactors = -primeFactors[index];
        long[] sortedBigPrimeFactors = getBiggerPrimeFactors(numberWithoutSmallFactors, maxPrimeFactor);
        primeFactors[index++] = sortedBigPrimeFactors[0];
        primeFactors[index] = sortedBigPrimeFactors[1];
        return primeFactors;

    }

    static long[] addSmallFactors(long number, int[] factorIndices, long[] smallPrimeFactorList,
                                  TrialDivisionAlgorithm smallFactorsAlgorithm, int maxPrimeFactor) {
        // since we have factored out all number below sqrt(number) the remaining number is a prime
        boolean remainderMustBeAPrime = (maxPrimeFactor + 1L) * (maxPrimeFactor + 1L) >= number;
        int trailingZeros = Long.numberOfTrailingZeros(number);
        number = number >> trailingZeros;
        int index = addFactor2(smallPrimeFactorList, trailingZeros);
        for (int factorIndex : factorIndices){
            if (factorIndex == -1) {
                if (number != 1 && remainderMustBeAPrime){
                    smallPrimeFactorList[index++] = number;
                }
                break;
            }
            do {
                long primeFactor = smallFactorsAlgorithm.getPrimeFactor(factorIndex);
                smallPrimeFactorList[index++] = primeFactor;
                // TODO we might speed this up by using reciprocals
                number = number / primeFactor;
                // factorFound is fast for Lemire trial division
            } while ((smallFactorsAlgorithm.hasPrimeFactor(number, factorIndex)));
        }
        return new long [] {number, index};
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
        // the following steps only help for small factors, but why not
        // try to find the remaining factor in the factorisation cache
//        if (factorizations.containsKey(number)){
//            // do a deep copy of Factors, to not mess up the already computed factorization
//            // eigentlich müssen wir nur an dem Factor mit value == factor den exponenten erhöhen
//            List<Factor> deepCopyOfExistingFactors = factorizations.get(number).stream()
//                    .map(Factor::new)
//                    .collect(Collectors.toCollection(ArrayList::new));
//            bigPrimeFactorList.addAll(deepCopyOfExistingFactors);
//            return bigPrimeFactorList;
//        }
        // try to find the remaining factor in the primes created in the MontgomeryTrialDivisionAlgorithm
//        int foundIndex = Arrays.binarySearch(LemireTrialDivision.primes, (int) number);
//        if (foundIndex > 0) {
//            Factor foundPrime = new Factor(LemireTrialDivision.primes[foundIndex]);
//            bigPrimeFactorList.add(foundPrime);
//            return bigPrimeFactorList;
//        }
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
            biggerFactorsAlgorithm.addPrimeFactorsAboveCubicRoot(number, bigPrimeFactorList);
        }
        return bigPrimeFactorList;
    }



    public static boolean isPerfectSquare(long n) {
        if (n < 0) return false;
        // Die letzten 4 Bits einer Quadratzahl in Hex sind nur 0, 1, 4, 9
        long h = n & 0xF;
        if (h > 9) return false;
        if (h != 2 && h != 3 && h != 5 && h != 6 && h != 7 && h != 8) {
            long t = (long) Math.sqrt(n);
            return t * t == n;
        }
        return false;
    }


}
