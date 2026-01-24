package de.harich.thilo.factoring;

import java.util.ArrayList;
import java.util.List;

public class Factor implements Comparable<Factor>{

    private static final boolean PRIME_FACTOR = true;
    private static final boolean SOME_FACTOR = false;

    @Override
    public String toString() {
//        return value +  "^" + exponent;
        return value + (exponent > 1 ? "^" + exponent : "");
    }

    public long value;
    public boolean isPrimeFactor;
    public int exponent;

    public Factor(long number, boolean isPrimeFactor, int exponent) {
        this.value = number;
        this.isPrimeFactor = isPrimeFactor;
        this.exponent = exponent;
    }

    public Factor(long number) {
        this(number, SOME_FACTOR, 1);
    }

    // Copy-Constructor für die tiefe Kopie
    public Factor(Factor other) {
        this.value = other.value;
        this.exponent = other.exponent;
        this.isPrimeFactor = other.isPrimeFactor;
    }

    public static Factor createPrimeFactor(long primeFactor) {
        return new Factor(primeFactor, PRIME_FACTOR, 1);
    }

    public static Factor createPrimeFactor(long primeFactor, int exponent) {
        return new Factor(primeFactor, PRIME_FACTOR, exponent);
    }

    public static Factor createFactor(long number) {
        return new Factor(number, SOME_FACTOR, 1);
    }

    public boolean isNonPrimeFactor() {
        return !isPrimeFactor;
    }

    public boolean isPrimeFactor() {
        return isPrimeFactor;
    }

    @Override
    public int compareTo(Factor o) {
        return (int) Math.signum(this.value - o.value);
    }

    public List<Long> getValues() {
        long currentValue = exponent % 2 == 0 ? 1 : value;
        List<Long> values = new ArrayList<>();
        for (int currentExponent = 0; currentExponent < (exponent + 2)/2; currentValue *= value*value, currentExponent++){
            values.add(currentValue);
        }
        return values;
    }
}

