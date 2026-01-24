package de.harich.thilo.factoring;


import de.harich.thilo.factoring.hart.HartFactorization;
import de.harich.thilo.factoring.trialdivision.LemireTrialDivision;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static de.harich.thilo.factoring.Factor.createPrimeFactor;

/**
 * A class for calculating the prime factorization of a long number.
 * Not optimized for speed. Uses lambdas to keep the number of lines in the code short.
 * TODO make an CLI interface to factorize a buch of number
 */
public class Factorization {
    Map<Long, List<Factor>> factorizations = new HashMap<>();

    public Factorization() {
        List<Factor> factorizationOf1 = new ArrayList<>(List.of());
        factorizations.put(1L, factorizationOf1);
    }
    //    HartFactorizationAlgorithm algorithm = new HartFactorizationAlgorithm(new AdjustXModPow2Calculator());
    LemireTrialDivision smallFactorsAlgorithm = new LemireTrialDivision();
    HartFactorization biggerFactorsAlgorithm = new HartFactorization();

    public static boolean isPrimeFactorisation(List<Factor> smallFactors) {
        return smallFactors.stream().allMatch(Factor::isPrimeFactor);
    }

    public static long getNonPrimeFactorValue(List<Factor> factors) {
        return factors.stream().filter(Factor::isNonPrimeFactor).findFirst().map(f -> f.value).orElse(Long.valueOf(1L));
    }

    public static List<Long> getNonPrimeFactorValues(List<Factor> factors) {
        return factors.stream().filter(Factor::isNonPrimeFactor).map(f -> f.value).collect(Collectors.toList());
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

    /**
     * The count of the numbers possible by multiplying subset's of the prime numbers of the number together.
     * @param number
     * @return
     */
    public double numberOfCombinations(int number, int maxPrimeFactor){
        List<Factor> factors = getSortedPrimeFactorsWithExponent(number, maxPrimeFactor);
        return factors.stream()
                .filter(factor -> factor.value != 2)
                .map(factor -> (factor.exponent + 1))
                .reduce(1, (a, b) -> (Integer) (a * b));
//        return factors.stream().map(factor -> 2.0).reduce(1.0, (a, b) -> a * b);
    }

    public List<Double> getAllFractions(int number, int maxPrimeFactor){
        List<Factor> factors = getSortedPrimeFactorsWithExponent(number, maxPrimeFactor);
        List<Factor> oddFactors = factors.stream().filter(factor -> factor.value != 2).collect(Collectors.toList());
//        List<Double> allFractionsUnsorted = getAllFractions(factors);
        List<Double> allFractionsUnsorted = getAllFractions(oddFactors);
        allFractionsUnsorted.sort(Comparator.reverseOrder());
        return allFractionsUnsorted;
    }

    private List<Double> getAllFractions(List<Factor> factors) {
        if (factors.isEmpty()){
            return new ArrayList<>(List.of(Double.valueOf(1.0)));
        }
        List<Double> allFractions = new ArrayList<>();
        Factor firstFactor = factors.removeFirst();
//        Factor firstFactor = factors.remove(0);
//        Factor firstFactor = null;
        List<Long> values = firstFactor.getValues();
        List<Double> otherFractions = getAllFractions(factors);
        for (Long factor : values){
            otherFractions.stream()
                    .map(fraction -> fraction * factor)
                    .forEach(allFractions::add);
            if (factor != 1) {
                otherFractions.stream()
                        .map(fraction -> fraction / factor)
                        .forEach(allFractions::add);
            }
        }
        return allFractions;
    }

    public List<Factor> getSortedPrimeFactorsWithExponent(int number, int maxPrimeFactor) {
        List<Factor> factors = getPrimeFactors(number, maxPrimeFactor);
        Collections.sort(factors);
        return combineIdenticalFactors(factors);
    }


    private List<Factor> getPrimeFactors(long number, int maxLowerPrimeFactor) {
        if (maxLowerPrimeFactor > 10000 && BigInteger.valueOf(number).isProbablePrime(10)) {
            return new ArrayList<>(List.of(createPrimeFactor(number)));
        }
        long[] factors = smallFactorsAlgorithm.findFactors(number, maxLowerPrimeFactor);
        List<Factor> smallPrimeFactorList = new ArrayList<>();
        number = addSmallFactors(number, factors, smallPrimeFactorList);
        // the following steps only help for small factors, but why not
        // try to find the remaining factor in the factorisation cache
        if (factorizations.containsKey(number)){
            // do a deep copy of Factors, to not mess up the already computed factorization
            // eigentlich müssen wir nur an dem Factor mit value == factor den exponenten erhöhen
            List<Factor> deepCopyOfExistingFactors = factorizations.get(number).stream()
                    .map(Factor::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            smallPrimeFactorList.addAll(deepCopyOfExistingFactors);
            return smallPrimeFactorList;
        }
        // try to find the remaining factor in the primes created in the MontgomeryTrialDivisionAlgorithm
        int foundIndex = Arrays.binarySearch(LemireTrialDivision.primes, (int) number);
        if (foundIndex > 0) {
            Factor foundPrime = new Factor(LemireTrialDivision.primes[foundIndex]);
            smallPrimeFactorList.add(foundPrime);
            return smallPrimeFactorList;
        }
        // check for squares
        if (isPerfectSquare(number)){
            long sqrt = (long) Math.sqrt(number);
            smallPrimeFactorList.add(Factor.createPrimeFactor(sqrt, 2));
            return smallPrimeFactorList;
        }
        List<Factor> biggerFactors = biggerFactorsAlgorithm.findFactors(number);

        if (maxLowerPrimeFactor >= Math.cbrt(number))
            // if maxLowerPrimeFactor > n^1/3 numberDivFactor is either a prime or has two different prime factors
            // each >= n^1/3
            smallPrimeFactorList.addAll(biggerFactors);
        else{
            // recursive call
            List<Factor> primeFactors = getPrimeFactors(biggerFactors);
            smallPrimeFactorList.addAll(primeFactors);
        }
        return smallPrimeFactorList;
    }

    private static long addSmallFactors(long number, long[] factors, List<Factor> smallPrimeFactorList) {
        for (long factor : factors){
            if (factor == 0)
                break;
            boolean goOn = false;
            do {
                // TODO use reciprocal
                long numberDivFactor = number / factor;
                long numberMaybe = numberDivFactor * factor;
                if (number == numberMaybe) {
                    smallPrimeFactorList.add(new Factor(factor));
                    number = numberDivFactor;
                    goOn = true;
                }
                else {
                    goOn = false;
                }
            } while (goOn);
        }
        return number;
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
}
