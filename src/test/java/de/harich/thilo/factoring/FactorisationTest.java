package de.harich.thilo.factoring;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class FactorisationTest {

    @Test
    public void testFactorisationCorrectness(){
        Factorization factorization = new Factorization();
//        int fromIndex = (int) 1L << 20;
        int fromIndex = 150;
        int lenght = 1000;
        for (int i = fromIndex; i < fromIndex + lenght; i++) {
            List<Factor> factors = factorization.getSortedPrimeFactorsWithExponent(i, (int) Math.ceil(Math.cbrt(i)));
            String factorString = factors.stream().map(Factor::toString).collect(Collectors.joining(" "));
            System.out.println(i + " : " + factorString);
        }
    }
}
