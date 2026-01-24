package de.harich.thilo.factoring.trialdivision.education;

/**
 *
 */
public class WheelTrialDivision extends ScalarTrialDivision {


    public WheelTrialDivision() {
    }

    @Override
    public int findSingleFactor(long number, int maxPrimeFactor) {
        if (number <= 3) return (int) number;
        if (number % 2 == 0) return 2;
        if (number % 3 == 0) return 3;

        for (int factor = 5; factor <= maxPrimeFactor; factor += 6) {
            if (factorFound (number, factor)) return factor;
            if (factorFound (number, factor + 2)) return factor + 2;
        }
        return -1;
    }
}

