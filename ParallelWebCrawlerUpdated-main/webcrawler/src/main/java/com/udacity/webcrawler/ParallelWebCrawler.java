package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
public final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private int maxDepth;
  private final ForkJoinPool pool;
  PageParserFactory pageParserFactory;
  private List<Pattern> ignoredUrls;

  @Inject
  ParallelWebCrawler(
          Clock clock,
          PageParserFactory pageParserFactory,
          @MaxDepth int maxDepth,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @IgnoredUrls List<Pattern> ignoredUrls,
          @TargetParallelism int threadCount) {
    this.clock = clock;
    this.pageParserFactory = pageParserFactory;
    this.maxDepth = maxDepth;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.ignoredUrls = ignoredUrls;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    // creating visited urls and wordCountMap inside crawl
    Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<String>());

    Map<String, Integer> wordCountMap = Collections.synchronizedMap(new HashMap<>());

    Logger.getLogger(this.getClass().getName()).info(String.valueOf(Runtime.getRuntime().availableProcessors()));

    Instant crawlDeadline = clock.instant().plus(timeout);
    while(!clock.instant().isAfter(crawlDeadline)){
      for(String url: startingUrls){
        pool.invoke(new WebCrawlerTask(clock, pageParserFactory, url, visitedUrls, ignoredUrls, maxDepth, crawlDeadline, wordCountMap));
      }
    }
      return new CrawlResult.Builder()
              .setWordCounts(WordCounts.sort(wordCountMap, popularWordCount))
              .setUrlsVisited(visitedUrls.size())
              .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
