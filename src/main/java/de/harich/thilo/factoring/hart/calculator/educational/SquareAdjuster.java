package de.harich.thilo.factoring.hart.calculator.educational;

/**
 * This class helps finding solutions of the 'fermat' equation x^2 - n = y^2, by adjusting x by some criteria.
 * Here baseline -> no adjustment on x
 */
public class SquareAdjuster {

    public void initialize(){}

    public long adjustX(long x, long multiplier, long n) {
        return x;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}

