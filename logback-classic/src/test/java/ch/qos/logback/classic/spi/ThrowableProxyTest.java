/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ThrowableProxyTest {

  StringWriter sw = new StringWriter();
  PrintWriter pw = new PrintWriter(sw);

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  public void verify(Throwable t) {
    t.printStackTrace(pw);

    IThrowableProxy tp = new ThrowableProxy(t);

    String result = ThrowableProxyUtil.asString(tp);
    result = result.replace("common frames omitted", "more");

    String expected = sw.toString();

    System.out.println("========expected");
    System.out.println(expected);

    System.out.println("========result");
    System.out.println(result);

    assertEquals(expected, result);
  }

  @Test
  public void smoke() {
    Exception e = new Exception("smoke");
    verify(e);
  }

  @Test
  public void nested() {
    Exception w = null;
    try {
      someMethod();
    } catch (Exception e) {
      w = new Exception("wrapping", e);
    }
    verify(w);
  }

  // see also http://jira.qos.ch/browse/LBCLASSIC-216
  @Test
  public void nullSTE() {
    Throwable t = new Exception("someMethodWithNullException") {
      @Override
      public StackTraceElement[] getStackTrace() {
        return null;
      }
    };
    // we can't test output as Throwable.printStackTrace method uses
    // the private getOurStackTrace method instead of getStackTrace

    // tests  ThrowableProxyUtil.steArrayToStepArray
    new ThrowableProxy(t);

    // tests  ThrowableProxyUtil.findNumberOfCommonFrames
    Exception top = new Exception("top", t);
    new ThrowableProxy(top);
  }

  @Test
  public void multiNested() {
    Exception w = null;
    try {
      someOtherMethod();
    } catch (Exception e) {
      w = new Exception("wrapping", e);
    }
    verify(w);
  }

  void someMethod() throws Exception {
    throw new Exception("someMethod");
  }

  void someMethodWithNullException() throws Exception {
    throw new Exception("someMethodWithNullException") {
      @Override
      public StackTraceElement[] getStackTrace() {
        return null;
      }
    };
  }

  void someOtherMethod() throws Exception {
    try {
      someMethod();
    } catch (Exception e) {
      throw new Exception("someOtherMethod", e);
    }
  }
}
