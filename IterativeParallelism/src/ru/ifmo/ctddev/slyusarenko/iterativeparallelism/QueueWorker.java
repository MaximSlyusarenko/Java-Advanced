package ru.ifmo.ctddev.slyusarenko.iterativeparallelism;

/**
 * @version 1.0
 * @author Maxim Slyusarenko
 */
public class QueueWorker implements Runnable {

    private TaskQueue queue;

    /**
     * Constructor from {@link TaskQueue}
     *
     * @param queue {@link TaskQueue} to construct from
     * @since 1.0
     */
    public QueueWorker(TaskQueue queue) {
        this.queue = queue;
    }


    /**
     * All work of instance of this class
     * @since 1.0
     * @see Runnable
     */
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                queue.getTask().process();
            }
        } catch (InterruptedException ignored) {}
    }
}
