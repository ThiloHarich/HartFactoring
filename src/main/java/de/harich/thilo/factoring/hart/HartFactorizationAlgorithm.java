package de.harich.thilo.factoring.hart;

import de.harich.thilo.factoring.FactorisationAlgorithm;
import de.harich.thilo.factoring.Factorization;
import de.harich.thilo.factoring.Factor;
import de.harich.thilo.factoring.hart.calculator.Mod32TableSquareAdjuster;
import de.harich.thilo.factoring.hart.calculator.MultiplierArraySquareSubtraction;
import de.harich.thilo.factoring.hart.calculator.educational.SquareAdjuster;
import de.harich.thilo.factoring.hart.calculator.educational.SquareSubtraction;
import de.harich.thilo.factoring.trialdivision.TrialDivisionAlgorithm;
import de.harich.thilo.math.BinaryGreatestCommonDivisorEngine;

import java.util.ArrayList;
import java.util.List;

import static de.harich.thilo.factoring.Factorization.isPrimeFactorisation;

/**
 * A clean code Approach for a Hart Factoring algorithm running in O(n^1/3).
 * We try to keep the code as clean as possible and let the JIT compiler of Java do the optimizations.
 * We use arrays intensively, but did not use any Vector specific Operations, but performance should still be goood.
 * The Auto-Vectorization of the compiler seems to work fine here. But the performance can vary, maybe due to
 * the Auto-Vectorization.
 *
 * It tries to solve the equation x^2 - y^2 = 4 * multiplier * n, by trying different multipliers,
 * calculating an x >= sqrt(4 * multiplier * n). Then we determine the value of x^2 - 4 * multiplier * n,
 * and check if this value is a square y^2. If so the gcd(x,y) should be a factor of n.
 *
 * if at least one Factor of size > n^(1/3) exist, and the number is no Square the method findSingleFactor
 * should return a factor of n. When calling findFactors there is a trial division algorithm finding
 * factors < n^1/3 before calling the highly optimized hart algorithm.
 *
 * It should be one of the fastest factoring algorithm for long numbers. Especially for numbers below 50 Bit,
 * with big multipliers (bigger then n^1/3) is should be fast.
 *
 * So we hope to make use of some JIT / Java features. We hope the algorithm can compete with the best algorithms
 * written in more performance oriented languages like C, c++
 *
 * We did not spend much time on
 * - finding lower prime factors
 * - calculating the Prime Factorization of a number
 *
 * This is the Basic Algorithm. It stays the same for all variants.
 * The main performance improvements are due to
 * - using arrays, this should enable Vector (MMX, SIMD, AVX) operations - hoping JIT can do it
 * - the x values were optimized due to some mod 32 arguments.
 * - clever selection of multipliers to be used.
 * - using a lookup table for the x values of the equation
 *
 * There are some more small optimizations concerning
 * - calculating the ceil on double values
 * - filter out bad multipliers of the hart algorithm
 *
 * The details for good performance are in the instances of SquareMultiplierSubtractor/SquareAdjuster.
 *
 * For such n's we use the multiplier. With adjust this range might not be to small -> write a test
 *
 * Ideas which do not improve the performance:
 * - optimize the multipliers by the number of factorizations it will provide (over a bigger number of semiprimes)
 * - use smooth multiplier which relate to many relations p/q of possible factorizations n=p*q
 * - optimize 'x' on other remainders on modulus other than a power of 2
 * - use more filters on the generated multipliers like prime factorization contains bigger exponents or just two factors
 *
 * So there is more structure in the multipliers and 'x', but we are not able to use it.
 * It seems like some multipliers are needed despite some arguments questioning this.
 *
 */
public class HartFactorizationAlgorithm implements FactorisationAlgorithm {


    static final int limitMightHelpFactorisation = 1 << 21;

    TrialDivisionAlgorithm trialDivisionAlgorithm = new TrialDivisionAlgorithm();
    BinaryGreatestCommonDivisorEngine greatestCommonDivisorEngine = new BinaryGreatestCommonDivisorEngine();

    // use decomposition not Inheritance,
    // the Algorithm itself is the same, only the way we calculate the values is different
    SquareSubtraction calculator;

    // and how we adjust the possible values of 'x'
    SquareAdjuster squareAdjuster;

    /**
     * The constructor with best performance
     */
    public HartFactorizationAlgorithm() {
        this.calculator = new MultiplierArraySquareSubtraction(true, 43);
        this.calculator.initialize();

        this.squareAdjuster = new Mod32TableSquareAdjuster();
        this.squareAdjuster.initialize();
    }

