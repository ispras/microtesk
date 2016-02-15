/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.CollectionUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.AddressTranslator;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.test.TestSettings;

public final class DataDirectiveFactory {
  private final MemoryMap memoryMap;
  private final MemoryAllocator allocator;
  private final AddressTranslator addressTranslator;

  private String spaceText;
  private BitVector spaceData;
  private String ztermStrText;
  private String nztermStrText;

  private List<String> preceedingLabels;

  private DataDirectiveFactory(
      final MemoryMap memoryMap,
      final MemoryAllocator allocator,
      final AddressTranslator addressTranslator) {
    InvariantChecks.checkNotNull(memoryMap);
    InvariantChecks.checkNotNull(allocator);
    InvariantChecks.checkNotNull(addressTranslator);

    this.memoryMap = memoryMap;
    this.allocator = allocator;
    this.addressTranslator = addressTranslator;

    this.preceedingLabels = Collections.emptyList();
  }

  protected final class Builder {
    
  }

  private static class Text implements DataDirective {
    private final String text;

    private Text(final String text) {
      InvariantChecks.checkNotNull(text);
      this.text = text;
    }

    @Override
    public String getText() {
      return text;
    }

    @Override
    public boolean needsIndent() {
      return true;
    }

    @Override
    public void apply() {
      // Nothing
    }

    @Override
    public String toString() {
      return getText();
    }
  }

  private static final class Comment extends Text {
    private Comment(final String text) {
      super(text);
    }

    @Override
    public String getText() {
      return TestSettings.getCommentToken() + super.getText();
    }
  }

  private static final class Label extends Text {
    private Label(final String name) {
      super(name);
    }

    @Override
    public String getText() {
      return super.getText() + ":";
    }

    @Override
    public boolean needsIndent() {
      return false;
    }
  }

  private final class Origin implements DataDirective {
    private final BigInteger origin;

    private Origin(final BigInteger origin) {
      InvariantChecks.checkNotNull(origin);
      this.origin = origin;
    }

    @Override
    public String getText() {
      return String.format(TestSettings.getOriginFormat(), origin);
    }

    @Override
    public boolean needsIndent() {
      return true;
    }

    @Override
    public void apply() {
      allocator.setOrigin(origin);
    }

    @Override
    public String toString() {
      return getText();
    }
  }

  private final class Align implements DataDirective {
    private final BigInteger alignment;
    private final BigInteger alignmentInBytes;

    private Align(final BigInteger alignment, final BigInteger alignmentInBytes) {
      InvariantChecks.checkNotNull(alignment);
      InvariantChecks.checkNotNull(alignmentInBytes);

      this.alignment = alignment;
      this.alignmentInBytes = alignmentInBytes;
    }

    @Override
    public String getText() {
      return String.format(TestSettings.getAlignFormat(), alignment);
    }

    @Override
    public boolean needsIndent() {
      return true;
    }

    @Override
    public void apply() {
      allocator.align(alignmentInBytes);
    }

    @Override
    public String toString() {
      return String.format("%s %s %d bytes",
          getText(), TestSettings.getCommentToken(), alignmentInBytes);
    }
  }

  private final class Space implements DataDirective {
    private final int length;
    private final List<String> labels;

    private Space(final int length, final List<String> labels) {
      InvariantChecks.checkGreaterThanZero(length);
      InvariantChecks.checkNotNull(labels);

      this.length = length;
      this.labels = labels;
    }

    @Override
    public String getText() {
      return String.format("%s %d", spaceText, length);
    }

    @Override
    public boolean needsIndent() {
      return true;
    }

    @Override
    public void apply() {
      final BigInteger address = allocator.allocate(spaceData, length);
      linkLabelsToAddress(labels, address);
    }

    @Override
    public String toString() {
      return getText();
    }
  }

  private final class AsciiStrings implements DataDirective {
    private final boolean zeroTerm;
    private final String[] strings;
    private final List<String> labels;

    private AsciiStrings(
        final boolean zeroTerm,
        final String[] strings,
        final List<String> labels) {
      InvariantChecks.checkNotEmpty(strings);
      InvariantChecks.checkNotNull(labels);

      this.zeroTerm = zeroTerm;
      this.strings = strings;
      this.labels = labels;
    }

    @Override
    public String getText() {
      final StringBuilder sb = new StringBuilder(zeroTerm ? ztermStrText : nztermStrText);

      for (int i = 0; i < strings.length; i++) {
        if (i > 0) {
          sb.append(',');
        }

        sb.append(String.format(" \"%s\"", strings[i]));
      }

      return sb.toString();
    }

    @Override
    public boolean needsIndent() {
      return true;
    }

    @Override
    public void apply() {
      final BigInteger address = allocator.allocateAsciiString(strings[0], zeroTerm);
      for (int index = 1; index < strings.length; index++) {
        allocator.allocateAsciiString(strings[index], zeroTerm);
      }
      linkLabelsToAddress(labels, address);
    }
  }

  public DataDirective newText(final String text) {
    return new Text(text);
  }

  public DataDirective newComment(final String text) {
    return new Comment(text);
  }

  public DataDirective newLabel(final String name) {
    InvariantChecks.checkNotNull(name);
    CollectionUtils.appendToList(preceedingLabels, name);
    return new Label(name);
  }

  public DataDirective newOrigin(final BigInteger origin) {
    return new Origin(origin);
  }

  public DataDirective newAlign(final BigInteger alignment, final BigInteger alignmentInBytes) {
    return new Align(alignment, alignmentInBytes);
  }

  public DataDirective newSpace(final int length) {
    InvariantChecks.checkNotNull(spaceText);
    InvariantChecks.checkNotNull(spaceData);

    final DataDirective result = new Space(length, preceedingLabels);
    preceedingLabels = Collections.emptyList();

    return result;
  }

  public DataDirective newAsciiStrings(final boolean zeroTerm, final String[] strings) {
    InvariantChecks.checkTrue(zeroTerm && ztermStrText != null);
    InvariantChecks.checkNotNull(!zeroTerm && nztermStrText != null);

    final DataDirective result = new AsciiStrings(zeroTerm, strings, preceedingLabels);
    preceedingLabels = Collections.emptyList();

    return result;
  }

  private void linkLabelsToAddress(
      final List<String> labels,
      final BigInteger physicalAddress) {
    for (final String label : labels) {
      final BigInteger virtuaAddress = addressTranslator.physicalToVirtual(physicalAddress);
      memoryMap.addLabel(label, virtuaAddress);
    }
  }
}
