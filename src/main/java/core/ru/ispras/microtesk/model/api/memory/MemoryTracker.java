/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api.memory;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link MemoryTracker} class tracks usage of memory regions.
 *  
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class MemoryTracker {
  public static final class Region {
    private final BigInteger startAddress;
    private final BigInteger endAddress;

    public Region(final BigInteger startAddress, final BigInteger endAddress) {
      InvariantChecks.checkNotNull(startAddress);
      InvariantChecks.checkNotNull(endAddress);
      InvariantChecks.checkGreaterOrEq(endAddress, startAddress);

      this.startAddress = startAddress;
      this.endAddress = endAddress;
    }

    public BigInteger getStartAddress() {
      return startAddress;
    }

    public BigInteger getEndAddress() {
      return endAddress;
    }

    @Override
    public String toString() {
      return String.format("[0x%016x..0x%016x)", startAddress, endAddress);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj == null || obj.getClass() != getClass()) {
        return false;
      }

      final Region other = (Region) obj;
      return this.startAddress.equals(other.startAddress) && 
             this.endAddress.equals(other.endAddress);
    }

    @Override
    public int hashCode() {
      return startAddress.hashCode() + 31 * (31 + endAddress.hashCode());
    }

    private Region getOverlapping(final BigInteger startAddress, final BigInteger endAddress) {
      final BigInteger start = max(this.startAddress, startAddress);
      final BigInteger end = min(this.endAddress, endAddress);
      return start.compareTo(end) < 0 ? new Region(start, end) : null;
    }

    private static BigInteger min(final BigInteger first, final BigInteger second) {
      return first.compareTo(second) <= 0 ? first : second;
    }

    private static BigInteger max(final BigInteger first, final BigInteger second) {
      return first.compareTo(second) >= 0 ? first : second;
    }
  }

  private final TreeMap<BigInteger, Region> startAddresses;
  private final TreeMap<BigInteger, Region> endAddresses;

  public MemoryTracker() {
    this.startAddresses = new TreeMap<>();
    this.endAddresses = new TreeMap<>();
  }

  /**
   * Tries to reserve a memory region within the specified address range. If these addresses
   * are already in use, reservation does not occur and the overlapped region is returned.
   * 
   * @param startAddress Start address of the region.
   * @param endAddress End address of a region (excluded).
   * 
   * @return {@code null} if region is reserved successfully or a overlapping region is the
   *         specified address range is already in use.
   */
  public Region use(final BigInteger startAddress, final BigInteger endAddress) {
    InvariantChecks.checkNotNull(startAddress);
    InvariantChecks.checkNotNull(endAddress);
    InvariantChecks.checkGreaterOrEq(endAddress, startAddress);

    // Pre is a region that ends at startAddress or a greater address.
    final Map.Entry<BigInteger, Region> pre = endAddresses.ceilingEntry(startAddress);
    if (null != pre) {
      final Region preOverlapping = pre.getValue().getOverlapping(startAddress, endAddress);
      if (null != preOverlapping) {
        return preOverlapping;
      }
    }

    // Post is a region that starts at endAddress or a lesser address.
    final Map.Entry<BigInteger, Region> post = startAddresses.floorEntry(endAddress);
    if (null != post) {
      final Region postOverlapping = post.getValue().getOverlapping(startAddress, endAddress);
      if (null != postOverlapping) {
        return postOverlapping;
      }
    }

    final BigInteger start;
    if (null != pre && pre.getValue().getEndAddress().equals(startAddress)) {
      start = pre.getValue().getStartAddress();
      endAddresses.remove(pre.getValue().getEndAddress());
    } else {
      start = startAddress;
    }

    final BigInteger end;
    if (null != post && post.getValue().getStartAddress().equals(endAddress)) {
      end = post.getValue().getEndAddress();
      startAddresses.remove(post.getValue().getStartAddress());
    } else {
      end = endAddress;
    }

    final Region region = new Region(start, end);

    startAddresses.put(start, region);
    endAddresses.put(end, region);

    return null;
  }

  /**
   * Checks whether the specified address is location in one
   * of regions in use. 
   *  
   * @param address Address to be checked.
   * @return {@code true} if the address is within one of the used
   *         regions of {@code false} othwerwise.
   */
  public boolean isUsed(final BigInteger address) {
    InvariantChecks.checkNotNull(address);

    // Pre is a region that ends at address or a greater address.
    final Map.Entry<BigInteger, Region> pre = endAddresses.ceilingEntry(address);
    if (null == pre) {
      return false;
    }

    // Post is a region that starts at address or a lesser address.
    final Map.Entry<BigInteger, Region> post = startAddresses.floorEntry(address);
    if (null == post) {
      return false;
    }

    // It must be a single region. Otherwise, address does not fall into it.
    if (pre.getValue() != post.getValue()) {
      return false;
    }

    // End address of the region is excluded.
    return !address.equals(post.getValue().getEndAddress());
  }

  public void reset() {
    startAddresses.clear();
    endAddresses.clear();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final Region region : startAddresses.values()) {
      if (sb.length() != 0) {
        sb.append(System.lineSeparator());
      }
      sb.append(region.toString());
    }
    return sb.toString();
  }
}
