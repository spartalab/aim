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
package aim4.config;

/**
 * A utility class for using with Condor.
 */
public final class Condor {

  /////////////////////////////////
  // CONDOR
  /////////////////////////////////

  /** The ID of the current Condor process. */
  public static final int CONDOR_ID =
      (System.getenv().get("condor_process_id") == null) ? (-1)
      : Integer.parseInt(System.getenv().get("condor_process_id"));

  /** The total number of Condor processes.  */
  public static final int CONDOR_NUM =
      (System.getenv().get("condor_process_num") == null) ? (-1)
      : Integer.parseInt(System.getenv().get("condor_process_num"));

  // Derived properties

  /** Whether the current job is submitted to Condor. */
  public static final boolean IS_CONDOR_EXIST = (CONDOR_ID >= 0);

  /** The common file suffix for condor jobs. */
  public static final String CONDOR_FILE_SUFFIX =
    IS_CONDOR_EXIST ? ("_condor" + CONDOR_ID) : "";

  /** The internal counter for the condorDo construct. */
  private static int condorI = -1;

  /**
   * The condorDo construct returns true if and only if its internal counter is
   * equal to the Condor ID of the current process.  It is intended to be used
   * in loops such that each Condor process would only execute the iterations
   * of the loops if and only if the ID of Condor process is equal to the
   * multiple of the iteration's count.  The objective to evenly partition the
   * iterations for the condor processes. The internal counter is incremented
   * by 1 after every call of condorDo().
   *
   * @return whether the condor process should execute the code block of the
   *         if-statement.
   */
  public static boolean condorDo() {
    if (IS_CONDOR_EXIST) {
      condorI++;
      return (condorI % CONDOR_NUM) == CONDOR_ID;
    } else {
      return true;
    }
  }

  /** Reset the internal counter of the condorDo construct. */
  public static void condorDoReset() { condorI = -1; }


  /////////////////////////////////
  // PRIVATE METHODS
  /////////////////////////////////

  /**
   * This private constructor is intended to forbid the instantiation of this
   * class.
   */
  private Condor() { };

}
