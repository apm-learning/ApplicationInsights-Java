package io.opentelemetry.auto.instrumentation.springwebflux.server;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.auto.api.SpanTypes;
import io.opentelemetry.auto.decorator.ServerDecorator;
import io.opentelemetry.trace.Tracer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringWebfluxHttpServerDecorator extends ServerDecorator {
  public static final SpringWebfluxHttpServerDecorator DECORATE =
      new SpringWebfluxHttpServerDecorator();
  public static final Tracer TRACER = OpenTelemetry.getTracerFactory().get("io.opentelemetry.auto");

  @Override
  protected String[] instrumentationNames() {
    return new String[] {"spring-webflux"};
  }

  @Override
  protected String spanType() {
    return SpanTypes.HTTP_SERVER;
  }

  @Override
  protected String component() {
    return "spring-webflux-controller";
  }
}