package org.ofbiz.core.rules.parse;

import java.util.*;
import org.ofbiz.core.rules.utensil.*;

/**
 * <p><b>Title:</b> Parser Tester
 * <p><b>Description:</b> None
 * <p>Copyright (c) 1999 Steven J. Metsker.
 * <p>Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * <br>
 * <p>This class generates random language elements for a
 * parser and tests that the parser can accept them.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */
public abstract class ParserTester {
  protected Parser p;
  protected boolean logTestStrings = true;
  /**
   * Constructs a tester for the given parser.
   */
  public ParserTester(Parser p) {
    this.p = p;
  }
  /**
   * Subclasses must override this, to produce an assembly
   * from the given (random) string.
   */
  protected abstract Assembly assembly(String s);
  /**
   * Generate a random language element, and return true if
   * the parser cannot unambiguously parse it.
   */
  protected boolean canGenerateProblem(int depth) {
    String s = p.randomInput(depth, separator());
    logTestString(s);
    Assembly a = assembly(s);
    a.setTarget(freshTarget());
    List in = new ArrayList();
    in.add(a);
    List out = completeMatches(p.match(in));
    if (out.size() != 1) {
      logProblemFound(s, out.size());
      return true;
    }
    return false;
  }
  /**
   * Return a subset of the supplied vector of assemblies,
   * filtering for assemblies that have been completely
   * matched.
   *
   * @param   in   a collection of partially or completely
   *                   matched assemblies
   *
   * @return   a collection of completely matched assemblies
   */
  public static List completeMatches(List in) {
    List out = new ArrayList();
    Enumeration e = Collections.enumeration(in);
    while (e.hasMoreElements()) {
      Assembly a = (Assembly) e.nextElement();
      if (!a.hasMoreElements()) {
        out.add(a);
      }
    }
    return out;
  }
  /**
   * Give subclasses a chance to provide fresh target at
   * the beginning of a parse.
   */
  protected PubliclyCloneable freshTarget() {
    return null;
  }
  /**
   * This method is broken out to allow subclasses to create
   * less verbose tester, or to direct logging to somewhere
   * other than System.out.
   */
  protected void logDepthChange(int depth) {
    System.out.println("Testing depth " + depth + "...");
  }
  /**
   * This method is broken out to allow subclasses to create
   * less verbose tester, or to direct logging to somewhere
   * other than System.out.
   */
  protected void logPassed() {
    System.out.println("No problems found.");
  }
  /**
   * This method is broken out to allow subclasses to create
   * less verbose tester, or to direct logging to somewhere
   * other than System.out.
   */
  protected void logProblemFound(String s, int matchSize) {
    System.out.println("Problem found for string:");
    System.out.println(s);
    if (matchSize == 0) {
      System.out.println(
      "Parser cannot match this apparently " +
      "valid string.");
    } else {
      System.out.println(
      "The parser found " + matchSize +
      " ways to parse this string.");
    }
  }
  /**
   * This method is broken out to allow subclasses to create
   * less verbose tester, or to direct logging to somewhere
   * other than System.out.
   */
  protected void logTestString(String s) {
    if (logTestStrings) {
      System.out.println("    Testing string " + s);
    }
  }
  /**
   * By default, place a blank between randomly generated
   * "words" of a language.
   */
  protected String separator() {
    return " ";
  }
  /**
   * Set the boolean which determines if this class displays
   * every test string.
   *
   * @param   boolean   true, if the user wants to see
   *                    every test string
   */
  public void setLogTestStrings(boolean logTestStrings) {
    this.logTestStrings = logTestStrings;
  }
  /**
   * Create a series of random language elements, and test
   * that the parser can unambiguously parse each one.
   */
  public void test() {
    for (int depth = 2; depth < 8; depth++) {
      logDepthChange(depth);
      for (int k = 0; k < 100; k++) {
        if (canGenerateProblem(depth)) {
          return;
        }
      }
    }
    logPassed();
  }
}
