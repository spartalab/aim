/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import aim4.config.Condor;
import aim4.config.Constants;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class provides helper methods that are used throughout the code.
 */
public class Util {

  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  /** The random seed for all random number generators in the simulation */
  public static long randSeed = (new Random()).nextLong();
  // public static final long randSeed = -6397397808339168785L;

  /** The global random number generator */
  public static final Random random = new Random(randSeed);

  static {
    if (Condor.IS_CONDOR_EXIST) {
      // To make sure different processes on Condor receives different random
      // seeds, different processes sleep for a different time.
      try{
        Thread.sleep(Condor.CONDOR_ID * 10);
      } catch(InterruptedException e){
        // ignore the interruption by another thread
      }
      randSeed = Util.random.nextLong() + Condor.CONDOR_ID;
      Util.random.setSeed(randSeed);
    }

//    if (Debug.IS_PRINT_RANDOM_SEED) {
//      System.err.println("randSeed = " + Util.randSeed + "L");
//    }
  }


  /////////////////////////////////
  // PUBLIC METHODS
  /////////////////////////////////

  //see http://floating-point-gui.de/errors/comparison

  /**
   * Check whether two double values are "nearly" equal.
   *
   * @param a        the first double value
   * @param b        the second double value
   * @param epsilon  the precision
   * @return whether the two values are nearly equal.
   *
   */
  public static boolean nearlyEqual(double a, double b, double epsilon) {
      final double absA = Math.abs(a);
      final double absB = Math.abs(b);
      final double diff = Math.abs(a - b);

      if (a * b == 0) { // a or b or both are zero
          // relative error is not meaningful here
          return diff < (epsilon * epsilon);
      } else { // use relative error
          return diff / (absA + absB) < epsilon;
      }
  }

  /**
   * Whether a floating-point numbers (doubles) is zero.
   *
   * @param a  a double value
   * @return whether the floating-point number is zero.
   */
  public static boolean isDoubleZero(double a) {
    return Math.abs(a) <= Constants.DOUBLE_EQUAL_PRECISION;
  }

  /**
   * Whether a floating-point numbers (doubles) is not zero.
   *
   * @param a  a double value
   * @return whether the floating-point number is not zero.
   */
  public static boolean isDoubleNotZero(double a) {
    return Math.abs(a) > Constants.DOUBLE_EQUAL_PRECISION;
  }

  /**
   * Whether two floating-point numbers (doubles) are equal.
   *
   * @param a  a double value
   * @param b  a double value
   * @return whether the two floating-point numbers are equal.
   */
  public static boolean isDoubleEqual(double a, double b) {
    return Math.abs(a-b) <= Constants.DOUBLE_EQUAL_PRECISION;
  }

  /**
   * Whether two floating-point numbers (doubles) are not equal.
   *
   * @param a  a double value
   * @param b  a double value
   * @return whether the two floating-point numbers are not equal.
   */
  public static boolean isDoubleNotEqual(double a, double b) {
    return Math.abs(a-b) > Constants.DOUBLE_EQUAL_PRECISION;
  }

  /**
   * Whether two floating-point numbers (doubles) are equal.
   *
   * @param a          a double value
   * @param b          a double value
   * @param precision  the bound on of the difference between a and b that
   *                   is considered equal.
   * @return whether the two floating-point numbers are equal.
   */
  public static boolean isDoubleEqual(double a, double b, double precision) {
    return Math.abs(a-b) <= precision;
  }

  /**
   * Whether two floating-point numbers (doubles) are not equal.
   *
   * @param a          a double value
   * @param b          a double value
   * @param precision  the bound on of the difference between a and b that
   *                   is considered equal.
   * @return whether the two floating-point numbers are not equal.
   */
  public static boolean isDoubleNotEqual(double a, double b, double precision) {
    return Math.abs(a-b) > precision;
  }

  /**
   * Whether two floating-point numbers (doubles) are greater than or
   * equal to another.
   *
   * @param a  a double value
   * @param b  a double value
   * @return whether a >= b.
   */
  public static boolean isDoubleEqualOrGreater(double a, double b) {
    return a > b || isDoubleEqual(a,b);
  }

  /**
   * Whether two floating-point numbers (doubles) are less than or
   * equal to another.
   *
   * @param a  a double value
   * @param b  a double value
   * @return whether a <= b.
   */
  public static boolean isDoubleEqualOrLess(double a, double b) {
    return a < b || isDoubleEqual(a,b);
  }

