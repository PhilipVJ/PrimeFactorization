package com.company;

import java.util.List;

public abstract class AbstractPrimeFactorization {

    private String jobName;
    public long primeOne;
    public long primeTwo;
    public long product;
    public int coresAvailable;
    public PrimeResult result;

    public List<Long> primeNumbers;

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

    public void printResult() {
        System.out.println("Result: " + this.result.primeOne + "  &  " + this.result.primeTwo);
    }

    public void startFactorization() {
        calculatePrimeNumbers(this.product);
        long timePreFactorize = System.currentTimeMillis();
        factorize();
        long timePostFactorize = System.currentTimeMillis();
        System.out.println("Time to factorize: " + ((timePostFactorize - timePreFactorize) / 1000));
    }

    public void calculatePrimeNumbers(long toValue) {
        System.out.println("Starting to find prime numbers");
        long currentTimePrePrime = System.currentTimeMillis();
        setPrimeNumbers(toValue);
        System.out.println("Total amount of primes: " + primeNumbers.size());
        long currentTimePostPrime = System.currentTimeMillis();
        System.out.println("Time to find prime numbers: " + ((currentTimePostPrime - currentTimePrePrime) / 1000));
    }

    abstract void factorize();

    abstract void setPrimeNumbers(long toValue);

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
