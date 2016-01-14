/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.antlrex.symbols;

/**
 * The {@link Where} class describes a place in a source file. 
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class Where {
  private final String unit;
  private final int line;
  private final int position;

  /**
   * Constructs the object from unit name, line number and position.
   * 
   * @param unit Source file (unit) name.
   * @param line Number of the line.
   * @param position Position in the line.
   */

  public Where(final String unit, final int line, final int position) {
    this.unit = unit;
    this.line = line;
    this.position = position;
  }

  /**
   * Returns the name of the source file (unit).
   * @return Source file (unit) name.
   */

  public String getUnit() {
    return unit;
  }

  /**
   * Returns the line number.
   * @return Line number.
   */

  public int getLine() {
    return line;
  }

  /**
   * Returns the position in the line.
   * @return Position in the line.
   */

  public int getPosition() {
    return position;
  }

  @Override
  public String toString() {
    return String.format("%s %d:%d", unit, line, position);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + ((unit == null) ? 0 : unit.hashCode());
    result = prime * result + line;
    result = prime * result + position;

    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Where other = (Where) obj;

    if (unit == null) {
      if (other.unit != null) {
        return false;
      }
    } else { 
      if (!unit.equals(other.unit)) {
        return false;
      }
    }

    return this.line == other.line &&
           this.position == other.position;
  }
}
