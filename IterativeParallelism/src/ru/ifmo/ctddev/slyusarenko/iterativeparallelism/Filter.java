package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


/**
 * @version 1.0
 * @author Maxim Slyusarenko
 * @param <T> type parameter
 */
public class Filter<T> implements Worker<List<T>> {
    private List<? extends T> list;
    private Predicate<? super T> predicate;
    private List<T> result;

    /**
     * Just a constructor.
     *
     * @param list given {@link java.util.List}
     * @param predicate given {@link java.util.function.Predicate}
     * @since 1.0
     */
    public Filter(List<? extends T> list, Predicate<? super T> predicate) {
        this.list = list;
        this.predicate = predicate;
    }

    /**
     *
     * @return {@link java.util.List} of elements from given list that satisfy the predicate
     * @since 1.0
     */
    @Override
    public List<T> getResult() {
        return result;
    }

    /**
     * Merge results from different threads
     * @param results results we received from different threads
     * @return {@link java.util.List} of elements from given list that satisfy the predicate
     * @since 1.0
     */
    @Override
    public List<T> getFinalResult(List<List<T>> results) {
        List<T> ans = new ArrayList<>();
        for (List<T> element: results) {
            ans.addAll(element);
        }
        return ans;
    }

    /**
     * Work with given <tt>list</tt>
     * @since 1.0
     * @see java.lang.Runnable
     */
    @Override
    public void run() {
        result = new ArrayList<>();
        for (T element: list) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
    }
}
