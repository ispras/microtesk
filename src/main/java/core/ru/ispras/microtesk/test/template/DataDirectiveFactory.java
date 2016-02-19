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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.CollectionUtils;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.memory.AddressTranslator;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;

import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.TestSettings;

public final class DataDirectiveFactory {
  private final MemoryMap memoryMap;

  private final DataDirective header;
  private final Map<String, TypeInfo> types;
  private final String spaceText;
  private final BitVector spaceData;
  private final String ztermStrText;
  private final String nztermStrText;

  private List<String> preceedingLabels;

  private DataDirectiveFactory(
      final MemoryMap memoryMap,
      final String headerText,
      final Map<String, TypeInfo> types,
      final String spaceText,
      final BitVector spaceData,
      final String ztermStrText,
      final String nztermStrText) {
    InvariantChecks.checkNotNull(memoryMap);
    InvariantChecks.checkNotNull(headerText);
    InvariantChecks.checkNotNull(types);

    this.memoryMap = memoryMap;
    this.header = new Text(headerText);
    this.types = types;
    this.spaceText = spaceText;
    this.spaceData = spaceData;
    this.ztermStrText = ztermStrText;
    this.nztermStrText = nztermStrText;

    this.preceedingLabels = Collections.emptyList();
  }

  public static final class Builder {
    private final MemoryMap memoryMap;
    private final int addressableUnitBitSize;
    private final String headerText;

    private final Map<String, TypeInfo> types;
    private String spaceText;
    private BitVector spaceData;
    private String ztermStrText;
    private String nztermStrText;

    protected Builder(
        final MemoryMap memoryMap,
        final int addressableUnitBitSize,
        final String headerText) {
      InvariantChecks.checkNotNull(memoryMap);
      InvariantChecks.checkGreaterThanZero(addressableUnitBitSize);
      InvariantChecks.checkNotNull(headerText);

      this.memoryMap = memoryMap;
      this.addressableUnitBitSize = addressableUnitBitSize;

      this.headerText = headerText;
      this.types = new HashMap<>();
      this.spaceText = null;
      this.spaceData = null;
      this.ztermStrText = null;
      this.nztermStrText = null;
    }

    public void defineType(
        final String id,
        final String text,
        final String typeName,
        final int[] typeArgs) {
      InvariantChecks.checkNotNull(id);
      InvariantChecks.checkNotNull(text);
      InvariantChecks.checkNotNull(typeName);
      InvariantChecks.checkNotNull(typeArgs);

      final Type type = Type.typeOf(typeName, typeArgs);
      Logger.debug("Defining %s as %s ('%s')...", type, id, text);

      types.put(id, new TypeInfo(type, text));
    }

    public void defineSpace(
        final String id,
        final String text,
        final BigInteger fillWith) {
      InvariantChecks.checkNotNull(id);
      InvariantChecks.checkNotNull(text);
      InvariantChecks.checkNotNull(fillWith);

      Logger.debug("Defining space as %s ('%s') filled with %x...", id, text, fillWith);

      spaceText = text;
      spaceData = BitVector.valueOf(fillWith, addressableUnitBitSize);
    }

    public void defineAsciiString(
        final String id,
        final String text,
        final boolean zeroTerm) {
      InvariantChecks.checkNotNull(id);
      InvariantChecks.checkNotNull(text);

      Logger.debug("Defining %snull-terminated ASCII string as %s ('%s')...",
          zeroTerm ? "" : "not ", id, text);

      if (zeroTerm) {
        ztermStrText = text;
      } else {
        nztermStrText = text;
      }
    }

    public DataDirectiveFactory build() {
      return new DataDirectiveFactory(
          memoryMap,
          headerText,
          types,
          spaceText,
          spaceData,
          ztermStrText,
          nztermStrText
          );
    }
  }

  public final static class TypeInfo {
    public final Type type;
    public final String text;

