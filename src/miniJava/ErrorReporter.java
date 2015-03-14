/*
 * @(#)ErrorReporter.java                        2.0 1999/08/11
 *
 * Copyright (C) 1999 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package miniJava;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ErrorReporter {

  public int numErrors;

  ErrorReporter() {
    numErrors = 0;
  }

  public void reportError(String message, String tokenName, SourcePosition pos) {
    System.out.print ("*** ");

    for (int p = 0; p < message.length(); p++)
    if (message.charAt(p) == '%')
      System.out.print(tokenName);
    else
      System.out.print(message.charAt(p));    
    numErrors++;
    System.out.println();
  }

  public void reportRestriction(String message) {
    System.out.println("RESTRICTION: " + message);
  }
}
