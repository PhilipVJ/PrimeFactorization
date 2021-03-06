package com.easv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ParallelPrimeFactorization extends AbstractPrimeFactorization {
    private boolean isDone = false;
    private final boolean useStream;
    private final ReentrantLock lock = new ReentrantLock(true);

    public ParallelPrimeFactorization(boolean useStream, long primeOne, long primeTwo, String jobName) {
        super(primeOne, primeTwo, jobName);
        this.useStream = useStream;
    }

    @Override
    PrimeResult factorize(long product, List<Long> primeNumbers) throws InterruptedException, IllegalArgumentException {
        // Executor service to handle threads
        // You could also use a cached thread pool, but it will generate threads if needed
        // which is very slow in this case
        ExecutorService executor = Executors.newFixedThreadPool(this.coresAvailable - 1);
        List<Future> futures = new ArrayList<>();
        int chunkSize = 10;
        AtomicReference<PrimeResult> result = new AtomicReference<>(null);
        // Make calculation tasks
        for (int i = 0; i < primeNumbers.size(); i += chunkSize) {
            int startIndex = i + 1;
            // Runnable does not return a value, but callable does
            Runnable task = () -> {
                for (int k = 0; k < chunkSize; k++) {
                    int stepStart = startIndex + k;
                    long multiplyValue = primeNumbers.get(stepStart - 1);
                    for (int j = stepStart; j < primeNumbers.size(); j++) {
                        if (!Thread.currentThread().isInterrupted()) {
                            long value = primeNumbers.get(j);
                            if (multiplyValue * value == product) {
                                result.set(new PrimeResult(value, multiplyValue));
                            }
                        } else {
                            break;
                        }
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
            Thread.sleep(100);
            if (result.get() != null) {
                isDone = true;
                // Cancel all futures
                futures.forEach(future -> future.cancel(true));
            } else {
                // Check if all tasks are done - meaning a result was not found
                boolean allTasksDone = true;
                for (Future task : futures) {
                    if (!task.isDone()) {
                        allTasksDone = false;
                    }
                }
                if (allTasksDone) {
                    throw new IllegalArgumentException("Did not find a result. The chosen inputs are not primes.");
                }
            }
        }
        // Executor services needs to be shutdown, or they will continue to run
        executor.shutdown();
        return result.get();
    }

    @Override
    List<Long> getPrimeNumbers(long toValue) throws InterruptedException {
        if (useStream) {
            return this.getPrimeNumbersWithStream(toValue);
        } else {
            return this.getPrimeNumbersWithoutStream(toValue);
        }
    }

    private List<Long> getPrimeNumbersWithStream(long toValue) {
        LongPredicate isPrime = value -> isAPrime(value);
        return LongStream.rangeClosed(1, toValue).
                parallel().filter(isPrime).boxed().
                collect(Collectors.toList());
    }

    private List<Long> getPrimeNumbersWithoutStream(long value) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(this.coresAvailable);
        List<Callable<Object>> tasks = new ArrayList<>();
        int maxTaskAmount = 100;
        long divided = value / maxTaskAmount;
        long leftOver = value - (divided * maxTaskAmount);
        List<Long> primeNumbers = new ArrayList<>();
        // I had to do partitions here otherwise it would show java heap space error
        int taskCount = 0;
        for (long i = 1; i <= maxTaskAmount + 1; i++) {
            taskCount++;
            long start = 0;
            long end = 0;
            start = taskCount * divided - divided;
            end = divided * taskCount;
            if (i == maxTaskAmount + 1) {
                // This is done to ensure the left over from the division is included
                start = value - leftOver;
                end = value;
            }
            long taskStart = start;
            long taskEnd = end;
            Runnable task = () -> {
                List<Long> taskLocalPrimes = new ArrayList<>();
                for (long j = taskStart; j < taskEnd; j++) {
                    if (isAPrime(j)) {
                        taskLocalPrimes.add(j);
                    }
                }
                this.lock.lock();
                try {
                    primeNumbers.addAll(taskLocalPrimes);
                } finally {
                    this.lock.unlock();
                }
            };
            Callable<Object> callableTask = Executors.callable(task);
            tasks.add(callableTask);
        }
        System.out.println("Getting primes in parallel without streams");
        executor.invokeAll(tasks);
        Collections.sort(primeNumbers);
        executor.shutdown();

        return primeNumbers;
    }
}
