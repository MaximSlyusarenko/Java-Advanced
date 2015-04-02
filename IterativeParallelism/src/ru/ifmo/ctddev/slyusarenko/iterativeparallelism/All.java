package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.List;
import java.util.function.Predicate;

/**
 * @version 1.0
 * @author Maxim Slusarenko
 * @param <T> type parameter
 */

public class All<T> implements Worker<Boolean> {

    private List<? extends T> list;
    private Predicate<? super T> predicate;
    private Boolean answer;

    /**
     * Just a constructor.
     *
     * @param list given {@link java.util.List}
     * @param predicate given {@link java.util.function.Predicate}
     * @since 1.0
     */
    public All(List<? extends T> list, Predicate<? super T> predicate) {
        this.list = list;
        this.predicate = predicate;
    }

    /**
     * Returns result
     *
     * @return true if all elements of <tt>list</tt> satisfy the <tt>predicate</tt> and false otherwise
     * @since 1.0
     */
    @Override
    public Boolean getResult() {
        return answer;
    }

    /**
     * Merge results from different threads
     *
     * @param results results we received from different threads
     * @return true if all elements of <tt>list</tt> satisfy the <tt>predicate</tt> and false otherwise
     * @since 1.0
     */
    @Override
    public Boolean getFinalResult(List<Boolean> results) {
        Worker<Boolean> merg = new All<>(results, Predicate.isEqual(true));
        merg.run();
        return merg.getResult();
    }

    /**
     * Function to work with given <tt>list</tt>
     * @since 1.0
     * @see java.lang.Runnable
     */
    @Override
    public void run() {
        for (T element: list) {
            if (!predicate.test(element)) {
                answer = false;
                break;
            }
            answer = true;
        }
    }
}
