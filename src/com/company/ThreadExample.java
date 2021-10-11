package com.company;

import java.util.concurrent.*;

public class ThreadExample {

    public void run() {
        Runnable task = () -> {
            // Some operation
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Doing an operation");
        };

        Callable task2 = () -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        };

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        System.out.println("BEFORE SUBMIT");
        Future futureRunnable = executorService.submit(task);
        Future futureCallable = executorService.submit(task2);
        try {
            int result = (int) futureCallable.get();
            Object result2 = futureRunnable.get();
            System.out.println("anotherresult" + result2);
            System.out.println("Result: " + result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Callable<Integer> calculationTask = this::bigCalculation;
        Future<Integer> calculationFuture = executorService.submit(calculationTask);
        try {
            calculationFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions
        }
        System.out.println("After submit");
        System.out.println("Completable future");
        // CompletableFuture
        CompletableFuture<Integer> futureCalculation =
                CompletableFuture.supplyAsync(() -> 90, executorService).thenApply(value -> value * 2);
        // Calling get
        try {
            System.out.println(futureCalculation.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    public int bigCalculation() {
        return 10;
    }

    public int value = 0;

    public synchronized void increment() {
        value++;
    }
}


