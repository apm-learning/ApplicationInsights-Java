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

package com.microsoft.applicationinsights.agent.internal.common;

import com.microsoft.applicationinsights.agent.internal.configuration.DefaultEndpoints;
import io.netty.handler.ssl.SslHandshakeTimeoutException;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkFriendlyExceptions {

  private static final List<FriendlyExceptionDetector> DETECTORS;
  private static final Logger logger = LoggerFactory.getLogger(NetworkFriendlyExceptions.class);

  static {
    DETECTORS = new ArrayList<>();
    // Note this order is important to determine the right exception!
    // For example SSLHandshakeException extends IOException
    DETECTORS.add(SslExceptionDetector.create());
    DETECTORS.add(UnknownHostExceptionDetector.create());
    try {
      DETECTORS.add(CipherExceptionDetector.create());
    } catch (NoSuchAlgorithmException e) {
      logger.debug(e.getMessage(), e);
    }
  }

  // returns true if the exception was "handled" and the caller should not log it
  public static boolean logSpecialOneTimeFriendlyException(
      Throwable error, String url, AtomicBoolean alreadySeen, Logger logger) {
    return logSpecialOneTimeFriendlyException(error, url, alreadySeen, logger, DETECTORS);
  }

  public static boolean logSpecialOneTimeFriendlyException(
      Throwable error,
      String url,
      AtomicBoolean alreadySeen,
      Logger logger,
      List<FriendlyExceptionDetector> detectors) {

    for (FriendlyExceptionDetector detector : detectors) {
      if (detector.detect(error)) {
        if (!alreadySeen.getAndSet(true)) {
          logger.error(detector.message(url));
        }
        return true;
      }
    }
    return false;
  }

  private static <T extends Exception> T getCausedByOfType(Throwable throwable, Class<T> type) {
    if (type.isInstance(throwable)) {
      @SuppressWarnings("unchecked")
      T ofType = (T) throwable;
      return ofType;
    }
    Throwable cause = throwable.getCause();
    if (cause == null) {
      return null;
    }
    return getCausedByOfType(cause, type);
  }

  private static String getFriendlyExceptionBanner(String url) {
    if (url.contains(DefaultEndpoints.LIVE_ENDPOINT)) {
      return "ApplicationInsights Java Agent failed to connect to Live metric end point.";
    }
    return "ApplicationInsights Java Agent failed to send telemetry data.";
  }

  interface FriendlyExceptionDetector {
    boolean detect(Throwable error);

    String message(String url);
  }

  static class SslExceptionDetector implements FriendlyExceptionDetector {

    static SslExceptionDetector create() {
      return new SslExceptionDetector();
    }

    @Override
    public boolean detect(Throwable error) {
      if (error instanceof SslHandshakeTimeoutException) {
        return false;
      }
      SSLHandshakeException sslException = getCausedByOfType(error, SSLHandshakeException.class);
      return sslException != null;
    }

    @Override
    public String message(String url) {
      return FriendlyException.populateFriendlyMessage(
          "Unable to find valid certification path to requested target.",
          getSslFriendlyExceptionAction(url),
          getFriendlyExceptionBanner(url),
          "This message is only logged the first time it occurs after startup.");
    }

    private static String getJavaCacertsPath() {
      String javaHome = System.getProperty("java.home");
      return new File(javaHome, "lib/security/cacerts").getPath();
    }

    @Nullable
    private static String getCustomJavaKeystorePath() {
      String cacertsPath = System.getProperty("javax.net.ssl.trustStore");
      if (cacertsPath != null) {
        return new File(cacertsPath).getPath();
      }
      return null;
    }

    private static String getSslFriendlyExceptionAction(String url) {
      String customJavaKeyStorePath = getCustomJavaKeystorePath();
      if (customJavaKeyStorePath != null) {
        return "Please import the SSL certificate from "
            + url
            + ", into your custom java key store located at:\n"
            + customJavaKeyStorePath
            + "\n"
            + "Learn more about importing the certificate here: https://go.microsoft.com/fwlink/?linkid=2151450";
      }
      return "Please import the SSL certificate from "
          + url
          + ", into the default java key store located at:\n"
          + getJavaCacertsPath()
          + "\n"
          + "Learn more about importing the certificate here: https://go.microsoft.com/fwlink/?linkid=2151450";
    }
  }

  static class UnknownHostExceptionDetector implements FriendlyExceptionDetector {

    static UnknownHostExceptionDetector create() {
      return new UnknownHostExceptionDetector();
    }

    @Override
    public boolean detect(Throwable error) {
      UnknownHostException unknownHostException =
          getCausedByOfType(error, UnknownHostException.class);
      return unknownHostException != null;
    }

    @Override
    public String message(String url) {
      return FriendlyException.populateFriendlyMessage(
          "Unable to resolve host in end point url",
          getUnknownHostFriendlyExceptionAction(url),
          getFriendlyExceptionBanner(url),
          "This message is only logged the first time it occurs after startup.");
    }

    private static String getUnknownHostFriendlyExceptionAction(String url) {
      return "Please upgrade your network to resolve the host in url :"
          + url
          + "\nLearn more about troubleshooting unknown host exception here: https://go.microsoft.com/fwlink/?linkid=2185830";
    }
  }

  static class CipherExceptionDetector implements FriendlyExceptionDetector {

    private static final List<String> EXPECTED_CIPHERS =
        Arrays.asList(
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
    private final List<String> cipherSuitesFromJvm;

    static CipherExceptionDetector create() throws NoSuchAlgorithmException {
      SSLSocketFactory socketFactory = SSLContext.getDefault().getSocketFactory();
      return new CipherExceptionDetector(Arrays.asList(socketFactory.getSupportedCipherSuites()));
    }

    CipherExceptionDetector(List<String> cipherSuitesFromJvm) {
      this.cipherSuitesFromJvm = cipherSuitesFromJvm;
    }

    @Override
    public boolean detect(Throwable error) {
      IOException exception = getCausedByOfType(error, IOException.class);
      if (exception == null) {
        return false;
      }
      for (String cipher : EXPECTED_CIPHERS) {
        if (cipherSuitesFromJvm.contains(cipher)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public String message(String url) {
      return FriendlyException.populateFriendlyMessage(
          "Probable root cause may be : missing cipher suites which are expected by the requested target.",
          getCipherFriendlyExceptionAction(url),
          getFriendlyExceptionBanner(url),
          "This message is only logged the first time it occurs after startup.");
    }

    private static String getCipherFriendlyExceptionAction(String url) {
      StringBuilder actionBuilder = new StringBuilder();
      actionBuilder
          .append(
              "The Application Insights Java agent detects that you do not have any of the following cipher suites that are supported by the endpoint it connects to: "
                  + url)
          .append("\n");
      for (String missingCipher : EXPECTED_CIPHERS) {
        actionBuilder.append(missingCipher).append("\n");
      }
      actionBuilder.append(
          "Learn more about troubleshooting this network issue related to cipher suites here: https://go.microsoft.com/fwlink/?linkid=2185426");
      return actionBuilder.toString();
    }
  }

  private NetworkFriendlyExceptions() {}
}
