package com.udacity.webcrawler.parser;

/**
 * A factory interface that supplies instances of {@link PageParser} that havecommon parameters
 * (such as the timeout and ignored words) preset from injected values.
 */
public interface PageParserFactory {

  /**
   * Returns a {@link PageParser} that parses the given {@param url}.
   */
  PageParser get(String url);
}
