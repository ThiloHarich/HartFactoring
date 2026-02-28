package de.harich.thilo.factoring.algorithm.hart.calculator.prototype.subtract;

/**
 * This class helps to find solutions of the 'fermat' equation x^2 - k*n = y^2.
 * In order to calculate x we have to calculate sqrt(k*n).
 * This implementation calculates the square roots of k in advance, and stores them in a table.
 * This has two advantages
 * - the sqrt itself does not hase to be calculated within the loop.
 * - the arrays enable Vector (MMX, SIMD, AVX) optimizations.
 * Compared with the calculation on primitive 52 bit double datatypes as in NativeDatatypeCalculator
 * the algorithm runs around is 3 times faster on a Celeron N5100 with 4 kernels / 128-Bit and Intel® SSE4.2.
 * This meens, the main speedup come from the Vector support of the cpu and the capabilities of the JIT
 * to use the Vector support. Keep in mind that we do not have changed the code to use any Vector operations.
 *
 * It would be interesting how big the Speedup with AVX2 8 kernels / 256-Bit and
 * AVX-512 16 kernels / 512-Bit is. An additional speedup by a Factor of 4 might be possible.
 *
 * deprecated This is just for demonstration
 */
public class SqrtArraySquareSubtraction
        extends SquareSubtraction {

    //    protected long baseMultiplier = 3*3*5*7;
    long baseMultiplier = 3*3*5*7;

    // 21 BIT should be enough for number around 21*3 = 63 BIT = unsigned long
    protected static final int MULTIPLIERS_LIMIT_50_BIT = 1 << 20;

    // SIMD/AVX operations work on arrays, here it is. Let the JIT do the optimization
    private static double [] sqrtMultiplier;

    public SqrtArraySquareSubtraction(int baseMultiplier) {
        super(baseMultiplier);
        this.baseMultiplier = baseMultiplier;
    }

    public SqrtArraySquareSubtraction() {
        super(315);
    }

    public long findSmallFactor(long numberToFactorize) {
        // TODO this makes the algorithm slow, but why?
//        if (BigInteger.valueOf(numberToFactorize).isProbablePrime(10))
//            return numberToFactorize;
//        if (numberToFactorize < 4)
//            return numberToFactorize;
//        long sqrt = (long) Math.sqrt(numberToFactorize);
//        if (sqrt * sqrt == numberToFactorize)
//            return sqrt;
        return 1L;
    }

    @Override
    public void initialize() {
        if (sqrtMultiplier == null) {
            sqrtMultiplier = new double[MULTIPLIERS_LIMIT_50_BIT];
            for (int i = 1; i < MULTIPLIERS_LIMIT_50_BIT; i++) {
                sqrtMultiplier[i] = Math.sqrt(getMultiplier(i));
            }
        }
    }

    protected double getSqrtMultiplier(int i) {
        return sqrtMultiplier[i];
    }
}
