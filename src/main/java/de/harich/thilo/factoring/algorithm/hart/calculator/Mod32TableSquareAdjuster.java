package de.harich.thilo.factoring.algorithm.hart.calculator;

import de.harich.thilo.factoring.algorithm.hart.calculator.prototype.adjust.Mod32SquareAdjuster;

/**
 * Stores the difference of x to the first possible x in a table for odd multipliers.
 * So it cuts down the 4 different branches of FermatMod32 to 2. Additionally, no more calculations have to be done
 * It is just a lookup in a 32 * 32 = 1024 entries table.
 *
 * We see around 20% improvement over FermatMod32 (without sorting), when multipliers are stored in an array.
 *
 * We still se more structure in the values for
 */
public class Mod32TableSquareAdjuster extends Mod32SquareAdjuster {

    public static final int MODULUS_FOR_ADJUST_X = 32;

    protected final long[][] adjustX = new long [MODULUS_FOR_ADJUST_X][MODULUS_FOR_ADJUST_X];

    public void initialize() {
        // TODO theoretically we only need to store odd multiplierN
        for (int multiplierN = 0; multiplierN < MODULUS_FOR_ADJUST_X; multiplierN++) {
            for (int x = 0; x < MODULUS_FOR_ADJUST_X; x++) {
                final long multiplierNPlus1 = multiplierN +1;
                if ((multiplierNPlus1 & 3) == 0)
                {
                    adjustX[multiplierN][x] = ((multiplierNPlus1 - x) & 7);
                }
                else if ((multiplierNPlus1 & 7) == 2) {
                    adjustX[multiplierN][x] = calculateAdjustMod(multiplierNPlus1, x, 15);
                }
                else {
                    adjustX[multiplierN][x] = calculateAdjustMod(multiplierNPlus1, x, 31);
                }
            }
        }
    }

    @Override
    public long adjustX(long x, long multiplier, long n) {
        // we have sorted the multipliers such that the two cases were called in an alternating order
        // such that JIT can optimize the loop
        if ((multiplier & 1) == 0) {
            return x | 1;
        }
        long multiplierN = multiplier * n;
        return x + adjustX[(int) multiplierN & 31][(int) x & 31];
    }
}
