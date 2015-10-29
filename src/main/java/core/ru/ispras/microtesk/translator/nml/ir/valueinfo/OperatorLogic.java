/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.valueinfo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.translator.nml.ir.expression.Operands;
import ru.ispras.microtesk.translator.nml.ir.expression.Operator;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

enum OperatorLogic {
  OR(Operator.OR, Arrays.asList(TypeId.BOOL),
    new BinaryAction(Boolean.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return (Boolean) left || (Boolean) right;
      }
    }),

  AND(Operator.AND, Arrays.asList(TypeId.BOOL),
    new BinaryAction(Boolean.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return (Boolean) left && (Boolean) right;
      }
    }),

  BIT_OR(Operator.BIT_OR, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.BOOL),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).or((BigInteger) right);
      }
    },
    new BinaryAction(Boolean.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return (Boolean) left | (Boolean) right;
      }
    }),

  BIT_XOR(Operator.BIT_XOR, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.BOOL),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).xor((BigInteger) right);
      }
    },
    new BinaryAction(Boolean.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return (Boolean) left ^ (Boolean) right;
      }
    }),

  BIT_AND(Operator.BIT_AND, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.BOOL),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).and((BigInteger) right);
      }
    },
    new BinaryAction(Boolean.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return (Boolean) left & (Boolean) right;
      }
    }),

  EQ(Operator.EQ, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT, TypeId.BOOL),
    Boolean.class,
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(final Object left, final Object right) {
        return left.equals(right);
      }
    },
    new BinaryAction(Boolean.class) {
      @Override
      public Object calculate(final Object left, final Object right) {
        return (Boolean) left == (Boolean) right;
      }
    }),

  NOT_EQ(Operator.NOT_EQ, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT, TypeId.BOOL),
    Boolean.class,
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return !left.equals(right);
      }
    },
    new BinaryAction(Boolean.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return (Boolean) left != (Boolean) right;
      }
    }),

  LEQ(Operator.LEQ, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    Boolean.class,
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(final Object left, final Object right) {
        return ((BigInteger) left).compareTo((BigInteger) right) <= 0;
      }
    }),

  GEQ(Operator.GEQ, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    Boolean.class,
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(final Object left, final Object right) {
        return ((BigInteger) left).compareTo((BigInteger) right) >= 0;
      }
    }),

  LESS(Operator.LESS, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    Boolean.class,
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(final Object left, final Object right) {
        return ((BigInteger) left).compareTo((BigInteger) right) < 0;
      }
    }),

  GREATER(Operator.GREATER, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    Boolean.class,
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(final Object left, final Object right) {
        return ((BigInteger) left).compareTo((BigInteger) right) > 0;
      }
    }),

  L_SHIFT(Operator.L_SHIFT, Arrays.asList(TypeId.CARD, TypeId.INT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).shiftLeft(((BigInteger) right).intValue());
      }
    }),

  R_SHIFT(Operator.R_SHIFT, Arrays.asList(TypeId.CARD, TypeId.INT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).shiftRight(((BigInteger) right).intValue());
      }
    }),

  L_ROTATE(Operator.L_ROTATE, Arrays.asList(TypeId.CARD, TypeId.INT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return Integer.rotateLeft(
            ((BigInteger) left).intValue(), ((BigInteger) right).intValue());
      }
    }),

  R_ROTATE(Operator.R_ROTATE, Arrays.asList(TypeId.CARD, TypeId.INT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return Integer.rotateRight(
            ((BigInteger) left).intValue(), ((BigInteger) right).intValue());
      }
    }),

  PLUS(Operator.PLUS, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).add((BigInteger) right);
      }
    }),

  MINUS(Operator.MINUS, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).subtract((BigInteger) right);
      }
    }),

  MUL(Operator.MUL, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).multiply(((BigInteger) right));
      }
    }),

  DIV(Operator.DIV, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).divide((BigInteger) right);
      }
    }),

  MOD(Operator.MOD, Arrays.asList(TypeId.CARD, TypeId.INT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).mod((BigInteger) right);
      }
    }),

  POW(Operator.POW, Arrays.asList(TypeId.CARD, TypeId.INT),
    new BinaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object left, Object right) {
        return ((BigInteger) left).pow(((BigInteger) right).intValue());
      }
    }),

  UPLUS(Operator.UPLUS, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    new UnaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object value) {
        return (BigInteger) value;
      }
    }),

  UMINUS(Operator.UMINUS, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),
    new UnaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object value) {
        return ((BigInteger) value).negate();
      }
    }),

  BIT_NOT(Operator.BIT_NOT, Arrays.asList(TypeId.CARD, TypeId.INT),
    new UnaryAction(BigInteger.class) {
      @Override
      public Object calculate(Object value) {
        return ((BigInteger) value).not();
      }
    }),

  NOT(Operator.NOT, Arrays.asList(TypeId.BOOL),
    new UnaryAction(Boolean.class) {
      @Override
      public Object calculate(Object value) {
        return !((Boolean) value);
      }
    }),

  ITE(Operator.ITE, Arrays.asList(TypeId.BOOL, TypeId.CARD, TypeId.INT)),

  SQRT(Operator.SQRT, Arrays.asList(TypeId.FLOAT));

  private static final Map<Operator, OperatorLogic> operators;
  static {
    operators = new EnumMap<Operator, OperatorLogic>(Operator.class);
    for (OperatorLogic ol : values()) {
      operators.put(ol.operator, ol);
    }

    for (Operator o : Operator.values()) {
      if (!operators.containsKey(o))
        throw new UnsupportedOperationException(
          "No implementation for Operator." + o.name());
    }
  }

  public static OperatorLogic forOperator(Operator op) {
    return operators.get(op);
  }

  private final Operator operator;

  private final Set<TypeId> modelTypes;
  private final Set<Class<?>> nativeTypes;

  private final Type modelResultType;
  private final Class<?> nativeResultType;

  private Map<Class<?>, Action> actions;

  private OperatorLogic(Operator operator, List<TypeId> modelTypes, Action... nativeActions) {
    this(operator, null, modelTypes, null, nativeActions);
  }

  private OperatorLogic(Operator operator, Type modelResultType, List<TypeId> modelTypes,
      Class<?> nativeResultType, Action... nativeActions) {
    if (null == operator) {
      throw new NullPointerException();
    }

    if (null == modelTypes) {
      throw new NullPointerException();
    }

    if (null == nativeActions) {
      throw new NullPointerException();
    }

    this.operator = operator;

    final Set<Class<?>> nativeTypeSet = new HashSet<Class<?>>(nativeActions.length);
    final Map<Class<?>, Action> actionMap = new HashMap<Class<?>, Action>(nativeActions.length);

    for (Action action : nativeActions) {
      if (action.getOperands() != operator.operands()) {
        throw new IllegalArgumentException();
      }

      nativeTypeSet.add(action.getType());
      actionMap.put(action.getType(), action);
    }

    this.modelTypes = EnumSet.copyOf(modelTypes);
    this.nativeTypes = nativeTypeSet;

    this.modelResultType = modelResultType;
    this.nativeResultType = nativeResultType;

    this.actions = actionMap;
  }

  public int operands() {
    return operator.operands();
  }

  public ValueInfo calculate(ValueInfo cast, List<ValueInfo> values) {
    if (!isSupportedFor(cast)) {
      throw new IllegalArgumentException();
    }

    if (cast.isModel()) {
      return (null != modelResultType) ? ValueInfo.createModel(modelResultType) : cast;
    }

    if (!allValuesConstant(values)) {
      return (null != nativeResultType) ? ValueInfo.createNativeType(nativeResultType) : cast;
    }

    final List<Object> nativeValues = new ArrayList<Object>(values.size());

    for (ValueInfo vi : values) {
      nativeValues.add(vi.getNativeValue());
    }

    final Object result = calculateNative(cast.getNativeType(), nativeValues);
    return ValueInfo.createNative(result);
  }

  public boolean isSupportedFor(ValueInfo value) {
    if (value.isNative()) {
      return nativeTypes.contains(value.getNativeType());
    }

    return modelTypes.contains(value.getModelType().getTypeId());
  }

  private static boolean allValuesConstant(List<ValueInfo> values) {
    for (ValueInfo vi : values) {
      if (!vi.isConstant()) {
        return false;
      }
    }

    return true;
  }

  private Object calculateNative(Class<?> type, List<Object> values) {
    final Action action = actions.get(type);

    if (null == action) {
      throw new NullPointerException();
    }

    if (action.getOperands() != values.size()) {
      throw new IllegalArgumentException();
    }

    if (Operands.UNARY.count() == action.getOperands()) {
      return ((UnaryAction) action).calculate(values.get(0));
    }

    return ((BinaryAction) action).calculate(values.get(0), values.get(1));
  }
}
