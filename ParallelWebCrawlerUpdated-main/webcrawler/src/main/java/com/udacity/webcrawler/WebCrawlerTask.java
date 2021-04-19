package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebCrawlerTask extends RecursiveTask<Void> {
    PageParserFactory pageParserFactory;
    Clock clock;
    String url;
    Set<String> visitedUrls;
    List<Pattern> ignoredUrls;
    int maxDepth;
    Instant crawlDeadline;
    Map<String, Integer> wordCountMap;

    public WebCrawlerTask(Clock clock,
                          PageParserFactory pageParserFactory,
                          String url,
                          Set<String> visitedUrls,
                          List<Pattern> ignoredUrls,
                          int maxDepth,
                          Instant crawlDeadline,
                          Map<String, Integer> wordCountMap) {
        this.pageParserFactory = pageParserFactory;
        this.crawlDeadline = crawlDeadline;
        this.clock = clock;
        this.url = url;
        this.visitedUrls = visitedUrls;
        this.ignoredUrls = ignoredUrls;
        this.maxDepth = maxDepth;
        this.wordCountMap = wordCountMap;
    }

    @Override
    protected Void compute() {
        if(visitedUrls.contains(url) || ignoredUrls.contains(Pattern.compile(url)) || maxDepth == 0 || clock.instant().isAfter(crawlDeadline)){
            return null;
        }
        while (clock.instant().isBefore(crawlDeadline)){
            // updating and adding url to the visited url set
            visitedUrls.add(url);
            // parse the page
            PageParser.Result crawlResult = pageParserFactory.get(url).parse();
            // updating the wordCountMap
            crawlResult.getWordCounts().forEach((word, count) ->
                    wordCountMap.compute(word, (w, c) -> Objects.isNull(c) ? count : c + count));

            List<WebCrawlerTask> childWebCrawlerTasks = crawlResult.getLinks()
                    .stream()
                    .map(childURL -> new WebCrawlerTask(clock, pageParserFactory, childURL, visitedUrls, ignoredUrls, maxDepth - 1, crawlDeadline, wordCountMap))
                    .collect(Collectors.toList());
            invokeAll(childWebCrawlerTasks);
        }
        return null;
    }
}
