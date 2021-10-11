package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PrimeFactorizationSynchronous {

    private PrimeResult result;
    private boolean isDone = false;

    synchronized void setResult(PrimeResult result) {
        this.result = result;
    }

    public void begin() {
        System.out.println("Lets begin");
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Cores available: " + cores);
        // Other primes:
        // 1471 2243 3593 271 10039 13841  18097
        long firstPrime = 10039;
        long secondPrime = 13841;
        long product = firstPrime * secondPrime;
        System.out.println("First prime to find: " + firstPrime);
        System.out.println("Second prime to find: " + secondPrime);
        System.out.println("Product: " + product);
        long startTime = System.currentTimeMillis();
        // Get all prime numbers from 1 to the product e.g. 1 to 1532
        System.out.println("Starting to find prime numbers");
        long currentTimePrePrime = System.currentTimeMillis();
        List<Long> primeNumbers = new ArrayList<>();
        for (long i = 1; i < product; i++) {
            if (isAPrime(i)) {
                primeNumbers.add(i);
            }
        }
        long currentTimePostPrime = System.currentTimeMillis();
        System.out.println("Took: " + ((currentTimePostPrime - currentTimePrePrime) / 1000));
        // Executor service to handle threads
        // You could also use a cached thread pool, but it will generate threads if needed
        // which is very slow in this case
        ExecutorService executor = Executors.newFixedThreadPool(cores - 1);
        List<Future> futures = new ArrayList<Future>();
        // Make calculation tasks
        for (int i = 0; i < primeNumbers.size(); i++) {
            int startIndex = i + 1;
            long multiplyValue = primeNumbers.get(i);
            boolean isDone = false;
            for (int j = startIndex; j < primeNumbers.size(); j++) {
                long value = primeNumbers.get(j);
                if (multiplyValue * value == product) {
                    setResult(new PrimeResult(value, multiplyValue));
                    isDone = true;
                }
            }
            if (isDone) {
                System.out.println("Found number!");
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;
        System.out.println("Total time: " + totalTimeInSeconds + " seconds");

    }

    private boolean isAPrime(long number) {
        if (number < 2) return false;
        if (number % 2 == 0) return (number == 2);
        int root = (int) Math.sqrt((double) number);
        for (int i = 3; i <= root; i += 2) {
            if (number % i == 0) return false;
        }
        return true;
    }
}
