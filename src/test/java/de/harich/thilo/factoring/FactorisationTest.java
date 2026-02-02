package de.harich.thilo.factoring;

import de.harich.thilo.factoring.trialdivision.LemireTrialDivision;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactorisationTest {

    @Test
    public void testFactorisationCorrectness(){
        Factorization factorization = new Factorization(new LemireTrialDivision());
//        int fromIndex = (int) 1L << 20;
        int fromIndex = 235133;
        int length = 1000;
        for (int i = fromIndex; i < fromIndex + length; i++) {
            // when testing trial division only we need sqrt as limit
//            List<Factor> factors = factorization.getSortedPrimeFactorsWithExponent(i, (int) Math.sqrt(i));
//            long[] factors = factorization.getSortedPrimeFactorsWithExponent(i, true);
            long[] factors = factorization.getSortedPrimeFactors(i, false);
            String factorString = Factorization.toString(factors);
            Long product = Arrays.stream(factors).filter(f -> f > 0).reduce(1L, (a, b) -> a * b);
            String csv = Factorization.toCsvString(factors);
            assertEquals(product, i);
            System.out.println(i + " : " + factorString + "\t csv : " + csv);
        }
    }


    @Test
    public void comparePerformance(){
        Factorization factorization = new Factorization(new LemireTrialDivision());
//        int fromIndex = (int) 1L << 20;
        int fromIndex = 235133;
        int length = 1000;
        for (int i = fromIndex; i < fromIndex + length; i++) {
            // when testing trial division only we need sqrt as limit
//            List<Factor> factors = factorization.getSortedPrimeFactorsWithExponent(i, (int) Math.sqrt(i));
//            long[] factors = factorization.getSortedPrimeFactorsWithExponent(i, true);
            long[] factors = factorization.getSortedPrimeFactors(i, false);
            String factorString = Factorization.toString(factors);
            Long product = Arrays.stream(factors).filter(f -> f > 0).reduce(1L, (a, b) -> a * b);
            String csv = Factorization.toCsvString(factors);
            assertEquals(product, i);
            System.out.println(i + " : " + factorString + "\t csv : " + csv);
        }
    }

}
