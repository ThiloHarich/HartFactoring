package de.harich.thilo.factoring.calculator;

public interface FactorisationCalculator {
    long[] getSortedPrimeFactors(long number);


//    default long[][] getSortedPrimeFactors(long[] numbers) {
//        long[][] sortedPrimeFactors = new long[numbers.length][];
//        for (int i = 0; i < numbers.length; i++) {
//            sortedPrimeFactors[i] = getSortedPrimeFactors(numbers[i]);
//        }
//        return sortedPrimeFactors;
//    }
}
