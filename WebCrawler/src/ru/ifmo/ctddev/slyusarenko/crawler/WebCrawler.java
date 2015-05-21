package ru.ifmo.ctddev.slyusarenko.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class which can download web pages
 * @author MaximSlyusarenko
 * @version 1.0
 */
public class WebCrawler implements Crawler {

    private ThreadControl threadControl;

    /**
     * Constructor from given arguments
     * @param downloader something that implements interface {@link Downloader}
     * @param downloaders number of threads which can download web pages in parallel
     * @param extractors number of threads which can extract links from web pages in parallel
     * @param perHost maximal number of threas which can work with one host in parallel
     * @since 1.0
     * @see Downloader
     * @see ReentrantLock
     * @see AtomicInteger
     * @see Condition
     * @see ConcurrentLinkedQueue
     * @see ThreadControl#ThreadControl(Downloader, int, int, int)
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        threadControl = new ThreadControl(downloader, downloaders, extractors, perHost);
    }

    /**
     * Download web pages starting from given url and to given depth
     * @param url web page to start from
     * @param depth depth of web pages
     * @return {@link List} of urls of web pages
     * @since 1.0
     * @see ru.ifmo.ctddev.slyusarenko.crawler.WebCrawler.DownloadWorker
     * @see IOException
     * @see ReentrantLock#lock()
     * @see ReentrantLock#unlock()
     * @see Condition#await()
     * @see ThreadControl
     */
    @Override
    public Result download(String url, int depth) {
        Resources resources = new Resources(threadControl);
        try {
            resources.getLocks().lock();
            threadControl.downloadThreads.submit(new DownloadWorker(url, 1, depth, resources));
            while (resources.getTasks().get() > 0) {
                resources.getDone().await();
            }
        } catch (InterruptedException ignored) {
        } finally {
            resources.getLocks().unlock();
        }
        return new Result((new ArrayList<>(resources.getLinks())), resources.getErrors());
    }

    /**
     * Close all working threads
     * @since 1.0
     * @see ThreadControl#close()
     */
    @Override
    public void close() {
        threadControl.close();
    }

    private void decrementTasks(Resources resources) {
        if (resources.getTasks().decrementAndGet() == 0) {
            try {
                resources.getLocks().lock();
                resources.getDone().signal();
            } finally {
                resources.getLocks().unlock();
            }
        }
    }

    /**
     * Download web pages
     * @since 1.0
     */
    private class DownloadWorker implements Runnable {

        private final String url;
        private final int depth;
        private final int maximalDepth;
        private Resources resources;

        private DownloadWorker(String url, int depth, int maximalDepth, Resources resources) {
            this.url = url;
            this.depth = depth;
            this.maximalDepth = maximalDepth;
            this.resources = resources;
            this.resources.getTasks().incrementAndGet();
        }

        /**
         * download web pages starts from given url and to given maximal depth
         * @since 1.0
         * @see ThreadControl#acquire(String)
         * @see ThreadControl#release(String)
         * @see ru.ifmo.ctddev.slyusarenko.crawler.WebCrawler.ExtractorWorker
         */
        @Override
        public void run() {
            try {
                if (resources.getDownloaded().add(url)) {
                    threadControl.acquire(url);
                    Document document = threadControl.downloader.download(url);
                    threadControl.release(url);
                    resources.getLinks().add(url);
                    if (depth < maximalDepth) {
                        threadControl.extractorThreads.submit(new ExtractorWorker(document, depth + 1, maximalDepth, resources));
                    }
                }
            } catch (IOException e) {
                resources.getErrors().put(url, e);
            } finally {
                decrementTasks(resources);
            }
        }
    }

    /**
     * Get links from web pages
     * @since 1.0
     */
    private class ExtractorWorker implements Runnable {

        private final Document document;
        private final int depth;
        private final int maximalDepth;
        private Resources resources;

        private ExtractorWorker(Document document, int depth, int maximalDepth, Resources resources) {
            this.document = document;
            this.depth = depth;
            this.maximalDepth = maximalDepth;
            this.resources = resources;
            this.resources.getTasks().incrementAndGet();
        }

        /**
         * get links from given url and starts to download pages with given urls
         * @since 1.0
         * @see ru.ifmo.ctddev.slyusarenko.crawler.WebCrawler.DownloadWorker
         */
        @Override
        public void run() {
            try {
                List<String> links1 = document.extractLinks();
                for (String link: links1) {
                    threadControl.downloadThreads.submit(new DownloadWorker(link, depth, maximalDepth, resources));
                }
            } catch (IOException ignored) {
            } finally {
                decrementTasks(resources);
            }
        }
    }

}
