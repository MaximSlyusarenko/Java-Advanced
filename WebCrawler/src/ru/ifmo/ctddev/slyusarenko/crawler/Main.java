package ru.ifmo.ctddev.slyusarenko.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class to run {@link WebCrawler} from command line
 * @author Maxim Slyusarenko
 * @version 1.0
 * @since 1.0
 */
public class Main {

    public static void main(String[] args) {
        if (args == null || args.length != 4 || args[0] == null || args[1] == null || args[2] == null || args[3] == null) {
            System.err.println("Incorrect arguments");
            return;
        }
        String url = args[0];
        int downloaders = Integer.parseInt(args[1]);
        int extractors = Integer.parseInt(args[2]);
        int perHost = Integer.parseInt(args[3]);
        //List<String> ans = new ArrayList<>();
        try (WebCrawler webCrawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)) {
            //ans = webCrawler.download(url, 2);
            webCrawler.download(url, 2);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        /*try (WebCrawler webCrawler = new WebCrawler(new CachingDownloader(new File(".")), downloaders, extractors, maximalDepth)) {
            ans = webCrawler.download(url, maximalDepth);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }*/
    }
}
