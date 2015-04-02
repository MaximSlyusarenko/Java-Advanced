package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.List;
import java.util.function.Predicate;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 * @param <T> type parameter
 */
public class Any<T> implements Worker<Boolean> {

    private All<T> all;

    /**
     * Just a constructor
     * @param list given {@link java.util.List}
     * @param predicate given {@link java.util.function.Predicate}
     * @since 1.0
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.All#All
     * @see java.util.function.Predicate#negate
     */
    public Any(List<? extends T> list, Predicate<? super T> predicate) {
        all = new All<>(list, predicate.negate());
    }

    /**
     *
     * @return true if it's at least one element in <tt>list</tt> which satisfy <tt>predicate</tt>
     * @since 1.0
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.All#getResult
     */
    @Override
    public Boolean getResult() {
        return !all.getResult();
    }

    /**
     * Merge results from different threads
     * @param results results we received from different threads
     * @return true if it's at least one element in <tt>list</tt> which satisfy <tt>predicate</tt>
     * @since 1.0
     */
    @Override
    public Boolean getFinalResult(List<Boolean> results) {
        Worker<Boolean> merg = new Any<>(results, Predicate.isEqual(true));
        merg.run();
        return merg.getResult();
    }

    /**
     * Function to work with given <tt>list</tt>
     * @since 1.0
     * @see java.lang.Runnable
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.All#run
     */
    @Override
    public void run() {
        all.run();
    }
}
