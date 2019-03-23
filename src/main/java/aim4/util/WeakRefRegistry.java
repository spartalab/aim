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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * A registry based on weak references.
 */
public class WeakRefRegistry<T> implements Registry<T> {

  /** The initial ID */
  private int initId;
  /** The next ID */
  private int nextId;
  /** A mapping from IDs to weak references of objects */
  private Map<Integer,WeakReference<T>> idToObj =
    new HashMap<Integer,WeakReference<T>>();

  /**
   * Create a weak reference registry.
   */
  public WeakRefRegistry() {
    this(0);
  }

  /**
   * Create a weak reference registry.
   *
   * @param initId  the initial ID
   */
  public WeakRefRegistry(int initId) {
    this.initId = initId;
    this.nextId = initId;
  }

  @Override
  public int register(T obj) {
    int id = nextId++;
    idToObj.put(id, new WeakReference<T>(obj));
    return id;
  }

  @Override
  public boolean isIdExist(int id) {
    return initId <= id && id < nextId;
  }

  @Override
  public T get(int id) {
    WeakReference<T> wr = idToObj.get(id);
    if(wr == null) {
      return null;
    } else {
      T obj = wr.get();  // Unwrap the reference
      // If it's null, then the object no longer exists
      if (obj == null) {
        idToObj.remove(id);
        return null;
      } else {
        return obj;
      }
    }
  }

  @Override
  public int getNewId() {
    int id = nextId++;
    idToObj.put(id, null);
    return id;
  }

  @Override
  public void set(int id, T obj) {
    assert idToObj.containsKey(id);
    idToObj.put(id, new WeakReference<T>(obj));
  }

  // for clean up
  @Override
  public void setNull(int id) {
    idToObj.remove(id);
  }
}
