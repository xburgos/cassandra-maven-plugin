/* $Id: $ */
package com.foo.bar;

import org.apache.commons.lang.StringUtils;


/**
 * This is brute nonesense to demonstrate the maven-solaris-plugin.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 */
public class Bar {

  /**
   * The constructor
   *
   */
  public Bar() {

    super();
  }

  public static void main(String[] args) {

    System.out.println(StringUtils.repeat("This is bar!", 5));
  }
  
}
