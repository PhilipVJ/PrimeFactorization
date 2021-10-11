package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PrimeFactorizationWithoutStreams {

    private PrimeResult result;
    private boolean isDone = false;
    public List<Long> primeNumbers = new ArrayList<Long>();

    synchronized void setResult(PrimeResult result) {
        this.result = result;
    }

    public void begin() {
        System.out.println("Lets begin");
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Cores available: " + cores);
        // Other primes:
        // 1471 2243 3593 271 10039 13841  18097
        long firstPrime = 7;
        long secondPrime = 11;
        long product = firstPrime * secondPrime;
        System.out.println("First prime to find: " + firstPrime);
        System.out.println("Second prime to find: " + secondPrime);
        System.out.println("Product: " + product);
        long startTime = System.currentTimeMillis();
        // Get all prime numbers from 1 to the product e.g. 1 to 1532
        System.out.println("Starting to find prime numbers");
        long currentTimePrePrime = System.currentTimeMillis();
        setPrimesInRange(product);
        System.out.println("PRIMES: ");
        this.primeNumbers.sort(null);
        for (long prime : this.primeNumbers
        ) {
            System.out.println(prime);
        }
        System.out.println("Total amount of primes: " + this.primeNumbers.size());
        long currentTimePostPrime = System.currentTimeMillis();
        System.out.println("Took: " + ((currentTimePostPrime - currentTimePrePrime) / 1000));
        // Executor service to handle threads
        // You could also use a cached thread pool, but it will generate threads if needed
        // which is very slow in this case
        ExecutorService executor = Executors.newFixedThreadPool(cores - 1);
        List<Future> futures = new ArrayList<Future>();
        // Make calculation tasks
        for (int i = 0; i < this.primeNumbers.size(); i++) {
            int startIndex = i + 1;
            long multiplyValue = primeNumbers.get(i);
            // Runnable does not return a value, but callable does
            Runnable task = () -> {
                for (int j = startIndex; j < primeNumbers.size(); j++) {
                    if (!Thread.currentThread().isInterrupted()) {
                        long value = primeNumbers.get(j);
                        if (multiplyValue * value == product) {
                            setResult(new PrimeResult(value, multiplyValue));
                        }
                    } else {
                        break;
                    }
                }
            };
            // execute method is also available but will not return a future
            Future future = executor.submit(task);
            futures.add(future);
        }
        System.out.println("Waiting for result ..");
        while (!isDone) {
            // Checking each 100 ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.result != null) {
                System.out.println("Result found");
                isDone = true;
                // Cancel all futures
                futures.forEach(future -> future.cancel(true));
            } else {
                // Check if all tasks are done - meaning a result was not found
                boolean allTasksDone = true;
                for (Future task : futures
                ) {
                    if (!task.isDone()) {
                        allTasksDone = false;
                    }
                }
                if (allTasksDone) {
                    isDone = true;
                    System.out.println("Did not find a result. The chosen inputs are not primes.");
                }
            }
        }
        if (this.result != null) {
            System.out.println("Result: " + this.result.primeOne + "  &  " + this.result.primeTwo);
        }
        // Executor services needs to be shutdown, or they will continue to run
        executor.shutdown();
        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;
        System.out.println("Total time: " + totalTimeInSeconds + " seconds");

    }

    public void setPrimesInRange(long value) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Object>> tasks = new ArrayList<>();
        int maxTaskAmount = 10;
        long divided = value / maxTaskAmount;
        long leftOver = value - (divided * maxTaskAmount);
        System.out.println("Divided: " + divided);
        System.out.println("Leftover: " + leftOver);

        // I had to do partitions here otherwise it would show java heap space error
        int taskCount = 0;
        for (long i = 1; i <= maxTaskAmount + 1; i++) {
            taskCount++;
            long start = 0;
            long end = 0;
            start = taskCount * divided - divided;
            end = divided * taskCount;
            if (i == maxTaskAmount + 1) {
                // This is left over time
                start = value - leftOver;
                end = value;
            }

            long finalStart = start;
            long finalEnd = end;
            System.out.println(finalStart + "    " + finalEnd);
            Runnable task = () -> {

                for (long j = finalStart; j < finalEnd; j++) {
                    if (isAPrime(j)) {
                        synchronized (this) {
                            this.primeNumbers.add(j);
                        }
                    }
                }
            };
            Callable callableTask = Executors.callable(task);
            tasks.add(callableTask);
        }
        try {
            System.out.println("Getting primes in parallel without streams");
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
