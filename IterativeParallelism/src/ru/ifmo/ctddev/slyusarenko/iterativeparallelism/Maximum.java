package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.Comparator;
import java.util.List;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 * @param <T> type parameter
 */
public class Maximum<T> implements Worker<T> {

    private Minimum<T> minimum;

    /**
     * Just a constructor
     *
     * @param list given {@link java.util.List}
     * @param comparator given {@link java.util.Comparator}
     * @since 1.0
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Minimum#Minimum
     */
    public Maximum(List<? extends T> list, Comparator<? super T> comparator) {
        minimum = new Minimum(list, comparator.reversed());
    }

    /**
     *
     * @return maximal element in given <tt>list</tt> according to <tt>comparator</tt>
     * @since 1.0
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Minimum#getResult
     */
    @Override
    public T getResult() {
        return minimum.getResult();
    }

    /**
     * Merge results from different threads
     *
     * @param results results we received from different threads
     * @return maximal element in given <tt>list</tt> according to <tt>comparator</tt>
     * @since 1.0
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Minimum#getFinalResult
     */
    @Override
    public T getFinalResult(List<T> results) {
        return minimum.getFinalResult(results);
    }

    /**
     * Work with given <tt>list</tt>
     * @since 1.0
     * @see java.lang.Runnable
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Minimum#run
     */
    @Override
    public void run() {
        minimum.run();
    }
}
