package com.easv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class AbstractPrimeFactorization {

    private final String jobName;
    public long primeOne;
    public long primeTwo;
    public long product;
    public int coresAvailable;

    private long startTime;

    public AbstractPrimeFactorization(long primeOne, long primeTwo, String jobName) {
        this.primeOne = primeOne;
        this.primeTwo = primeTwo;
        this.jobName = jobName;
    }

    private void setProduct() {
        this.product = this.primeOne * this.primeTwo;
        System.out.println("Product is: " + this.product);
    }

    public void startFactorization() {
        CompletableFuture<PrimeResult> resultFuture =
                CompletableFuture.supplyAsync(() -> {
                    // Get primes and handle exceptions
                    List<Long> primes = new ArrayList<>();
                    try {
                        primes=calculatePrimeNumbers(this.product);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        System.exit(1);
                    }
                    return primes;
                }).thenApply(primes -> {
                    // Get result and handle exceptions
                    PrimeResult result = null;
                    try {
                        result = factorizeProduct(this.product, primes);
                    } catch (InterruptedException | IllegalArgumentException ex) {
                        System.out.println(ex.getMessage());
                        System.exit(1);
                    }
                    return result;
                });
        try {
            PrimeResult result = resultFuture.get();
            System.out.println("Result is: " + result.primeOne + " & " + result.primeTwo);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error getting result with message: " + e.getMessage());
        }
    }

    public List<Long> calculatePrimeNumbers(long toValue) throws InterruptedException {
        System.out.println("Starting to find prime numbers");
        long currentTimePrePrime = System.currentTimeMillis();

        List<Long> primeNumbers = getPrimeNumbers(toValue);

        System.out.println("Total amount of primes: " + primeNumbers.size());
        long currentTimePostPrime = System.currentTimeMillis();

        System.out.println("Time to find prime numbers: " + ((currentTimePostPrime - currentTimePrePrime) / 1000));
        return primeNumbers;
    }

    public PrimeResult factorizeProduct(long product, List<Long> primes) throws InterruptedException {
        long timePreFactorize = System.currentTimeMillis();
        PrimeResult result = factorize(product, primes);
        long timePostFactorize = System.currentTimeMillis();
        System.out.println("Time to factorize: " + ((timePostFactorize - timePreFactorize) / 1000));
        return result;
    }

    abstract PrimeResult factorize(long product, List<Long> primes) throws InterruptedException, IllegalArgumentException;

    abstract List<Long> getPrimeNumbers(long toValue) throws InterruptedException;

    private void setAvailableCores() {
        this.coresAvailable = Runtime.getRuntime().availableProcessors();
    }

    public void begin() {
        System.out.println("Executing following job: " + this.jobName);
        System.out.println("------------------------------------------");
        // Init
        setProduct();
        setAvailableCores();

        // Action
        this.setStartTime();
        this.startFactorization();
        this.printTotalTime();
        System.out.println("------------------------------------------");
    }

    private void setStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    private void printTotalTime() {
        System.out.println("Total time: " + ((System.currentTimeMillis() - startTime) / 1000));
    }

    boolean isAPrime(long number) {
        if (number < 2) return false;
        if (number % 2 == 0) return (number == 2);
        int root = (int) Math.sqrt((double) number);
        for (int i = 3; i <= root; i += 2) {
            if (number % i == 0) return false;
        }
        return true;
    }


}
