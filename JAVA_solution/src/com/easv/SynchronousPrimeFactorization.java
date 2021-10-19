package com.easv;
import java.util.ArrayList;
import java.util.List;

public class SynchronousPrimeFactorization extends AbstractPrimeFactorization {

    public SynchronousPrimeFactorization(long primeOne, long primeTwo, String jobName) {
        super(primeOne, primeTwo, jobName);
    }

    @Override
    PrimeResult factorize(long product, List<Long> primeNumbers) {
        for (int i = 0; i < primeNumbers.size(); i++) {
            int startIndex = i + 1;
            long multiplyValue = primeNumbers.get(i);
            for (int j = startIndex; j < primeNumbers.size(); j++) {
                long value = primeNumbers.get(j);
                if (multiplyValue * value == this.product) {
                    return new PrimeResult(value, multiplyValue);
                }
            }
        }
        return null;
    }

    @Override
    List<Long> getPrimeNumbers(long toValue) {
        List<Long> primeNumbers = new ArrayList<>();
        for (long i = 1; i < toValue; i++) {
            if (isAPrime(i)) {
                primeNumbers.add(i);
            }
        }
        return primeNumbers;
    }
}
