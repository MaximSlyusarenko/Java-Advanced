package ru.ifmo.ctddev.slyusarenko.crawler;

import info.kgeorgiy.java.advanced.crawler.Downloader;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Resources {

    private Lock locks;
    private Condition done;
    private ThreadControl threadControl;
    private Queue<String> links;
    private AtomicInteger tasks;
    private ConcurrentSkipListSet<String> downloaded;
    private Map<String, IOException> errors;

    public Resources(ThreadControl threadControl) {
        this.threadControl = threadControl;
        locks = new ReentrantLock();
        done = locks.newCondition();
        links = new ConcurrentLinkedQueue<>();
        tasks = new AtomicInteger();
        downloaded = new ConcurrentSkipListSet<>();
        errors = new ConcurrentHashMap<>();
    }

    public Lock getLocks() {
        return locks;
    }

    public Condition getDone() {
        return done;
    }

    public ThreadControl getThreadControl() {
        return threadControl;
    }

    public Queue<String> getLinks() {
        return links;
    }

    public AtomicInteger getTasks() {
        return tasks;
    }

    public ConcurrentSkipListSet<String> getDownloaded() {
        return downloaded;
    }

    public Map<String, IOException> getErrors() {
        return errors;
    }
}
