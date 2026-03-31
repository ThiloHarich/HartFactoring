package de.harich.thilo.math;

import jakarta.annotation.Nonnull;

public class MillerRabin {

    /**
     * Fast Miller-Rabin primality test for long values.
     * For n < 3,825,123,056,546,413,051, it is sufficient to test a = 2, 3, 5, 7, 11, 13, 17, 19, 23.
     */
    public static boolean isProbablePrime(long n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;

        long d = n - 1;
        int s = Long.numberOfTrailingZeros(d);
        d >>= s;

        // Bases for Miller-Rabin deterministic test for 64-bit longs
        // For n < 2^64, these bases are sufficient
        long[] bases = getBases(n);

        for (long a : bases) {
            if (n <= a) break;
            if (!millerRabinTest(n, a, d, s)) return false;
        }
        return true;
    }

    private static long[] getBases(long n) {
        if (n <= 2047)
            return new long[]{2};
        if (n <= 1373653)
            return new long[]{2,3};
        if (n <= 4759123141L)
            return new long[]{2, 7, 61};
        if (n <= 2152302898747L)
            return new long[]{2, 3, 5, 7, 11};
        if (n <= 341550071728321L)
            return new long[]{2, 3, 5, 7, 11, 13, 17};
        return new long[]{2, 3, 5, 7, 11, 13, 17, 19, 23};
    }

    private static boolean millerRabinTest(long n, long a, long d, int s) {
        long x = power(a, d, n);
        if (x == 1 || x == n - 1) return true;
        for (int r = 1; r < s; r++) {
            x = multiply(x, x, n);
            if (x == n - 1) return true;
        }
        return false;
    }

    private static long power(long base, long exp, long mod) {
        long res = 1;
        base %= mod;
        while (exp > 0) {
            if (exp % 2 == 1) res = multiply(res, base, mod);
            base = multiply(base, base, mod);
            exp /= 2;
        }
        return res;
    }

    private static long multiply(long a, long b, long mod) {
//        if (mod <= 1_000_000_000L) return (a * b) % mod;
        if (mod <= Integer.MAX_VALUE) return (a * b) % mod;

        // Use BigInteger-like multiplication to avoid overflow for large longs
        // Or use the property (a * b) % mod = (a * b - (long)((double)a * b / mod) * mod + mod) % mod
        // But for simplicity and safety:
        long res = 0;
        a %= mod;
        while (b > 0) {
            if (b % 2 == 1) res = (res + a) % mod;
            a = (a + a) % mod;
            b /= 2;
        }
        return res;
    }
}
