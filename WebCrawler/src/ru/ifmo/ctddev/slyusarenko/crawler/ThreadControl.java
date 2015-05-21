package ru.ifmo.ctddev.slyusarenko.crawler;

import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.net.MalformedURLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Class which works with threads, controls that number of threads don't be greater than number of threads we can have and so on
 * @author Maxim Slyusarenko
 * @version 1.0
 * @since 1.0
 */
public class ThreadControl {

    public Downloader downloader;
    public final int downloaders;
    public final int extractors;
    public final int perHost;
    public ExecutorService downloadThreads;
    public ExecutorService extractorThreads;
    public ConcurrentHashMap<String, Semaphore> hosts;

    /**
     * Constructor from given arguments
     * @param downloader something that implements interface {@link Downloader}
     * @param downloaders number of threads which can download web pages in parallel
     * @param extractors number of threads which can extract links from web pages in parallel
     * @param perHost maximal number of threas which can work with one host in parallel
     */
    public ThreadControl(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = downloaders;
        this.extractors = extractors;
        this.perHost = perHost;
        downloadThreads = Executors.newFixedThreadPool(downloaders);
        extractorThreads = Executors.newFixedThreadPool(extractors);
        hosts = new ConcurrentHashMap<>();
    }

    /**
     * if <tt>hosts</tt> don't contain host with given <tt>url</tt> than insert it. Call method acquire of {@link Semaphore} of given <tt>url</tt>
     * @param url url to do this with it
     * @since 1.0
     * @see Semaphore#acquire()
     * @see URLUtils#getHost(String)
     */
    public void acquire(String url) {
        try {
            String host = URLUtils.getHost(url);
            if (!hosts.containsKey(host)) {
                hosts.put(host, new Semaphore(perHost));
            }
            hosts.get(host).acquire();
        } catch (MalformedURLException | InterruptedException ignored) {
        }
    }

    /**
     * Call method release of {@link Semaphore} of given <tt>url</tt>
     * @param url url to do this with it
     * @since 1.0
     * @see Semaphore#release()
     * @see URLUtils#getHost(String)
     */
    public void release(String url) {
        try {
            String host = URLUtils.getHost(url);
            hosts.get(host).release();
        } catch (MalformedURLException ignored) {
        }
    }

    /**
     * Close all working threads
     * @since 1.0
     * @see ExecutorService#shutdown()
     */
    public void close() {
        downloadThreads.shutdown();
        extractorThreads.shutdown();
    }
}
