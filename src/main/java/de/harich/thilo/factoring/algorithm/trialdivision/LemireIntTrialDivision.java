package de.harich.thilo.factoring.algorithm.trialdivision;

/**
 * We use the Lemire trick to check if a int values is dividable by a number.
 * On single register machines performing long or int operations has the same speed.
 * But since 2 int fit into a long value on a processor with vectorisation support
 * like SSE-2, AVX, .. we theoretically can perform twice as much such operations like checking the divideability
 * of a number in one step.
 * In reality, we see a speedup over LemireTrialDivision of 50%.
 * This is the fastest trial division algorithm working only on int values
 */
public class LemireIntTrialDivision extends LemireTrialDivision {

    static int[] modularInverse;
    static int[] limitIfDividable;

    protected void ensureLemireDataExists() {
        if (modularInverse == null || modularInverse.length != primes.length) {
            modularInverse = new int[primes.length];
            limitIfDividable = new int[primes.length];
            for (int i = 0; i < primes.length; i++) {
                int prime = primes[i];
                // calculate modular Inverse for 32 bit by Newton
                int inv = modularInverseInt(prime);
                modularInverse[i] = inv;
                // Limit = (2^32 - 1) / prime (Unsigned)
                // In Java: Integer.divideUnsigned(-1, prime)
                limitIfDividable[i] = Integer.divideUnsigned(-1, prime);
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

    public boolean hasPrimeFactor(long number, int primeIndex) {
        int numberInt = (int) number;
        // multiply number and primeModularInverted (overflow can happen!)
        int product = numberInt * modularInverse[primeIndex];
        boolean isNumberDivideableByPrime = Integer.compareUnsigned(product, limitIfDividable[primeIndex]) <= 0;
        return isNumberDivideableByPrime;
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
            if (this.hasPrimeFactor(number, i))    return getPrimeFactor(i);
            // unrolling 8 times for Lemire int seems to be optimal
            // this is 2 times the number of unrolling for the 2 times bigger long based LemireTrialDivision
            if (this.hasPrimeFactor(number, ++i))  return getPrimeFactor(i);
            if (this.hasPrimeFactor(number, ++i))  return getPrimeFactor(i);
            if (this.hasPrimeFactor(number, ++i))  return getPrimeFactor(i);
            if (this.hasPrimeFactor(number, ++i))  return getPrimeFactor(i);
            if (this.hasPrimeFactor(number, ++i))  return getPrimeFactor(i);
            if (this.hasPrimeFactor(number, ++i))  return getPrimeFactor(i);
            if (this.hasPrimeFactor(number, ++i))  return getPrimeFactor(i);
        }
        return -1;
    }
}
