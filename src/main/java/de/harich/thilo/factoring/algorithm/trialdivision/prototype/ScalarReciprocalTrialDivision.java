package de.harich.thilo.factoring.algorithm.trialdivision.prototype;

import de.harich.thilo.factoring.algorithm.trialdivision.baseline.ScalarTrialDivision;

public class ScalarReciprocalTrialDivision extends ScalarTrialDivision {

    // calculating the reciprocal and casting is faster than doing a division,
    // we have to add a value to avoid casting problems but still ~ 20% faster
    public long numberDivFactor(long numberToFactorize, int factorIndex) {
        double pInverse = 1.0/ factorIndex;
        return (long) (numberToFactorize * pInverse);
    }

}
