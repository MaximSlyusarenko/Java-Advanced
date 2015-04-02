package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 */
public class ParallelMapperImpl implements ParallelMapper {

    private List<Thread> threads;
    private TaskQueue queue;

    /**
     * Construct parallel mapper with given number of threads
     *
     * @param numberOfThreads number of threads to which work should be divided
     * @see Thread
     * @see TaskQueue
     * @since 1.0
     */
    public ParallelMapperImpl(int numberOfThreads) {
        threads = new ArrayList<>();
        queue = new TaskQueue();
        for (int i = 0; i < numberOfThreads; i++) {
            Thread current = new Thread(new QueueWorker(queue));
            threads.add(current);
            current.start();
        }
    }

    /**
     * Apply given <tt>function</tt> to given list of argument and work in parallel in number of threads given in constructor
     * @param function function we need to apply
     * @param list list of arguments to apply function for
     * @param <T> type parameter of given arguments in <tt>list</tt>
     * @param <R> type parameter of resulting list
     * @return list of arguments- result of the applying given function to given list
     * @throws InterruptedException if we have exception in work with threads
     * @since 1.0
     * @see Task
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<Task<? super T, ? extends R>> tasks = new ArrayList<>();
        for (T element: list) {
            Task<? super T, ? extends R> task = new Task<>(element, function);
            queue.add(task);
            tasks.add(task);
        }
        List<R> result = new ArrayList<>();
        for (Task<? super T, ? extends R> task: tasks) {
            result.add(task.getResult());
        }
        return result;
    }

    /**
     * Interrupt all working threads
     *
     * @throws InterruptedException if we have exception in work with threads
     */
    @Override
    public void close() throws InterruptedException {
        threads.forEach(java.lang.Thread::interrupt);
    }
}
