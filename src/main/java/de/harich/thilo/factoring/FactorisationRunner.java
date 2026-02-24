package de.harich.thilo.factoring;

import de.harich.thilo.factoring.calculator.*;
import de.harich.thilo.factoring.validation.NumberValidator;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class FactorisationRunner {
    // TODO find out!!!
    private static final int LEMIRE_TRIAL_BETTER_THAN_HART = 35;

    FactorisationCalculator lemireInt = new FactorisationCalculatorLemireInt();
    FactorisationCalculator lemireLong = new LemireFactorisationCalculator();
    FactorisationCalculator lemireHartSmooth = new LemireHartSmoothFactorisationCalculator();
    FactorisationCalculator lemireHartRough = new LemireHartRoughFactorisationCalculator();

    NumberValidator numberValidator = new NumberValidator();

    public String[][] getFactorisationOutput(String numbers){
        if (numberValidator.validate(numbers) != null){
            return new String[][] {{numberValidator.validate(numbers), null}};
        }
        String[] numbersAsString = numbers.split(",");

        long[] numbersAsLong = Arrays.stream(numbersAsString).map(String::trim).mapToLong(Long::parseLong).toArray();
        return getFactorisationOutput(numbersAsLong);
    }
    public String[] getFactorisationOutput(long number){
        long[] factors = getSortedPrimeFactors(number);
        String factorString = toString(factors);
//        Long product = Arrays.stream(factors).filter(f -> f > 0).reduce(1L, (a, b) -> a * b);
        String csv = toCsvString(factors);
        System.out.println(number + " : " + factorString + "\t csv : " + csv);
        return new String[] {factorString, csv};
    }

    public String[][] getFactorisationOutput(long[] numbers){
        String[][] factorisationOutput = new String[numbers.length][];
        long[][] factors = getSortedPrimeFactors(numbers);
        for (int i = 0; i < factors.length; i++) {
            String factorString = toString(factors[i]);
//        Long product = Arrays.stream(factors).filter(f -> f > 0).reduce(1L, (a, b) -> a * b);
            String csv = toCsvString(factors[i]);
            System.out.println(numbers[i] + " : " + factorString + "\t csv : " + csv);
            factorisationOutput[i] = new String[] {factorString, csv};
        }
        return factorisationOutput;
    }

    long[] getSortedPrimeFactors(long number) {
        FactorisationCalculator factorisationCalculator = getBestFactorisationByLength(number);
//        return factorisationCalculator.getSortedPrimeFactors(number);
        return Arrays.stream(factorisationCalculator.getSortedPrimeFactors(number))
                .filter(v -> v != 0)
                .map(Math::abs)
                .toArray();
    }

    long[][] getSortedPrimeFactors(long[] numbers) {
        long[][] sortedPrimeFactorsArray = new long[numbers.length][];
        long firstNumber = numbers[0];
        FactorisationCalculator factorisationCalculator = getBestFactorisationByLength(firstNumber);
        // we might use FactorisationLemireHartRough also in case of FactorisationCalculatorLemireInt
        if (factorisationCalculator instanceof LemireHartSmoothFactorisationCalculator) {
            sortedPrimeFactorsArray[0] = factorisationCalculator.getSortedPrimeFactors(firstNumber);
            long biggestPrime = sortedPrimeFactorsArray[0][sortedPrimeFactorsArray[0].length - 1];
            int biggestPrimeBits = Long.numberOfTrailingZeros(biggestPrime);

            if (biggestPrimeBits > firstNumber / 4) {
                factorisationCalculator = lemireHartRough;
            }
        }
        for (int i = 1; i < numbers.length; i++) {
            sortedPrimeFactorsArray[i] = factorisationCalculator.getSortedPrimeFactors(numbers[i]);
        }
        return sortedPrimeFactorsArray;
    }

    FactorisationCalculator getBestFactorisationByLength(long number){
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(number);
        if (numberBits <= Integer.SIZE)
           return lemireInt;
        if(numberBits < LEMIRE_TRIAL_BETTER_THAN_HART)
            return lemireLong;
        return lemireHartSmooth;
    }

    public static String toCsvString(long[] factors) {
        return Arrays.stream(factors)
                .filter(f -> f > 0).mapToObj(Long::toString).collect(Collectors.joining(","));
    }

    public static String toString (long[] sortedFactors) {
        if (sortedFactors.length == 0 || sortedFactors[0] == 0)
            return "";
        String factorsWithExponent = "";
        long lastFactor = Math.abs(sortedFactors[0]);
        int exponent = 1;

        for (int factorIndex = 1; factorIndex < sortedFactors.length && sortedFactors[factorIndex] != 0; ){
            long factor = Math.abs(sortedFactors[factorIndex]);
            if (factor == lastFactor){
                exponent++;
            }
            else{
                factorsWithExponent += getFactorsWithExponent(lastFactor, exponent) + " * ";
                exponent = 1;
            }
            lastFactor = factor;
            factorIndex++;
        }
        factorsWithExponent += getFactorsWithExponent(lastFactor, exponent);
        return factorsWithExponent;
    }

    private static String getFactorsWithExponent(long lastFactor, int exponent) {
        if (exponent == 1)
            return "" + lastFactor;
        return lastFactor + "^" + exponent;
    }
}
