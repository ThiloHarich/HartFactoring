package de.harich.thilo.factoring.hart.calculator.educational;

/**
 * This class helps to find solutions of the 'fermat' equation x^2 - n = y^2, by adjusting x by analysing
 * which solutions for x Mod 64 are possible. The values could be found by AnalyzeSolutionsModulus.
 * This handling gives a speedup of factor ~ 2.
 * One branch depending on the last bit of the multiplier just does a bitwise or on the x value.
 * The other branch does some additions and Mod operations on x.
 *
 * More structure in the solutions can be found. For example Mod powers of 3.
 * it seems to be more beneficial to add a Factor of 3 instead of adjusting x mod 3 or 9.
 */
public class Mod32SquareAdjuster extends SquareAdjuster {

    /**
     * Increases x to return the next possible solution on x for x^2 - 4 * multiplier * n = y^2 mod 64
     * Due to performance reasons we give back solutions for this equations modulo a
     * power of 2, since we can determine the solutions just by additions and binary
     * operations.
     * The branches depend on the value multiplier * n mod 8, but not on x.
     * Since n is fixed, the branches only depend on multiplier mod 8
     * -> JIT works best if we iterate over multiplier mod 8 in a deterministic way.
     * here we iterate over multipliers which have the same remainder mod 8.
     * <p>
     * if multiplier is even x must be odd.
     * if multiplier*n == 3 mod 4 -> x = multiplier*n+1 mod 8
     * if multiplier*n == 1 mod 8 -> x = multiplier*n+1 mod 16 or -multiplier*n+1 mod 16
     * if multiplier*n == 5 mod 8 -> x = multiplier*n+1 mod 32 or -multiplier*n+1 mod 32
     *
     * @return the improved x
     */
    @Override
    public long adjustX(long x, long multiplier, long n) {
        if ((multiplier&1)==0)
            return x | 1;
        final long multiplierNPlus1 = multiplier*n+1;
        if ((multiplierNPlus1 & 3) == 0)
        {
            return x + ((multiplierNPlus1 - x) & 7);
        }
        else if ((multiplierNPlus1 & 7) == 2) {
            return x +  calculateAdjustMod(multiplierNPlus1, x, 15);
        }
        return x + calculateAdjustMod(multiplierNPlus1, x, 31);
    }

    protected long calculateAdjustMod(long multiplierNPlus1, long x, int modulusMask) {
        final long adjust1 = (multiplierNPlus1 - x) & modulusMask;
        final long adjust2 = (-multiplierNPlus1 - x) & modulusMask;

        return Math.min(adjust1, adjust2);
    }
}
