package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to write a {@link com.udacity.webcrawler.json.CrawlResult} to file.
 */
public final class CrawlResultWriter {
  private final com.udacity.webcrawler.json.CrawlResult result;
  private final static Logger log = Logger.getLogger(CrawlResultWriter.class.getName());

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   */
  public void write(Path path) {
      log.log(Level.INFO, "writing crawl results to path: " + path.toString());
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(path);
    ObjectMapper objectMapper = new ObjectMapper();
    CrawlResultWriter crawlResultWriter = new CrawlResultWriter(result);
    try(FileWriter fileWriter = new FileWriter(path.toString(), true)){
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        objectMapper.writeValue(bufferedWriter, this.result);
        log.log(Level.INFO,"object mapper finished writing crawl results in CrawlResultWriter");
    } catch (IOException ioException) {
        log.log(Level.SEVERE, ioException .toString());
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   */
  public void write(Writer writer){
      log.log(Level.INFO, "writing crawl results to writer");
      // This is here to get rid of the unused variable warning.
      Objects.requireNonNull(writer);
      ObjectMapper objectMapper = new ObjectMapper();
      // to stop the need to throw JsonMappingException
      objectMapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
      try {
          objectMapper.writeValue(new BufferedWriter(writer), this.result);
          log.log(Level.INFO,"object mapper finished writing crawl results in CrawlResultWriter");
      } catch (Exception e){
          log.log(Level.SEVERE, e.toString());
      }
    }
  }
