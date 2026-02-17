package de.harich.thilo.factoring.calculator;


import de.harich.thilo.factoring.algorithm.hart.HartFactorization;

import static de.harich.thilo.factoring.algorithm.hart.HartFactorization.UNDEFINED;
import static de.harich.thilo.factoring.calculator.LemireHartSmoothFactorisationCalculator.addFactor2;

/**
 * A class for calculating the prime factorization of a long number.
 * Not optimized for speed. Uses lambdas to keep the number of lines in the code short.
 * TODO make an CLI interface to factorize a buch of number
 */
public class HartFactorisationCalculator implements FactorisationCalculator {

    HartFactorization factorisationAlgorithm = new HartFactorization();

    public HartFactorisationCalculator() {}


    @Override
    public long[] getSortedPrimeFactors(long number) {
        int numberBits = Long.SIZE - Long.numberOfLeadingZeros(number);
        long[] primeFactors = new long[numberBits];
        int trailingZeros = Long.numberOfTrailingZeros(number);
        number = number >> trailingZeros;
        int index = addFactor2(primeFactors, trailingZeros);
        // check for squares
        long sqrt;
        if (number > 1 && (sqrt = getPerfectSquare(number)) > 0) {
            primeFactors[index++] = sqrt;
            primeFactors[index] = sqrt;
            return primeFactors;
        }
        while (number > 1){

            int stopAt = (int) Math.cbrt(number);
            long factor = factorisationAlgorithm.findSingleFactor(number, stopAt);
            // if we have stopped and have not found a factor, number must be a prime factor itself
            if (factor == UNDEFINED){
                primeFactors[index] = number;
                return primeFactors;
            }
            number = number / factor;
            primeFactors[index++] = factor;

            if (number > 1 && (sqrt = getPerfectSquare(number)) > 0) {
                primeFactors[index++] = sqrt;
                primeFactors[index] = sqrt;
                return primeFactors;
            }
        }
        return primeFactors;
    }


    public static int getPerfectSquare(long n) {
        if (n < 0) return -1;
        // Die letzten 4 Bits einer Quadratzahl in Hex sind nur 0, 1, 4, 9
        long h = n & 0xF;
        if (h > 9) return -1;
        if (h != 2 && h != 3 && h != 5 && h != 6 && h != 7 && h != 8) {
            long t = (long) Math.sqrt(n);
            if (t * t == n)
                return (int) t;
        }
        return -1;
    }


}
