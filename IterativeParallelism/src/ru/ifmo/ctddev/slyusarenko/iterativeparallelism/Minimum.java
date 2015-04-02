package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.Comparator;
import java.util.List;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 * @param <T> type parameter
 */
public class Minimum<T> implements Worker<T> {

    private List<? extends T> list;
    private Comparator<? super T> comparator;
    private T minimum;

    /**
     * Just a constructor
     *
     * @param list given {@link java.util.List}
     * @param comparator given {@link java.util.Comparator}
     * @since 1.0
     */
    public Minimum(List<? extends T> list, Comparator<? super T> comparator) {
        this.list = list;
        this.comparator = comparator;
    }

    /**
     *
     * @return minimal element in given <tt>list</tt> according to <tt>comparator</tt>
     * @since 1.0
     */
    @Override
    public T getResult() {
        return minimum;
    }

    /**
     * Merge results from different threads
     *
     * @param results results we received from different threads
     * @return minimal element in given <tt>list</tt> according to <tt>comparator</tt>
     * @since 1.0
     */
    @Override
    public T getFinalResult(List<T> results) {
        Worker<T> merg = new Minimum<T>(results, comparator);
        merg.run();
        return merg.getResult();
    }

    /**
     * Work with given <tt>list</tt>
     *
     * @since 1.0
     */
    @Override
    public void run() {
        T min = list.get(0);
        for (T element: list) {
            if (comparator.compare(min, element) > 0) {
                min = element;
            }
        }
        minimum = min;
    }
}