  /**
   * Constrain a double value between a lower and upper bound.  If it is
   * below the minimum value, return the minimum value.  If it is above the
   * maximum value, return the maximum value.  Otherwise, return the original
   * value.
   *
   * @param inputValue the value to constrain
   * @param minValue the lower bound
   * @param maxValue the upper bound
   * @return         a value between the lower and upper bounds
   */
  public static double constrain(double inputValue,
                                 double minValue, double maxValue){
    if(inputValue > maxValue) {
      return maxValue;
    }
    if(inputValue < minValue) {
      return minValue;
    }
    return inputValue;
  }


  /**
   * Recenter a double value between a lower and upper bound.  This means that
   * if a number is between the two bounds, it is returned unchanged, but if
   * it falls out of bounds, it is adjusted by the size of the interval until
   * it fits.  For example, it can be used for for recentering angles
   * between 0 and 2&pi; or 0 and 180.
   *
   * @param inputValue the value to recenter
   * @param minValue the lower bound
   * @param maxValue the upper bound
   * @return         a value between the lower and upper bounds
   */
  public static double recenter(double inputValue,
                                double minValue, double maxValue) {
    while(inputValue < minValue) {
      inputValue += (maxValue - minValue);
    }
    while(inputValue > maxValue) {
      inputValue -= (maxValue - minValue);
    }
    return inputValue;
  }


  /**
   * The sum of a sequence of floating-point numbers
   *
   * @param as  an iterable of double values
   * @return the sum of the double values
   */
  public static double sum(Iterable<Double> as) {
    double sum = 0;
    for(double a : as) {
      sum += a;
    }
    return sum;
  }

  /**
   * The sum of an array of floating-point numbers
   *
   * @param as  an array of double values
   * @return the sum of the double values
   */
  public static double sum(double[] as) {
    double sum = 0;
    for(double a : as) {
      sum += a;
    }
    return sum;
  }


  /**
   * Choose a number according to a finite probability distribution.
   *
   * @param distribution  the probability distribution
   * @return an index of the distribution that is randomly chosen according
   *         to the distribution
   */
  public static int randomIndex(double[] distribution) {
    double a = Util.random.nextDouble();
    for(int i=0; i<distribution.length; i++) {
      a -= distribution[i];
      if (a<0.0) {
        return i;
      }
    }
    throw new IllegalArgumentException("Invalid proportions.");
  }

  /**
   * Choose a number according to a finite probability distribution.
   *
   * @param distribution  the probability distribution
   * @return an index of the distribution that is randomly chosen according
   *         to the distribution
   */
  public static int randomIndex(List<Double> distribution) {
    double a = Util.random.nextDouble();
    for(int i=0; i<distribution.size(); i++) {
      a -= distribution.get(i);
      if (a<0.0) {
        return i;
      }
    }
    throw new IllegalArgumentException("Invalid proportions.");
  }

  /**
   * Concatenate a list of strings.
   *
   * @param strings  a list of string
   * @param sep      the separator
   * @return the concatenation of the list of string.
   */
  public static String concatenate(List<String> strings, String sep) {
    String str = "";
    for(String s : strings) {
      if (str.equals("")) {
        str = s;
      } else {
        str += sep + s;
      }
    }
    return str;
  }

  /**
   * Concatenate an array of strings.
   *
   * @param strings  an array of string
   * @param sep      the separator
   * @return the concatenation of the list of string.
   */
  public static String concatenate(String[] strings, String sep) {
    return concatenate(Arrays.asList(strings), sep);
  }

  /**
   * Read the content of a file into a list of strings.
   *
   * @param inFileName  the name of the file
   * @return the list of strings
   * @throws IOException
   */
  public static List<String> readFileToStrArray(String inFileName) throws
      IOException {
    List<String> result = new LinkedList<String>();
    // FileInputStream fstream = new FileInputStream(inFileName);
    InputStream fstream = Util.class.getResourceAsStream(inFileName);
    if (fstream == null) { System.err.printf("Fuck this: %s\n", inFileName); }
    DataInputStream in = new DataInputStream(fstream);
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    while (true) {
      String strLine = br.readLine();
      if (strLine == null) {
        break;
      }
      result.add(strLine);
    }
    in.close();
    return new ArrayList<String>(result);
  }

  /////////////////////////////////
  // CLASS CONSTRUCTORS
  /////////////////////////////////

  /** This class should never be instantiated. */
  private Util(){};

}
