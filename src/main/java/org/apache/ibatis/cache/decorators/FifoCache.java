/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * FIFO (first in, first out) cache decorator
 *
 * @author Clinton Begin
 */
public class FifoCache implements Cache {

  // 类似于spring中的循环依赖
  private final Cache delegate;
  private final Deque<Object> keyList;
  private int size;

  public FifoCache(Cache delegate) {

    this.delegate = delegate;
    // 使用了 LinkedList
    this.keyList = new LinkedList<Object>();
    // 默认为 1024
    this.size = 1024;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    // 循环 keyList
    cycleKeyList(key);
    delegate.putObject(key, value);
  }

  @Override
  public Object getObject(Object key) {
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyList.clear();
  }

  @Override
  public ReadWriteLock getReadWriteLock() {
    return null;
  }

  private void cycleKeyList(Object key) {
    // <1> 添加到 keyList 对位
    keyList.addLast(key);
    // 超过上限，将队首位移除
    if (keyList.size() > size) {
      Object oldestKey = keyList.removeFirst();
      delegate.removeObject(oldestKey);
    }
  }

}
