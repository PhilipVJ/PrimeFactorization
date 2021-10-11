package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class PrimeFactorization {

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
        LongPredicate isPrime = argument -> isAPrime(argument);
        long startTime = System.currentTimeMillis();
        // Get all prime numbers from 1 to the product e.g. 1 to 1532
        List<Long> primeNumbers = LongStream.rangeClosed(1, product).parallel().filter(isPrime).boxed().collect(Collectors.toList());
        // Executor service to handle threads
        // You could also use a cached thread pool, but it will generate threads if needed
        // which is very slow in this case
        ExecutorService executor = Executors.newFixedThreadPool(cores - 1);
        List<Future> futures = new ArrayList<Future>();
        // Make calculation tasks
        for (int i = 0; i < primeNumbers.size(); i++) {
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
