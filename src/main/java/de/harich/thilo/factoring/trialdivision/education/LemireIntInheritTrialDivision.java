package de.harich.thilo.factoring.trialdivision.education;

/**
 * We use the Lemire trick to check if a int values is dividable by a number.
 * On single register machines performing long or int operations has the same speed.
 * But since 2 int fit into a long value on a processor with vectorisation support
 * like SSE-2, AVX, .. we theoretically can perform twice as much such operations like checking the divideability
 * of a number in one step.
 * In reality, we see a speedup over LemireTrialDivision of 50%.
 * This is the fastest trial division algorithm working only on int values
 */
public class LemireIntInheritTrialDivision extends LemireInheritTrialDivision {

    private static int[] modularInverseInt;
    private static int[] limitIfDividableInt;

    protected void ensureLemireDataExists() {
        if (modularInverseInt == null || modularInverseInt.length != primes.length) {
            modularInverseInt = new int[primes.length];
            limitIfDividableInt = new int[primes.length];
            for (int i = 0; i < primes.length; i++) {
                int prime = primes[i];
                // calculate modular Inverse for 32 bit by Newton
                int inv = modularInverseInt(prime);
                modularInverseInt[i] = inv;
                // Limit = (2^32 - 1) / prime (Unsigned)
                // In Java: Integer.divideUnsigned(-1, prime)
                limitIfDividableInt[i] = Integer.divideUnsigned(-1, prime);
            }
        }
    }

    private static int modularInverseInt(int n) {
        int inverse = n;
        int iterationsFor32Bit = 4;
        for (int i = 0; i < iterationsFor32Bit; i++) { // 4 Iterationen reichen für 32-bit
            inverse *= 2 - n * inverse;
        }
        return inverse;
    }


    public int findSingleFactor(long number, int maxPrimeFactor) {
        // Lemire can not handle even numbers
        if (number % 2 == 0) return 2;
        int maxPrimeFactorIndex = ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();

        for (int i = 1; i <= maxPrimeFactorIndex; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            if (factorFound (number, i))    return getFactor(i);
            // unrolling 8 times for Lemire int seems to be optimal
            if (factorFound (number, ++i))  return getFactor(i);
            if (factorFound (number, ++i))  return getFactor(i);
            if (factorFound (number, ++i))  return getFactor(i);
            if (factorFound (number, ++i))  return getFactor(i);
            if (factorFound (number, ++i))  return getFactor(i);
            if (factorFound (number, ++i))  return getFactor(i);
            if (factorFound (number, ++i))  return getFactor(i);
        }
        return -1;
    }

    public boolean factorFound(long number, int primeIndex) {
        int numberInt = (int) number;
        // multiply number and primeModularInverted (overflow can happen!)
        int product = numberInt * modularInverseInt[primeIndex];
        boolean isNumberDivideableByPrime = Integer.compareUnsigned(product, limitIfDividableInt[primeIndex]) <= 0;
        return isNumberDivideableByPrime;
    }
}
