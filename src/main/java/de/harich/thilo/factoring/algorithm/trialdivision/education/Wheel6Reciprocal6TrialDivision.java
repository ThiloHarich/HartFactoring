package de.harich.thilo.factoring.algorithm.trialdivision.education;

public class Wheel6Reciprocal6TrialDivision extends Wheel6TrialDivision {

    // calculating the reciprocal and casting is faster than doing a division,
    // we have to add a value to avoid casting problems but still ~ 26% faster
    public long numberDivFactor(long numberToFactorize, int factorIndex) {
        double pInverse = 1.0/ factorIndex;
//        return (long) ((numberToFactorize * pInverse) + ROUND_DOUBLE);
        return (long) (numberToFactorize * pInverse);
    }
}
