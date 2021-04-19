package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.Writer;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  private void incorrectInterface(String className){
    throw new IllegalArgumentException(className + " does not contain any profiled method (with @Profiled)");
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
//    Objects.requireNonNull(klass);
//    Object proxy;
//    Boolean klassIsProfiledInterfaced = false; //by default it should be fault
//    for(Method method: klass.getMethods()){
//      if(method.isAnnotationPresent(Profiled.class)){
//        klassIsProfiledInterfaced = true;
//      }
//    }
//    if(!klassIsProfiledInterfaced){
////      incorrectInterface(klass.getName());
//      throw new IllegalArgumentException(klass.getName() + " does not contain any profiled method (with @Profiled)");
//    }
//    else {
//      InvocationHandler profilingMethodInterceptor = new ProfilingMethodInterceptor(clock, state, delegate);
//      proxy = Proxy.newProxyInstance(
//                      klass.getClassLoader(),
//                      new Class<?>[]{klass},
//                      profilingMethodInterceptor);
//    }
//    return (T) proxy;
    Objects.requireNonNull(klass);

    // To avoid TestCase Failure, throwing IllegalArgumentException if the wrapped interface does not contain a @Profiled method
    if (Arrays.stream(klass.getMethods()).noneMatch(method -> method.isAnnotationPresent(Profiled.class))) {
      throw new IllegalArgumentException("Class does not contain any @Profiled Annotated methods");
    }

    @SuppressWarnings("unchecked")
    T methodProfilerProxy = (T) Proxy.newProxyInstance(
            klass.getClassLoader(),
            new Class<?>[] { klass },
            new ProfilingMethodInterceptor(clock, state, delegate)
    );

    return methodProfilerProxy;
  }

  @Override
  public void writeData(Path path) {
    Boolean append = false;
    File file = new File(path.toString());
    if(file.exists()) append = true;
    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "writing profiling data to: " + path.toString());
    try (FileWriter fileWriter = new FileWriter(path.toString(), append)){
      state.write(fileWriter);
    } catch (IOException exception) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while writing the results to path: " + exception.getMessage() + "in writeData method");
      exception.printStackTrace();
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
