package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ParallelPrimeFactorization extends AbstractPrimeFactorization {
    private boolean isDone = false;
    private final boolean useStream;
    private final ReentrantLock lock = new ReentrantLock(true);

    synchronized void setResult(PrimeResult result) {
        this.result = result;
    }

    public ParallelPrimeFactorization(boolean useStream, long primeOne, long primeTwo, String jobName) {
        super(primeOne, primeTwo, jobName);
        this.useStream = useStream;
    }

    @Override
    void factorize() {
        // Executor service to handle threads
        // You could also use a cached thread pool, but it will generate threads if needed
        // which is very slow in this case
        ExecutorService executor = Executors.newFixedThreadPool(this.coresAvailable - 1);
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
            printResult();
        }
        // Executor services needs to be shutdown, or they will continue to run
        executor.shutdown();
    }

    @Override
    void setPrimeNumbers(long toValue) {
        this.primeNumbers = new ArrayList<>();
        if (useStream) {
            this.setPrimeNumbersWithStream(toValue);
        } else {
            this.setPrimeNumbersWithoutStream(toValue);
        }
    }

    private void setPrimeNumbersWithStream(long toValue) {
        LongPredicate isPrime = value -> isAPrime(value);
        this.primeNumbers = LongStream.rangeClosed(1, toValue).parallel().filter(isPrime).boxed().collect(Collectors.toList());
    }

    private void setPrimeNumbersWithoutStream(long value) {
        ExecutorService executor = Executors.newFixedThreadPool(this.coresAvailable + 50);
        List<Callable<Object>> tasks = new ArrayList<>();
        int maxTaskAmount = 100;
        long divided = value / maxTaskAmount;
        long leftOver = value - (divided * maxTaskAmount);
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
            Runnable task = () -> {
                for (long j = finalStart; j < finalEnd; j++) {
                    if (isAPrime(j)) {
                        this.lock.lock();
                        try {
                            this.primeNumbers.add(j);
                        } finally {
                            this.lock.unlock();
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
            this.primeNumbers.sort(null);
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