    private TypeInfo(final Type type, final String text) {
      InvariantChecks.checkNotNull(type);
      InvariantChecks.checkNotNull(text);

      this.type = type;
      this.text = text;
    }
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
    public void apply(final MemoryAllocator allocator) {
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
    public void apply(final MemoryAllocator allocator) {
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
    public void apply(final MemoryAllocator allocator) {
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
    public void apply(final MemoryAllocator allocator) {
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
      for (int index = 0; index < strings.length; index++) {
        if (index > 0) {
          sb.append(',');
        }
        sb.append(String.format(" \"%s\"", strings[index]));
      }
      return sb.toString();
    }

    @Override
    public boolean needsIndent() {
      return true;
    }

    @Override
    public void apply(final MemoryAllocator allocator) {
      for (int index = 0; index < strings.length; index++) {
        final BigInteger address =
            allocator.allocateAsciiString(strings[index], zeroTerm);

        if (0 == index) {
          linkLabelsToAddress(labels, address);
        }
      }
    }

    @Override
    public String toString() {
      return getText();
    }
  }

  private final class Data implements DataDirective {
    private final String typeText;
    private final List<BitVector> values;
    private final List<String> labels;

    private Data(
        final String typeText,
        final List<BitVector> values,
        final List<String> labels) {
      InvariantChecks.checkNotNull(typeText);
      InvariantChecks.checkNotEmpty(values);
      InvariantChecks.checkNotNull(labels);

      this.typeText = typeText;
      this.values = values;
      this.labels = labels;
    }

    @Override
    public String getText() {
      final StringBuilder sb = new StringBuilder(typeText);

      boolean isFirst = true;
      for (final BitVector value : values) {
        if (isFirst) { 
          isFirst = false;
        } else {
          sb.append(',');
        }

        sb.append(" 0x");
        sb.append(value.toHexString());
      }

      return sb.toString();
    }

    @Override
    public boolean needsIndent() {
      return true;
    }

    @Override
    public void apply(final MemoryAllocator allocator) {
      boolean isFirst = true;
      for (final BitVector value : values) {
        final BigInteger address = allocator.allocate(value);
        if (isFirst) {
          linkLabelsToAddress(labels, address);
          isFirst = false;
        }
      }
    }

    @Override
    public String toString() {
      return getText();
    }
  }

  public DataDirective getHeader() {
    return header;
  }

  public DataDirective newText(final String text) {
    return new Text(text);
  }

  public DataDirective newComment(final String text) {
    return new Comment(text);
  }

  public DataDirective newLabel(final String name) {
    InvariantChecks.checkNotNull(name);
    preceedingLabels = CollectionUtils.appendToList(preceedingLabels, name);
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
    InvariantChecks.checkTrue(zeroTerm ? ztermStrText != null : nztermStrText != null);

    final DataDirective result = new AsciiStrings(zeroTerm, strings, preceedingLabels);
    preceedingLabels = Collections.emptyList();

    return result;
  }

  public DataDirective newData(final String typeName, final BigInteger[] values) {
    final TypeInfo typeInfo = findTypeInfo(typeName);
    return newData(typeInfo, values);
  }

  public DataDirective newData(final TypeInfo typeInfo, final BigInteger[] values) {
    InvariantChecks.checkNotNull(typeInfo);
    InvariantChecks.checkNotEmpty(values);

    final List<BitVector> valueList = new ArrayList<>(values.length);
    for (final BigInteger value : values) {
      final BitVector data = BitVector.valueOf(value, typeInfo.type.getBitSize());
      valueList.add(data);
    }

    final DataDirective result = new Data(typeInfo.text, valueList, preceedingLabels);
    preceedingLabels = Collections.emptyList();

    return result;
  }

  public DataDirective newData(
      final String typeName, final DataGenerator generator, final int count) {
    final TypeInfo typeInfo = findTypeInfo(typeName);
    return newData(typeInfo, generator, count);
  }

  public DataDirective newData(
      final TypeInfo typeInfo, final DataGenerator generator, final int count) {
    InvariantChecks.checkNotNull(typeInfo);
    InvariantChecks.checkNotNull(generator);
    InvariantChecks.checkGreaterThanZero(count);

    final List<BitVector> values = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      values.add(generator.nextData());
    }

    final DataDirective result = new Data(typeInfo.text, values, preceedingLabels);
    preceedingLabels = Collections.emptyList();

    return result;
  }

  public TypeInfo findTypeInfo(final String typeName) {
    InvariantChecks.checkNotNull(typeName);
    final TypeInfo typeInfo = types.get(typeName);

    if (null == typeInfo) {
      throw new GenerationAbortedException(
          String.format("The %s data type is not defined.", typeName));
    }

    return typeInfo;
  }

  public TypeInfo findTypeInfo(final int typeSizeInBytes) {
    InvariantChecks.checkGreaterThanZero(typeSizeInBytes);

    final int bitSize = typeSizeInBytes * 8;
    for (final TypeInfo typeInfo : types.values()) {
      if (bitSize == typeInfo.type.getBitSize()) {
        return typeInfo;
      }
    }

    throw new GenerationAbortedException(
        String.format("No %d-byte type is defined.", typeSizeInBytes));
  }

  private void linkLabelsToAddress(
      final List<String> labels,
      final BigInteger physicalAddress) {
    for (final String label : labels) {
      final BigInteger virtuaAddress = AddressTranslator.get().physicalToVirtual(physicalAddress);
      memoryMap.addLabel(label, virtuaAddress);
    }
  }
}
