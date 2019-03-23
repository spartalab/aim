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

import java.util.ArrayList;

/**
 * A registry based on array list.
 */
public class ArrayListRegistry<T> implements Registry<T> {

  /** The initial capacity of the array */
  private static final int INITIAL_CAPACITY = 10;

  /** The initial ID */
  private int initId;
  /** The next ID */
  private int nextId;
  /** The mapping from IDs to objects */
  private ArrayList<T> idToObj = new ArrayList<T>(INITIAL_CAPACITY);

  /**
   * Create an array list registry.
   */
  public ArrayListRegistry() {
    this(0);
  }

  /**
   * Create an array list registry.
   *
   * @param initId  the initial ID
   */
  public ArrayListRegistry(int initId) {
    this.initId = initId;
    this.nextId = initId;
  }

  @Override
  public int register(T obj) {
    int id = nextId++;
    idToObj.add(obj);
    return id;
  }

  @Override
  public boolean isIdExist(int id) {
    return initId <= id && id < nextId;
  }

  @Override
  public T get(int id) {
    if (isIdExist(id)) {
      return idToObj.get(id-initId);
    } else {
      return null;
    }
  }

  @Override
  public int getNewId() {
    int id = nextId++;
    idToObj.add(null);
    return id;
  }

  @Override
  public void set(int id, T obj) {
    assert isIdExist(id);
    idToObj.set(id-initId, obj);
  }

  // for clean up
  @Override
  public void setNull(int id) {
    idToObj.set(id-initId, null);
  }
}
