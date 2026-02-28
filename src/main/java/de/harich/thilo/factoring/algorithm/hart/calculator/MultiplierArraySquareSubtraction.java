package de.harich.thilo.factoring.algorithm.hart.calculator;

import de.harich.thilo.factoring.algorithm.hart.calculator.prototype.subtract.SqrtArraySquareSubtraction;
import de.harich.thilo.math.SmallPrimes;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * This SquareSubtractor is free on using the multipliers. Instead of using a fixed multiplier 315, we use
 * different Multiplier Sequences.
 * This change speeds up the algorithm by a factor of ~ 2.5.
 *
 *
 * We have to use the optimizeRemainderArrays method to arrange the multipliers in a good order for the loop.
 * Unfortunately JIT compiler can not always optimize the code (even with the same input) in the same way.
 * We see difference of 30% in execution time -> there might be more work to do
 *
 * The multipliers exist out of many small primes. All include the following multiplier 2*2*3*3*5.
 * The multipliers itself were created in an initialization step, and were stored in an array.
 *
 * Additionally, we pull in more small primes and create different Multiplier Sequences of the form multiplier * i.
 * The multiplier sequences itself do not create some multipliers were we know that it does not lead to many
 * solutions of the equation x^2 - 4*k*n = y^2.
 * We use the following criteria to skipping some bad multipliers :
 * - The size of the number
 * - if 'i' is a prime
 * - a Mod 81 argument
 *
 * TODO analyze if/how allocating memory has an effect
 */
public class MultiplierArraySquareSubtraction extends SqrtArraySquareSubtraction {

    // base multiplier used in all multiplier sequences
    public static final long _3_3_5 = 3 * 3 * 5;

    protected int numberOfRemaindersMultiplierN;

    protected long[] multiplier;
    // overwrite from SqrtArraySquareMultiplierSubtractor
    private double [] sqrtMultiplier;
    boolean filterBadMultipliers;

    // the initialization time and running time are affected by the size of the number to be factorized,
    // since it increases the arrays to be initialized and stored.
    int maxBitsOfNumberToFactorize;

    public MultiplierArraySquareSubtraction(boolean filterBadMultipliers, int maxBitsOfNumberToFactorize) {
        this.filterBadMultipliers = filterBadMultipliers;
        this.maxBitsOfNumberToFactorize = maxBitsOfNumberToFactorize;
        // the number of different cases of kn or multiplier * numberToFactorise in the main loop.
        // for SquareAdjuster and Mod32SquareAdjuster we have different Cases modulus 8.
        // So 8 is here the number that works for all implementations.
        // for AVX-512 bigger values like 32 might be better
        // For Mod32TableSquareAdjuster there are only 2 cases -> numberOfRemaindersMultiplierN = 2 should work as well
        // even without sorting JIT seems to be able to use Vector stuff, but more seldom (?)
        numberOfRemaindersMultiplierN = 8;
    }

    public MultiplierArraySquareSubtraction() {
    }

    public void initialize() {
        multiplier = new long[MULTIPLIERS_LIMIT_50_BIT];
        sqrtMultiplier = new double[MULTIPLIERS_LIMIT_50_BIT];
        // TODO going with the big array -> performance is not consistent and is higher
        int limit43Bit = 50000; // n^1/3 * 3
        int limit63Bit = (MULTIPLIERS_LIMIT_50_BIT) * 3; // n^1/3 * 3
        long[] smoothMultipliers = createMultipliersByMultiplerSequences(MULTIPLIERS_LIMIT_50_BIT, filterBadMultipliers);

        // you can use booth methods, for Mod32TableSquareAdjuster initializeWithMultipliersAndLength seems to work
//        initializeMultipliersWithoutLoopOptimisation(smoothMultipliers);
        optimizeArraysForVectorisation(smoothMultipliers);
    }

    long[] createMultipliersByMultiplerSequences(int limit, boolean filterBadMultipliers) {
        int maxIndex = limit / 3;
        BitSet primes = SmallPrimes.generatePrimesSet(maxIndex);
        long[] goodMultipliers = new long[limit];
        Set<Long> multipliersUsed = new HashSet<>();

        // Definition of a multiplier sequence: {Delay or inverted Frequency, base Factor}
        long[][] multiplierSequences = {
                // Always add:
                {1, _3_3_5},       // 45
                // beside the base multiplier 45 = 3^2 * 5, the next (unused) prime 7 (5 is used already) is working good
                {1, _3_3_5 * 7},   // 105

                // We see another 3 (beside 3*3 already) working good as an additional multiplier.
                // but 9 is bad.
                // TODO since we  always add '3' cant we move it in the loop?
                // so we add 3 to the tow multiplier sequences from above but with a low frequency, only every second step
                {2, _3_3_5 * 3},   // 315
                {2, _3_3_5 * 7 * 3},
                // and add a new prime '11' lowest prime not used so far
                {2, _3_3_5 * 11},
                {2, _3_3_5 *  7 * 11},

                // every fourth iteration add the following multipliers
                // again adding 3 to the newly create base multipliers from above
                {4, _3_3_5 * 11 * 3},
                {4, _3_3_5 *  7 * 11 * 3},
                // and a new prime '13'
                {4, _3_3_5 * 13},
                {4, _3_3_5 * 11 * 13},
                {4, _3_3_5 *  7 * 11 * 13},

                // every 8th step add a multipler '3' to the multipliers created in the last step
                {8, _3_3_5 * 13 * 3},
                {8, _3_3_5 * 11 * 13 * 3},
                {8, _3_3_5 *  7 * 11 * 13 * 3}

                // the pattern, we have seen above seems to not work forever maybe due to the increasing steps, so we stop
        };

        int storeIndex = 1;
        int maxIncreasePerLoop = multiplierSequences.length;

        for (int creationIndex = 1; creationIndex < maxIndex && storeIndex + maxIncreasePerLoop < limit; creationIndex++) {
            for (long[] seq : multiplierSequences) {
                int delay = (int) seq[0];
                long baseMultiplier = seq[1];

                if (creationIndex % delay == 0) {
                    int correctedIndex = creationIndex / delay;
                    if (!filterBadMultipliers || isAGoodMultiplier(correctedIndex, baseMultiplier, primes))
                        storeIndex = addMultiplier(correctedIndex * baseMultiplier, multipliersUsed, goodMultipliers, storeIndex);
                }
            }
        }

        goodMultipliers[0] = storeIndex - 1;
        return goodMultipliers;
    }

