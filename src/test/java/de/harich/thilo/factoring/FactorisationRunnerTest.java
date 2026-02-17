package de.harich.thilo.factoring;

import org.junit.jupiter.api.Test;

import static de.harich.thilo.factoring.TestData.makeSemiprimeList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FactorisationRunnerTest {
    FactorisationRunner factorisationRunner = new FactorisationRunner();

    @Test
    public void testRunnerForLongParameter(){
        String[] output = factorisationRunner.getFactorisationOutput(15);
        assertEquals("3 * 5", output[0]);
        assertEquals("3,5", output[1]);
    }
    @Test
    public void testRunnerForLongArrayParameter(){
        int bits = 40;
        int numPrimes = 10;
        long[] numbersToFactorize = makeSemiprimeList(bits, numPrimes, 4);
        String[][] output = factorisationRunner.getFactorisationOutput(numbersToFactorize);
        assertEquals(10, output.length);
    }

    @Test
    public void testRunnerForStringParameterOnNull(){
        // Arrange
        FactorisationRunner factorisationRunner = new FactorisationRunner();
        // Act
        String[][] output = factorisationRunner.getFactorisationOutput((String) null);
        // Assert
        assertEquals("The field of the number(s) to factorize should not be empty", output[0][0]);
        assertNull(output[0][1]);
    }

    @Test
    public void testRunnerForStringParameterOnEmpty(){
        // Arrange
        FactorisationRunner factorisationRunner = new FactorisationRunner();
        // Act
        String[][] output = factorisationRunner.getFactorisationOutput("13321,,1414");
        // Assert
        assertEquals("The string '' does not represent a integer value with at most 19 digits, and such can not be factorized", output[0][0]);
        assertNull(output[0][1]);
    }

    @Test
    public void testRunnerForStringParameterOnNonLong(){
        // Arrange
        FactorisationRunner factorisationRunner = new FactorisationRunner();
        // Act
        String[][] output = factorisationRunner.getFactorisationOutput("13321,zuzewqn,1414");
        // Assert
        assertEquals("The string 'zuzewqn' does not represent a integer value with at most 19 digits, and such can not be factorized", output[0][0]);
        assertNull(output[0][1]);
    }

    @Test
    public void testRunnerOnBigInteger(){
        // Arrange
        FactorisationRunner factorisationRunner = new FactorisationRunner();
        // Act
        String[][] output = factorisationRunner.getFactorisationOutput("678267836217637812698921");
        // Assert
        assertEquals("The string '678267836217637812698921' does not represent a integer value with at most 19 digits, and such can not be factorized", output[0][0]);
        assertNull(output[0][1]);
        assertEquals(1, output.length);
    }

    @Test
    public void testRunnerOnLongMaxvalue(){
        // Arrange
        FactorisationRunner factorisationRunner = new FactorisationRunner();
        // Act
        String[] output = factorisationRunner.getFactorisationOutput(Long.MAX_VALUE);
        // Assert
        assertEquals("7^2 * 73 * 127 * 337 * 92737 * 649657", output[0]);
        assertEquals(2, output.length);
    }
}
