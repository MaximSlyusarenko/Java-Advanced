package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @version 1.0
 * @author Maxim Slyusarenko
 * @param <T> type parameter of given {@link java.util.List}
 * @param <U> type parameter of result {@link java.util.List}
 */
public class Map<T, U> implements Worker<List<U>> {
    private List<? extends T> list;
    private Function<? super T, ? extends U> function;
    private List<U> result;

    /**
     * Just a constructor
     *
     * @param list given {@link java.util.List}
     * @param function given {@link java.util.function.Function}
     * @since 1.0
     */
    public Map(List<? extends T> list, Function<? super T, ? extends U> function) {
        this.list = list;
        this.function = function;
    }

    /**
     *
     * @return {@link java.util.List} of elements. Elements in List are result of applying function to elements in given <tt>list</tt>
     */
    @Override
    public List<U> getResult() {
        return result;
    }

    /**
     * Merge results fro different threads.
     *
     * @param results results we received from different threads
     * @return {@link java.util.List} of elements. Elements in List are result of applying function to elements in given <tt>list</tt>
     * @since 1.0
     */
    @Override
    public List<U> getFinalResult(List<List<U>> results) {
        List<U> ans = new ArrayList<>();
        for (List<U> element: results) {
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
            result.add(function.apply(element));
        }
    }
}