    /**
     * For demonstration/testing purposes, where you might define some not so performant Implementations
     */
    public HartFactorizationAlgorithm(SquareSubtraction calculator, SquareAdjuster squareAdjuster) {
        this.calculator = calculator;
        this.calculator.initialize();
//        this.calculator.useFusedMultipleAdd();
        this.squareAdjuster = squareAdjuster;
        this.squareAdjuster.initialize();
    }

    public String getName(){
        return calculator.getName() + "/" + squareAdjuster.getName();
    }

    /**
     * This is the main method for calculating a prime factorisation.
     * The implementation has to return a list of factors such that the absolut value of the elements in the list multiplied together
     * must be the numberToFactorize. The prime factors were marked with a negative sign and should be at the beginning
     * of the list.
     */
    public List<Factor> findFactors(long numberToFactorize) {
        List<Factor> factors = trialDivisionAlgorithm.findFactors(numberToFactorize, (int) Math.cbrt(numberToFactorize));
        if (isPrimeFactorisation(factors))
            return factors;

        try {
            long numberWithoutSmallPrimeFactors = Factorization.getNonPrimeFactorValue(factors);
            long factor = findSingleFactor(numberWithoutSmallPrimeFactors);
            long remainingNumber = numberWithoutSmallPrimeFactors / factor;
            factors.add(new Factor(factor));
            factors.add(new Factor(remainingNumber));

            return factors;
        } catch (final RuntimeException e) {
            handleRuntimeException(numberToFactorize, e);
            // no factor can be found
            return new ArrayList<>();
        }
    }

    public long findSingleFactor(final long numberToFactorize) {
        final long fourN = 4 * numberToFactorize;
        final double sqrt4N = Math.sqrt(fourN);

        // this loop is pretty simple and has no branches except, when we find the solution,
        // this makes it a good candidate for parallel processing. In Java we can let the JIT compiler do the work
        // for us. We can still write clean Java code. In the calculator we (might) have a simple array.
        // VectorApi in Java or using AVX in C is not (much) faster, but the code is really complex.
//        for (int i=1; true ;i++) {
//        final int limitForFactorisation = calculator.getLoopEnd(numberToFactorize);
        for (int i = 1; i < limitMightHelpFactorisation; i++) {

            // create a multiplier which has a high chance to solve the equation x^2 - y^2 = 4 * multiplier * n
            // final might help the JIT to vectorize the arrays
            final long multiplier = calculator.getMultiplier(i);

            // get an x = ceil (sqrt (4 * n * multiplier))
            final long xFirst = calculator.getFirstX(sqrt4N, i);

            // get an x which might have a better chance to solve  y^2 = x^2 - 4 * multiplier * n
            final long x = squareAdjuster.adjustX(xFirst, multiplier, numberToFactorize);

            // we look for y^2 = x^2 - 4 * multiplier * n
            // the number x^2 and multiplier * fourN might exceed 64 bits, but the difference is usually still correct
            // this is the reason we switch to long calculation here.
            final long possibleSquare = x*x - multiplier * fourN;

            // the fastest way to check if a number is a square - if lower Bits are bits of a square - seems to be
            // to calculate the square root, which is usually only supported for double values. So another
            // conversion to double needs to be performed. Support for such conversions, or avoiding in AVX is complex.
            final long y = (long) Math.sqrt(possibleSquare);
            if (y*y == possibleSquare) {
                long greatestCommonDivisor = greatestCommonDivisorEngine.greatestCommonDivisor(x + y, numberToFactorize);
                if (greatestCommonDivisor  > 1 && greatestCommonDivisor < numberToFactorize) {
                    // slowing down the algorithm a little bit, but is needed when using AnalyzeSolutionsModulus
                    calculator.handleSolution(greatestCommonDivisor, i, numberToFactorize, x, multiplier);
                    return greatestCommonDivisor;
                }
            }
        }
        return -1;
    }

    public void handleRuntimeException(long numberToFactorize, RuntimeException e) {
        System.out.println("Failed to factor N =" + numberToFactorize +
                ". " + numberToFactorize + " might have factors < below " + Math.ceil(Math.cbrt(numberToFactorize)) +
                " (n^1/3) -> add handling in findSmallFactor, the array for the stored square roots is too small " +
                "-> increase value of MULTIPLIERS_LIMIT_50_BIT, or the number is a square -> add handling in findSmallFactor");
        System.out.println("Exception is : " + e.getLocalizedMessage());
    }
    public SquareSubtraction getCalculator() {
        return calculator;
    }
}
