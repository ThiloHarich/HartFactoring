package de.harich.thilo.factoring.algorithm.trialdivision;

public class Decomposition {
    public long[] sortedPrimeFactors;
    public long factor = 1;
    public int nextFactorIndex;

    public Decomposition(long[] primeFactors,int nextFactorIndex) {
        this.sortedPrimeFactors = primeFactors;
        this.nextFactorIndex = nextFactorIndex;
    }

    public Decomposition(long[] primeFactors, int nextFactorIndex, long someFactor) {
        this.sortedPrimeFactors = primeFactors;
        this.nextFactorIndex = nextFactorIndex;
        factor = someFactor;
    }

    public boolean onlyHasPrimeFactors(){
        return factor == 1;
    }

    public long[] getSortedPrimeFactorsTrimmed() {
        long [] result = new long[nextFactorIndex];
        System.arraycopy(sortedPrimeFactors, 0, result, 0, nextFactorIndex);
        return result;
    }

    public void addPrimeFactor(long primeFactor) {
        sortedPrimeFactors[nextFactorIndex++] = primeFactor;
    }
}
