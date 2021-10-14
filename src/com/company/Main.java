package com.company;

public class Main {

    public static void main(String[] args) {
        // Other primes:
        // 1471 2243 3593 271 10039 13841  18097  23689

        // Selected primes
        long firstPrime = 2243;
        long secondPrime = 10039;

        // Jobs
        ParallelPrimeFactorization psFactorization =
                new ParallelPrimeFactorization(true, firstPrime, secondPrime, "Parallel with stream");
        psFactorization.begin();

        ParallelPrimeFactorization pFactorization =
                new ParallelPrimeFactorization(false, firstPrime, secondPrime, "Parallel without stream");
        pFactorization.begin();

        SynchronousPrimeFactorization sFactorization =
                new SynchronousPrimeFactorization(firstPrime, secondPrime, "Synchronous");
        sFactorization.begin();
    }
}
