In this project we provide fast java factoring algorithms for integers up to 63 bits.
The main goal for this are 

- Clean Code
- a fast implementation of Hart and trial division Factoring algorithms
- how to use vectorisation in factoring algorithms in Java without loosing readability
- get hands on some aspects of the algorithm by prepared tests, which can be extended
- provide a easy-to-use command line interface (on the way)

# Algorithms

## Hart Factoring algorithm

Some work went into it, to improve the original approach 
  - enable JIT to optimize the code for vectorization
  - optimize the multipliers 
  - optimize values on different modulus (64 and 81)

The algorithm runs in O(n^1/3). In combination with a optimized trial division algorithm
it should be one of the fastest algorithms for long numbers.

## Lemire Trial Division algorithm 

Since the main loop is simple java can also make use of vectorisation.
The fastest algorithm uses no divisions, which are expensive.
It uses just long values (casting to double and back to long), and such has a good 
performance on cpu's which support SSE, AVX.
Lemire's approach is from 2019 and relatively new, and has good support from 
java and cpu's with vectorisation support.
On SSE-2 We se 250% speedup compared to using a multiplications with the Reciprocals
instead of using a division together with a rounding trick which avoids expensive type conversions.
We see 1200% improvement over division and rounding.
# Author 

Thilo Harich a java software developer. 


# Credits 
Tilman Neumann - for the great joint work on the Hart algorithm.