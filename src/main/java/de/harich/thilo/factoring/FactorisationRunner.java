package de.harich.thilo.factoring;

import de.harich.thilo.factoring.data.Factorisation;
import de.harich.thilo.factoring.service.*;
import de.harich.thilo.factoring.validation.NumberValidator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.harich.thilo.factoring.service.FactorisationType.TRIAL_DIVISION_AND_HART;


@Component
public class FactorisationRunner {

    private final FactorisationService factorisationService;
    private final NumberValidator numberValidator;

    public FactorisationRunner() {
        this.factorisationService = new FactorisationService(TRIAL_DIVISION_AND_HART);
        this.numberValidator = new NumberValidator();
    }

    public List<Factorisation> getFactorisationOutput(String numbers){
        if (numberValidator.validate(numbers) != null){
            return List.of(new Factorisation(numberValidator.validate(numbers), null));
        }
        String[] numbersAsString = numbers.split(",");

        long[] numbersAsLong = Arrays.stream(numbersAsString).map(String::trim).mapToLong(Long::parseLong).toArray();
        return getFactorisationOutput(numbersAsLong);
    }

    public List<Factorisation> getFactorisationOutput(long number){
        long startTime = System.currentTimeMillis();
        long[] factors = getSortedPrimeFactors(number);
        long duration = System.currentTimeMillis() - startTime;
        
        String factorString = toString(factors);
        String csv = toCsvString(factors);
        System.out.println(number + " : " + factorString + "\t csv : " + csv + " duration: " + duration + "ms");
        return List.of(new Factorisation(factorString, csv, duration));
    }

    public  List<Factorisation> getFactorisationOutput(long[] numbers){
        List<Factorisation> factorisationOutput = new ArrayList<>();
        for (long number : numbers) {
            long startTime = System.currentTimeMillis();
            long[] factors = getSortedPrimeFactors(number);
            long duration = System.currentTimeMillis() - startTime;

            String factorString = toString(factors);
            String csv = toCsvString(factors);
            System.out.println(number + " : " + factorString + "\t csv : " + csv + " duration: " + duration + "ms");
            factorisationOutput.add(new Factorisation(factorString, csv, duration));
        }
        return factorisationOutput;
    }

    long[] getSortedPrimeFactors(long number) {
        return Arrays.stream(factorisationService.getSortedPrimeFactors(number))
                .filter(v -> v != 0)
                .map(Math::abs)
                .toArray();
    }

    long[][] getSortedPrimeFactors(long[] numbers) {
        long[][] sortedPrimeFactorsArray = new long[numbers.length][];

        for (int i = 0; i < numbers.length; i++) {
            sortedPrimeFactorsArray[i] = factorisationService.getSortedPrimeFactors(numbers[i]);
        }
        return sortedPrimeFactorsArray;
    }


    public static String toCsvString(long[] factors) {
        return Arrays.stream(factors)
                .filter(f -> f > 0).mapToObj(Long::toString).collect(Collectors.joining(","));
    }

    public static String toString (long[] sortedFactors) {
        if (sortedFactors.length == 0 || sortedFactors[0] == 0)
            return "";
        String factorsWithExponent = "";
        long lastFactor = sortedFactors[0];
        int exponent = 0;

        for (long factor :  sortedFactors){
            if (factor == lastFactor){
                exponent++;
            }
            else{
                factorsWithExponent += getFactorsWithExponent(lastFactor, exponent) + " * ";
                exponent = 1;
            }
            lastFactor = factor;
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
