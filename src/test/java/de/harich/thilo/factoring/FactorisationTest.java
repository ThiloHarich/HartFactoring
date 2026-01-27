package de.harich.thilo.factoring;

import de.harich.thilo.factoring.trialdivision.LemireTrialDivision;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class FactorisationTest {

    @Test
    public void testFactorisationCorrectness(){
        Factorization factorization = new Factorization(new LemireTrialDivision());
//        int fromIndex = (int) 1L << 20;
        int fromIndex = 150;
        int lenght = 1000;
        for (int i = fromIndex; i < fromIndex + lenght; i++) {
            // when testing trial division only we need sqrt as limit
            List<Factor> factors = factorization.getSortedPrimeFactorsWithExponent(i, (int) Math.ceil(Math.sqrt(i)));
//            List<Factor> factors = factorization.getSortedPrimeFactorsWithExponent(i, (int) Math.ceil(Math.cbrt(i)));
            String factorString = factors.stream().map(Factor::toString).collect(Collectors.joining(" "));
            System.out.println(i + " : " + factorString);
        }
    }
}