    // TODO we might precompute (on the index?) good Multipliers
    private boolean isAGoodMultiplier(int index, long baseMultiplier, BitSet primes) {
        long multiplier = index * baseMultiplier;
        // multiples of 81 do not work out good. Found by looking at how often multipliers i % 81
        // with AnalyzeSolutionsModulus factorize random semiprimes, all multipliers are multiples of 9 already.
        boolean isBadMultipleMod9 = multiplier % 81 == 0;
        if (isBadMultipleMod9)
            return false;
        // TODO we might move the check on index out of the loop
        // small numbers and smooth numbers seem to create good multipliers.
        // up to a certain limit it seems we need all numbers
        boolean isSmall = index <= 100;
        if (isSmall)
            return true;

        // a prime by definition only consists out of one factor and can not be split in different
        // dominator nominator combinations, and such covers only one relation of p/q , where q*p
        // is the number to factorize. ~ 2% speed improvement
        // Using only smooth multipliers is not fast on the other hand
        return !primes.get(index);
//        return true;
    }

    private static int addMultiplier(long currentMultiplier, Set<Long> multipliersUsed, long[] goodMultipliers, int index) {
        if (!multipliersUsed.contains(currentMultiplier)) {
            goodMultipliers[index++] = currentMultiplier;
            multipliersUsed.add(currentMultiplier);
        }
        return index;
    }

    public void initializeMultipliersWithoutLoopOptimisation(long[] smoothMultipliers) {
        long multiplierLength = smoothMultipliers[0];
        multiplier = new long[(int) multiplierLength];
        multiplier[0] = multiplierLength;
        for(int index = 1; index < multiplierLength; index++){
            multiplier[index] = smoothMultipliers[index];
            sqrtMultiplier[index] = Math.sqrt(multiplier[index]);
        }
    }

    @Override
    public long getMultiplier(int index) {
        return multiplier[index];
    }

    /**
     * After processing the multipliers, the multiplier (and sqrtMultiplier) array are structured in such a way,
     * that when iterating over it, the remainders of the multiplier are increasing by one modulus numberOfRemaindersMultiplierN.
     * This helps to optimise the main loop.
     */
    protected void optimizeArraysForVectorisation(long[] multipliers) {
        int length = (int) multipliers[0];
        long[][] multiplierPerRemainder = new long[numberOfRemaindersMultiplierN][2* (length / numberOfRemaindersMultiplierN)];
        int[] indexPerRemainder = fillRemainderLists(multipliers, length, multiplierPerRemainder);
        // TODO align with initializeWithMultipliersAndLength(long[] smoothMultipliers)
        int minIndex = Arrays.stream(indexPerRemainder).min().getAsInt();

        int sizeForVectorisation = numberOfRemaindersMultiplierN * minIndex;
        multiplier = new long[sizeForVectorisation];
        sqrtMultiplier = new double[sizeForVectorisation];

        int index = 0;

        for (int i= 0; i < minIndex; i++){
            // remainder 0 (mod 8) only leads to half of the factorisations compared with odd remainder,
            // but filling with an odd remainder is not helping. Maybe not using 4 * n ?
            for(int remainder = 0; remainder < numberOfRemaindersMultiplierN; remainder++){
                multiplier[index] = multiplierPerRemainder[remainder][i];
                sqrtMultiplier[index] = Math.sqrt(multiplier[index]);
                index++;
            }
        }
    }

    int[] fillRemainderLists(long[] multipliers, int length, long[][] multiplierPerRemainder) {
        int[] indexPerRemainder = new int[numberOfRemaindersMultiplierN];
        for (int index = 1; index < length; index++ ){
            long goodMultiplier = multipliers[index];
            int remainder = (int) (goodMultiplier % numberOfRemaindersMultiplierN);
            multiplierPerRemainder[remainder][indexPerRemainder[remainder]++] = goodMultiplier;
        }
        return indexPerRemainder;
    }
    protected double getSqrtMultiplier(int i) {
        return sqrtMultiplier[i];
    }

    public String getName() {
        return this.getClass().getSimpleName()  + (filterBadMultipliers ? " filter" : "");
    }
}
