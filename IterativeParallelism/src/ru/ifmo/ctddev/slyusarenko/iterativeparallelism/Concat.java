package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.List;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 * @param <T> type parameter
 */
public class Concat<T> implements Worker<String> {
    private List<? extends T> list;
    private String result;

    /**
     * Just a constructor
     * @param list given {@link java.util.List}
     * @since 1.0
     */
    public Concat(List<? extends T> list) {
        this.list = list;
    }

    /**
     *
     * @return Concatenation of {@link java.lang.String} representations of given <tt>list</tt> elements
     * @since 1.0
     */
    @Override
    public String getResult() {
        return result;
    }

    /**
     * Merge results from different threads
     *
     * @param results results we received from different threads
     * @return Concatenation of {@link java.lang.String} representations of given <tt>list</tt> elements
     * @since 1.0
     */
    @Override
    public String getFinalResult(List<String> results) {
        Worker<String> merg = new Concat<>(results);
        merg.run();
        return merg.getResult();
    }

    /**
     * Function to work with <tt>list</tt>
     * @since 1.0
     * @see java.lang.Runnable
     */
    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        for (T element: list) {
            builder.append(element.toString());
        }
        result = builder.toString();
    }
}
