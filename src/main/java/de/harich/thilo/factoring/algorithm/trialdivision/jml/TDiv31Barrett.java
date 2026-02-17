package de.harich.thilo.factoring.algorithm.trialdivision.jml;

import de.harich.thilo.factoring.algorithm.trialdivision.education.PrimeArrayTrialDivision;

public class TDiv31Barrett extends PrimeArrayTrialDivision {

    protected static final int NUM_PRIMES_FOR_31_BIT_TDIV = 4793;

    private long[] pinv;
    public TDiv31Barrett() {
        super();
    }

    public TDiv31Barrett(int maxPrimeFactor) {
        super(maxPrimeFactor);
        ensureReciprocalsExist();
    }

    private void ensureReciprocalsExist() {
        pinv = new long[NUM_PRIMES_FOR_31_BIT_TDIV];
        for (int i=0; i<NUM_PRIMES_FOR_31_BIT_TDIV; i++) {
            int p = primes[i];
            pinv[i] = (1L<<32)/p;
        }
    }

    @Override
    public long findSingleFactor(long number) {
        int N = (int) number;
        if (N<0) N = -N; // sign does not matter
        if (N<4) return 1; // prime
        if ((N&1)==0) return 2; // N even

        // if N is odd and composite then the loop runs maximally up to prime = floor(sqrt(N))
        // unroll the loop
        int i=1;
        int unrolledLimit = NUM_PRIMES_FOR_31_BIT_TDIV-8;
        for ( ; i<unrolledLimit; i++) {
            if ((1 + (int) ((N*pinv[i])>>32)) * primes[i] == N) return primes[i];
            if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
            if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
            if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
            if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
            if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
            if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
            if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
        }
        for ( ; i<NUM_PRIMES_FOR_31_BIT_TDIV; i++) {
            if ((1 + (int) ((N*pinv[i])>>32)) * primes[i] == N) return primes[i];
        }
        // otherwise N is prime
        return 1;
    }
}
