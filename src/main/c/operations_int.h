#ifndef OPERATIONS_H_INCLUDED
#define OPERATIONS_H_INCLUDED

#include "Location.h"
#include "BitArray/bit_array.h"

Data negate_op(Data l) {
    return Data_constructor(l.val);
}

Data add_op(Data l, Data r) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_add(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data subtract_op(Data l, Data r) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_subtract(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data multiply_op(Data l, Data r) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_multiply(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data divide_op(Data l, Data r) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_add(l.val, DST_ARRAY, r.val);
    return Data_constructor(l.val);
}

Data mod_op(Data l, Data r) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_add(l.val, DST_ARRAY, r.val);
    return Data_constructor(DST_ARRAY);
}

Data pow_op(Data l, Data r) {
    int32_t val = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);
    int32_t exp = bit_array_get_wordn(r.val, 0, r.val->num_of_bits);

    int32_t result = 1;
    for (int i = 0; i < exp; i++) {
        result *= val;
    }
    BIT_ARRAY* DST_ARRAY = bit_array_create(64);
    bit_array_set_word32(DST_ARRAY, 0, result);
    return Data_constructor(DST_ARRAY);
}

Data not_op(Data l) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_not(DST_ARRAY, l.val);
    return Data_constructor(DST_ARRAY);
}

Data and_op(Data l, Data r) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_and(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data or_op(Data l, Data r) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_or(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data xor_op(Data l, Data r) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_xor(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data shiftLeft_op(Data l, Data amount) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    bit_array_shift_left(DST_ARRAY, bit_array_get_wordn(amount.val, 0, amount.val->num_of_bits), 0);
    return Data_constructor(DST_ARRAY);
}

Data shiftRight_op(Data l, Data amount) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    bit_array_shift_right(DST_ARRAY, bit_array_get_wordn(amount.val, 0, amount.val->num_of_bits), 0);
    return Data_constructor(DST_ARRAY);
}

Data rotateLeft_op(Data l, Data amount) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    bit_array_cycle_left(DST_ARRAY, bit_array_get_wordn(amount.val, 0, amount.val->num_of_bits));
    return Data_constructor(DST_ARRAY);
}

Data rotateRight_op(Data l, Data amount) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    bit_array_cycle_right(DST_ARRAY, bit_array_get_wordn(amount.val, 0, amount.val->num_of_bits));
    return Data_constructor(DST_ARRAY);
}

int equals_op(Data l, Data r) {
    return bit_array_get_wordn(l.val, 0, l.val->num_of_bits) == bit_array_get_wordn(r.val, 0, r.val->num_of_bits);
}

int32_t compare_op(Data l, Data r) {
    if (equals_op(l, r)) {
        return 0;
    }

    if (bit_array_get_wordn(l.val, 0, l.val->num_of_bits) > bit_array_get_wordn(r.val, 0, r.val->num_of_bits)) {
        return 1;
    } else {
        return -1;
    }
}

char* toString(Data l) {
    char* str = (char*) malloc(l.val->num_of_bits + 1);
    return bit_array_word2str_rev(l.val, l.val->num_of_bits, str);
}

char* toHexString(Data l) {
    char* str = (char*) malloc(l.val->num_of_bits + 1 + 3);
    str[0] = "H";
    str[1] = "E";
    str[2] = "X";
    return bit_array_word2str_rev(l.val, l.val->num_of_bits, str + 3);
}

int isSigned(Data l) {
    return 0;
}

Data zeroExtend(Data l) {
    BIT_ARRAY* DST_ARRAY = bit_array_create(l.val->num_of_bits);
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    return Data_constructor(DST_ARRAY);
}

#endif
