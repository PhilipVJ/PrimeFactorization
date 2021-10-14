package com.company;

import java.util.ArrayList;

public class SynchronousPrimeFactorization extends AbstractPrimeFactorization {

    public SynchronousPrimeFactorization(long primeOne, long primeTwo, String jobName) {
        super(primeOne, primeTwo, jobName);
    }

    @Override
    void factorize() {
        for (int i = 0; i < this.primeNumbers.size(); i++) {
            int startIndex = i + 1;
            long multiplyValue = this.primeNumbers.get(i);
            boolean isDone = false;
            for (int j = startIndex; j < this.primeNumbers.size(); j++) {
                long value = this.primeNumbers.get(j);
                if (multiplyValue * value == this.product) {
                    this.result = new PrimeResult(value, multiplyValue);
                    isDone = true;
                }
            }
            if (isDone) {
                printResult();
                break;
            }
        }
    }

    @Override
    void setPrimeNumbers(long toValue) {
        this.primeNumbers = new ArrayList<>();
        for (long i = 1; i < toValue; i++) {
            if (isAPrime(i)) {
                this.primeNumbers.add(i);
            }
        }
    }
}
