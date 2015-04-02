package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 */
public class TaskQueue {

    private Queue<Task<?, ?>> queue;

    TaskQueue() {
        queue = new LinkedList<>();
    }

    /**
     * Add new task to queue
     *
     * @param task task we want to add in queue
     * @see Task
     * @since 1.0
     */
    public synchronized void add(Task<?, ?> task) {
        queue.add(task);
        notifyAll();
    }

    /**
     * Get next task from queue
     * @return next task in task queue
     * @throws InterruptedException if we have exception in work with threads
     */
    public synchronized Task<?, ?> getTask() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }
}
