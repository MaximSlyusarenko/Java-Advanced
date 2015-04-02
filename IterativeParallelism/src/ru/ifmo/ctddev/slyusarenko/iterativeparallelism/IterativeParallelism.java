package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 */

@SuppressWarnings("unchecked")
public class IterativeParallelism implements ListIP {

    private ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    private static <T> List<List<? extends T>> split(int threads, List<? extends T> work) {
        if (work.size() == 0) {
            throw new UnsupportedOperationException("Can't do some operation with empty list");
        }
        List<List<? extends T>> works = new ArrayList<>();
        int threadSize = work.size() / threads;
        if (threadSize == 0) {
            threadSize = 1;
            threads = work.size();
        }
        if (work.size() - (threads - 1) * threadSize - 1 > threadSize) {
            threadSize++;
        }
        for (int i = 0; i < threads; i++) {
            if (i != threads - 1) {
                works.add(work.subList(i * threadSize, (i + 1) * threadSize));
            } else {
                works.add(work.subList(i * threadSize, work.size()));
            }
        }
        return works;
    }


    /**
     *
     * <p>Concatenate list elements.</p>
     * <p>Work in given number of threads or if given number is greater than size of list work in {@code list.size()} threads.</p>
     *
     * @param i number of threads
     * @param list {@link java.util.List} of arguments we want to concat
     * @return {@link java.lang.String} which is concatenation of String representation of given <tt>list</tt> elements
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Concat
     */
    @Override
    public String concat(int i, List<?> list) throws InterruptedException {
        //return parallel(Math.min(i, list.size()), split(i, list), arg -> new Concat(arg));
        List<String> results = map(i, list, Object::toString);
        StringBuilder finalResult = new StringBuilder();
        results.forEach(finalResult::append);
        return finalResult.toString();
    }

    /**
     * <p>Filter list elements.</p>
     * <p>Work in given number of threads or if given number is greater than size of list work in {@code list.size()} threads.</p>
     *
     * @param i number of threads
     * @param list {@link java.util.List} of arguments we want to filter
     * @param predicate {@link java.util.function.Predicate} on which we have to filter
     * @param <T> type name
     * @return {@link java.util.List} of elements from given list that satisfy the predicate
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see java.util.function.Predicate
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Filter
     */
    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallel(Math.min(i, list.size()), split(i, list), arg -> new Filter(arg, predicate), work -> work.stream().filter(predicate).collect(Collectors.toList()), results -> results.stream().reduce(new ArrayList<T>(), (x, y) -> {
            x.addAll(y);
            return x;}));
    }


    /**
     * <p>Map of list elements.</p>
     * <p>Work in given number of threads or if given number is greater than size of list work in {@code list.size()} threads.</p>
     *
     * @param i number of threads
     * @param list {@link java.util.List} of elements we want to do map from
     * @param function {@link java.util.function.Function} we want apply to given list elements
     * @param <T> type parameter
     * @param <U> returned list type parameter
     * @return {@link java.util.List} of elements. Elements in List are result of applying function to elements in given <tt>list</tt>
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see java.util.function.Function
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Map
     */
    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return parallel(Math.min(i, list.size()), split(i, list), arg -> new Map(arg, function), work -> work.stream().map(function).collect(Collectors.toList()),
                results -> results.stream().reduce(new ArrayList<U>(), (x, y) -> {
                    x.addAll(y);
                    return x;
                }));
    }


    /**
     * <p>Find maximal element in given list.</p>
     * <p>Work in given number of threads or if given number is greater than size of list work in {@code list.size()} threads.</p>
     *
     * @param i number of threads
     * @param list {@link java.util.List} of elements we want to know maximum for
     * @param comparator {@link java.util.Comparator} whereby we want to find maximum
     * @param <T> type parameter
     * @return maximal element in given <tt>list</tt> according to <tt>comparator</tt>
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see java.util.Comparator
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Maximum
     */
    @Override
    public <T> T maximum(int i, List<? extends T> list, final Comparator<? super T> comparator) throws InterruptedException {
        return parallel(Math.min(i, list.size()), split(i, list), arg -> new Maximum<T>(arg, comparator), work -> Collections.max(work, comparator), results -> Collections.max(results, comparator));
    }

    /**
     * <p>Find minimal element in given list.</p>
     * <p>Work in given number of threads or if given number is greater than size of list work in {@code list.size()} threads.</p>
     * @param i number of threads
     * @param list {@link java.util.List} of elements we want
     * @param comparator {@link java.util.Comparator} whereby we want to find minimum
     * @param <T> type parameter
     * @return minimal element in given <tt>list</tt> according to <tt>comparator</tt>
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see java.util.Comparator
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Minimum
     */
    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return parallel(Math.min(i, list.size()), split(i, list), arg -> new Minimum<T>(arg, comparator), work -> Collections.min(work, comparator), results -> Collections.min(results, comparator));
    }

    /**
     * <p>Check if al elements of given list satisfy the given predicate.</p>
     * <p>Work in given number of threads or if given number is greater than size of list work in {@code list.size()} threads.</p>
     *
     * @param i number of threads
     * @param list {@link java.util.List} of elements
     * @param predicate {@link java.util.function.Predicate} to check the information about elements in list
     * @param <T> type parameter
     * @return true if of all elements from given <tt>list</tt> satisfy the given <tt>predicate</tt> and false otherwise
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see java.util.function.Predicate
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.All
     */
    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallel(Math.min(i, list.size()), split(i, list), arg -> new All(arg, predicate), work -> work.stream().allMatch(predicate), results -> results.stream().allMatch(Predicate.isEqual(true)));
    }

    /**
     * <p>Check if it's at least one element in given list which satisfy the given predicate.</p>
     * <p>Work in given number of threads or if given number is greater than size of list work in {@code list.size()} threads.</p>
     *
     * @param i number of threads
     * @param list {@link java.util.List} of elements
     * @param predicate {@link java.util.function.Predicate} to check the information about elements in list
     * @param <T> type parameter
     * @return true if it's at least one element in given <tt>list</tt> which satisfy the given <tt>predicate</tt> and false otherwise
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see java.util.function.Predicate
     * @see ru.ifmo.ctddev.slyusarenko.iterativeparallelism.Any
     */
    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallel(Math.min(i, list.size()), split(i, list), arg -> new Any(arg, predicate), work -> work.stream().anyMatch(predicate), results -> results.stream().anyMatch(Predicate.isEqual(true)));
    }

    private <T, U> U parallel(int i, List<List<? extends T>> list, Function<List<? extends T>, Worker<T>> function, Function<List<? extends T>, U> func, Function<List<U>, U> getResult) throws InterruptedException {
        if (mapper != null) {
            List<U> results = mapper.map(func, list);
            return getResult.apply(results);
        }
        List<Thread> threads = new ArrayList<>();
        List<Worker<T>> workers = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            workers.add(function.apply(list.get(j)));
            threads.add(new Thread(workers.get(j)));
            threads.get(j).start();
        }
        try {
            for (Thread thread: threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        List<T> results = workers.stream().map(Worker::getResult).collect(Collectors.toList());
        return (U) workers.get(0).getFinalResult(results);
    }

}
