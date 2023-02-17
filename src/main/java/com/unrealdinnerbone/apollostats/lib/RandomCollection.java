package com.unrealdinnerbone.apollostats.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomCollection<E> {
    private final Map<E, Double> values = new HashMap<>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public void add(double weight, E result) {
        total += weight;
        values.put(result, weight);
    }

    public E getRandom() {
        E result = null;
        double bestValue = Double.MAX_VALUE;
        for (E element : values.keySet()) {
            double value = -Math.log(random.nextDouble()) / values.get(element);
            if (value < bestValue) {
                bestValue = value;
                result = element;
            }
        }
        return result;
    }
    public double getTotal() {
        return total;
    }

}