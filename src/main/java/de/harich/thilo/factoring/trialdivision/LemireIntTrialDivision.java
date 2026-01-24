package de.harich.thilo.factoring.trialdivision;

import de.harich.thilo.factoring.trialdivision.education.PrimeAvoidCastTrialDivision;

/**
 * We use the Lemire trick to check if a int values is dividable by a number.
 * On single register machines performing long or int operations has the same speed.
 * But since 2 int fit into a long value on a processor with vectorisation support
 * like SSE-2, AVX, .. we theoretically can perform twice as much such operations like checking the divideability
 * of a number in one step.
 * In reality we see a speedup over LemireTrialDivision of 30%.
 * This is the
 */
public class LemireIntTrialDivision extends PrimeAvoidCastTrialDivision {

    static int[] modularInverse;
    static int[] limits;

    private static void ensureLemireDataExists() {
        if (modularInverse == null || modularInverse.length != primes.length) {
            modularInverse = new int[primes.length];
            limits = new int[primes.length];
            for (int i = 0; i < primes.length; i++) {
                int prime = primes[i];
                // Modulare Inverse für 32-bit (Newton-Verfahren)
                int inv = modularInverseInt(prime);
                modularInverse[i] = inv;
                // Limit = (2^32 - 1) / prime (Unsigned)
                // In Java: Integer.divideUnsigned(-1, prime)
                limits[i] = Integer.divideUnsigned(-1, prime);
            }
        }
    }

    private static int modularInverseInt(int n) {
        int inverse = n;
        for (int i = 0; i < 4; i++) { // 4 Iterationen reichen für 32-bit
            inverse *= 2 - n * inverse;
        }
        return inverse;
    }

    public int findSingleFactor(long numberToFactorize, int maxPrimeFactor) {
        // Montgomery can not handle even numbers
        if (numberToFactorize % 2 == 0) return 2;
        ensurePrimesExist(maxPrimeFactor);
        ensureLemireDataExists();
        // adjusting the loop end to fit in the vectorisation lanes seems not to help
//        int loopEndForVectorisation = primes.length & ~15;
//        for (int i = 1; i < loopEndForVectorisation; i++) {
        for (int i = 1; i < primes.length; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            // you might just copy the lines at the end to enable more lanes e.g. for AVX-512
            // TODO how to support different AVX ? For SSE-2 4 but not 8 statements are optimal
            if (factorFound (numberToFactorize, i))    return primes[i];
            // unrolling seems to not work here
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
//            if (factorFound (numberToFactorize, ++i))  return primes[i];
        }
        return -1;
    }

    protected boolean factorFound(long numberToFactorize, int i) {
        int nInt = (int) numberToFactorize;
        int product = nInt * modularInverse[i];
        return Integer.compareUnsigned (product, limits[i]) <= 0;
    }
}
