package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.List;

/**
 * Interface to work in more than one thread
 * @param <R> type parameter
 * @version 1.0
 * @author Maxim Slyusarenko
 */

public interface Worker<R> extends Runnable {
    R getResult();
    R getFinalResult(List<R> results);
}
