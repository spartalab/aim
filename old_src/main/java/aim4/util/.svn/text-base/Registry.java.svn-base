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

/**
 * A generic registry.
 *
 * @param <T>  the type of object in the registry
 */
public interface Registry<T> {

  /**
   * Register an object.
   *
   * @param obj  the object
   * @return the ID of the object
   */
  int register(T obj);

  /**
   * Whether or not an object is in the registry.
   *
   * @param id  the ID of the object
   * @return Whether or not an object is in the registry
   */
  boolean isIdExist(int id);

  /**
   * Retrieve an object from the registry.
   *
   * @param id  the ID of the object
   * @return the object
   */
  T get(int id);

  /**
   * Get a new unused ID.
   *
   * @return a new unused ID
   */
  int getNewId();

  /**
   * Associate an object to a given ID.
   *
   * @param id   the ID
   * @param obj  the object
   */
  void set(int id, T obj);

  /**
   * Remove the object associated with a given ID from the registry.
   *
   * @param id  the ID
   */
  void setNull(int id);
}
