/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.agent.internal.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.applicationinsights.agent.internal.exporter.models.TelemetryExceptionDetails;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExceptionsTest {

  @Test
  void testMinimalParse() {
    // given
    String str = toString(new IllegalStateException("test"));

    // when
    List<TelemetryExceptionDetails> list = Exceptions.minimalParse(str);

    // then
    assertThat(list.size()).isEqualTo(1);

    TelemetryExceptionDetails details = list.get(0);
    assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
    assertThat(details.getMessage()).isEqualTo("test");
  }

  @Test
  void testMinimalParseWithNoMessage() {
    // given
    String str = toString(new IllegalStateException());

    // when
    List<TelemetryExceptionDetails> list = Exceptions.minimalParse(str);

    // then
    assertThat(list.size()).isEqualTo(1);

    TelemetryExceptionDetails details = list.get(0);
    assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
    assertThat(details.getMessage()).isEqualTo(IllegalStateException.class.getName());
  }

  @Test
  void testMinimalParseWithProblematicMessage() {
    // given
    String str = toString(new ProblematicException());

    // when
    List<TelemetryExceptionDetails> list = Exceptions.minimalParse(str);

    // then
    assertThat(list.size()).isEqualTo(1);

    TelemetryExceptionDetails details = list.get(0);
    assertThat(details.getTypeName()).isEqualTo(ProblematicException.class.getName());
    assertThat(details.getMessage()).isEqualTo(ProblematicException.class.getName());
  }

  @Test
  void testFullParse() {
    // given
    String str = toString(new IllegalStateException("test"));

    // when
    List<TelemetryExceptionDetails> list = Exceptions.fullParse(str);

    // then
    assertThat(list.size()).isEqualTo(1);

    TelemetryExceptionDetails details = list.get(0);
    assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
    assertThat(details.getMessage()).isEqualTo("test");
  }

  @Test
  void testFullParseWithNoMessage() {
    // given
    String str = toString(new IllegalStateException());

    // when
    List<TelemetryExceptionDetails> list = Exceptions.fullParse(str);

    // then
    assertThat(list.size()).isEqualTo(1);

    TelemetryExceptionDetails details = list.get(0);
    assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
    assertThat(details.getMessage()).isEqualTo(IllegalStateException.class.getName());
  }

  @Test
  void testFullParseWithProblematicMessage() {
    // given
    String str = toString(new ProblematicException());

    // when
    List<TelemetryExceptionDetails> list = Exceptions.fullParse(str);

    // then
    assertThat(list.size()).isEqualTo(1);

    TelemetryExceptionDetails details = list.get(0);
    assertThat(details.getTypeName()).isEqualTo(ProblematicException.class.getName());
    assertThat(details.getMessage()).isEqualTo(ProblematicException.class.getName());
  }

  @Test
  void testWithCausedBy() {
    // given
    RuntimeException causedBy = new RuntimeException("the cause");
    String str = toString(new IllegalStateException("test", causedBy));

    // when
    List<TelemetryExceptionDetails> list = Exceptions.fullParse(str);

    // then
    assertThat(list.size()).isEqualTo(2);

    TelemetryExceptionDetails details = list.get(0);
    assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
    assertThat(details.getMessage()).isEqualTo("test");

    TelemetryExceptionDetails causedByDetails = list.get(1);
    assertThat(causedByDetails.getTypeName()).isEqualTo(RuntimeException.class.getName());
    assertThat(causedByDetails.getMessage()).isEqualTo("the cause");
  }

  @Test
  void shouldIgnoreSuppressed() {
    // given
    RuntimeException suppressed = new RuntimeException("the suppressed");
    IllegalStateException exception = new IllegalStateException("test");
    exception.addSuppressed(suppressed);
    String str = toString(exception);

    // when
    List<TelemetryExceptionDetails> list = Exceptions.fullParse(str);

    // then
    assertThat(list.size()).isEqualTo(1);

    TelemetryExceptionDetails details = list.get(0);
    assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
    assertThat(details.getMessage()).isEqualTo("test");
  }

  private static String toString(Throwable t) {
    StringWriter out = new StringWriter();
    t.printStackTrace(new PrintWriter(out));
    return out.toString();
  }

  @SuppressWarnings("OverrideThrowableToString")
  private static class ProblematicException extends Exception {
    @Override
    public String toString() {
      return ProblematicException.class.getName() + ":";
    }
  }
}
