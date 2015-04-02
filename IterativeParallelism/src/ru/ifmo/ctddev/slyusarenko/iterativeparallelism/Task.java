package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.function.Function;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 * @param <T> type parameter
 * @param <R> type parameter
 */
public class Task<T, R> {

    private T argument;
    private R result;
    private Function<? super T, ? extends R> function;

    Task(T argument, Function<? super T, ? extends R> function) {
        this.argument = argument;
        this.function = function;
    }

    /**
     * Apply function to argument
     * @since 1.0
     */
    public synchronized void process() {
        result = function.apply(argument);
        notifyAll();
    }

    /**
     * Just return result of the counting
     * @return result of applying function to argument
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see Thread
     */
    public synchronized R getResult() throws InterruptedException {
        while (result == null) {
            wait();
        }
        return result;
    }
}
