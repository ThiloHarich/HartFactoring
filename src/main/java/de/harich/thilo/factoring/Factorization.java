package de.harich.thilo.factoring;


import de.harich.thilo.factoring.hart.HartFactorization;
import de.harich.thilo.factoring.trialdivision.LemireTrialDivision;

import java.util.*;
import java.util.stream.Collectors;

import static de.harich.thilo.factoring.Factor.createPrimeFactor;

/**
 * A class for calculating the prime factorization of a long number.
 * Not optimized for speed. Uses lambdas to keep the number of lines in the code short.
 * TODO make an CLI interface to factorize a buch of number
 */
public class Factorization {

    // TODO this might overflow WeakHashMap? or remove completely?
    Map<Long, List<Factor>> factorizations = new HashMap<>();

    boolean useHartFactorisation = true;

    public Factorization() {
        List<Factor> factorizationOf1 = new ArrayList<>(List.of());
        factorizations.put(1L, factorizationOf1);
    }

    public Factorization(TrialDivisionAlgorithm factorisationAlgorithm) {
        this();
        smallFactorsAlgorithm = factorisationAlgorithm;
        useHartFactorisation = false;
    }
    //    HartFactorizationAlgorithm algorithm = new HartFactorizationAlgorithm(new AdjustXModPow2Calculator());
    TrialDivisionAlgorithm smallFactorsAlgorithm = new LemireTrialDivision();
    HartFactorization biggerFactorsAlgorithm = new HartFactorization();

    public List<Factor> getSortedPrimeFactorsWithExponent(long number, int maxPrimeFactor) {
        int[] factors = smallFactorsAlgorithm.findFactors(number, maxPrimeFactor);
        List<Factor> smallPrimeFactorList = new ArrayList<>();
        number = addSmallFactors(number, factors, smallPrimeFactorList, smallFactorsAlgorithm);

        if (!useHartFactorisation){
            return combineIdenticalFactors(smallPrimeFactorList);
        }
        List<Factor> bigPrimeFactors = getBiggerPrimeFactors(number, maxPrimeFactor);
        smallPrimeFactorList.addAll(bigPrimeFactors);
        Collections.sort(smallPrimeFactorList);
        return combineIdenticalFactors(smallPrimeFactorList);
    }

    private static long addSmallFactors(long number, int[] factors, List<Factor> smallPrimeFactorList, TrialDivisionAlgorithm smallFactorsAlgorithm) {
        for (int factorIndex : factors){
            if (factorIndex == -1)
                break;
            do {
                smallPrimeFactorList.add(new Factor(smallFactorsAlgorithm.getFactor(factorIndex)));
                int factor  = smallFactorsAlgorithm.getFactor(factorIndex);
                // TODO we might speed this up by using reciprocals
                number = number / factor;
                // factorFound is fast for Lemire trial division, but we
            } while ((smallFactorsAlgorithm.factorFound (number, factorIndex)));
        }
        return number;
    }

    private List<Factor> getBiggerPrimeFactors(long number, int maxLowerPrimeFactor) {
        // TODO it can only be two factors List overdose?
        List<Factor> bigPrimeFactorList = new ArrayList<>();
        // the following steps only help for small factors, but why not
        // try to find the remaining factor in the factorisation cache
        if (factorizations.containsKey(number)){
            // do a deep copy of Factors, to not mess up the already computed factorization
            // eigentlich müssen wir nur an dem Factor mit value == factor den exponenten erhöhen
            List<Factor> deepCopyOfExistingFactors = factorizations.get(number).stream()
                    .map(Factor::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            bigPrimeFactorList.addAll(deepCopyOfExistingFactors);
            return bigPrimeFactorList;
        }
        // try to find the remaining factor in the primes created in the MontgomeryTrialDivisionAlgorithm
//        int foundIndex = Arrays.binarySearch(LemireTrialDivision.primes, (int) number);
//        if (foundIndex > 0) {
//            Factor foundPrime = new Factor(LemireTrialDivision.primes[foundIndex]);
//            bigPrimeFactorList.add(foundPrime);
//            return bigPrimeFactorList;
//        }
        // check for squares
        if (isPerfectSquare(number)){
            long sqrt = (long) Math.sqrt(number);
            bigPrimeFactorList.add(Factor.createPrimeFactor(sqrt, 2));
            return bigPrimeFactorList;
        }
        List<Factor> biggerFactors = biggerFactorsAlgorithm.findFactors(number);

        if (maxLowerPrimeFactor >= Math.cbrt(number))
            // if maxLowerPrimeFactor > n^1/3 numberDivFactor is either a prime or has two different prime factors
            // each >= n^1/3
            bigPrimeFactorList.addAll(biggerFactors);
        else{
            // recursive call
            List<Factor> primeFactors = getPrimeFactors(biggerFactors);
            bigPrimeFactorList.addAll(primeFactors);
        }
        return bigPrimeFactorList;
    }


    public static List<Factor> combineIdenticalFactors(List<Factor> sortedFactors) {
        if (sortedFactors.isEmpty())
            return sortedFactors;
        List<Factor> factorsWithExponent = new ArrayList<>();
        Factor firstFactor = sortedFactors.getFirst();
        long lastFactor = firstFactor.value;
        int exponent = firstFactor.exponent;

        for (int factorIndex = 1; factorIndex < sortedFactors.size(); ){
            long factor = sortedFactors.get(factorIndex).value;
            if (factor == lastFactor){
                exponent++;
            }
            else{
                factorsWithExponent.add(createPrimeFactor(lastFactor, exponent));
                exponent = 1;
            }
            lastFactor = factor;
            factorIndex++;
        }
        factorsWithExponent.add(createPrimeFactor(lastFactor, exponent));
        return factorsWithExponent;
    }

    public static List<Factor> getPrimeFactors(List<Factor> factors) {
        return factors.stream().filter(Factor::isPrimeFactor).collect(Collectors.toList());
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

    public List<Factor> addFactor(List<Factor> smallFactorization, long factor) {
        // 1. Suche nach dem Index des Faktors unter Berücksichtigung von "value"
        // Wir nutzen eine einfache Schleife oder binarySearch
        int insertIndex = -1;

        for (int i = 0; i < smallFactorization.size(); i++) {
            if (smallFactorization.get(i).value == factor) {
                // Faktor gefunden: Exponent erhöhen und Methode verlassen
                smallFactorization.get(i).exponent++;
                return smallFactorization;
            }
            if (smallFactorization.get(i).value > factor) {
                // Wir haben die Stelle gefunden, an der der Faktor stehen müsste
                insertIndex = i;
                break;
            }
        }

        // 2. Wenn wir hier landen, existiert der Faktor noch nicht
        Factor newFactor = new Factor(factor);

        if (insertIndex == -1) {
            // Der neue Faktor ist größer als alle vorhandenen (oder Liste war leer)
            smallFactorization.add(newFactor);
        } else {
            // An der richtigen sortierten Stelle einfügen
            smallFactorization.add(insertIndex, newFactor);
        }
        return smallFactorization;
    }

    public static boolean isPrimeFactorisation(List<Factor> smallFactors) {
        return smallFactors.stream().allMatch(Factor::isPrimeFactor);
    }

    public static long getNonPrimeFactorValue(List<Factor> factors) {
        return factors.stream().filter(Factor::isNonPrimeFactor).findFirst().map(f -> f.value).orElse(Long.valueOf(1L));
    }

    public static List<Long> getNonPrimeFactorValues(List<Factor> factors) {
        return factors.stream().filter(Factor::isNonPrimeFactor).map(f -> f.value).collect(Collectors.toList());
    }

}
