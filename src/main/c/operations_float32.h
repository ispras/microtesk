#include "Location.h"
#include "BitArray/bit_array.h"
#include "SoftFloat/source/include/softfloat.h"

BIT_ARRAY* DST_ARRAY;

Data negate(Data l) {
    return Data_constructor(l.val);
}

Data add(Data l, Data r) {
    int32_t l_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);
    int32_t r_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);

    float32_t l_float = {.v = l_int,};
    float32_t r_float = {.v = l_int,};
    float32_t res = f32_add(l_float, r_float);

    bit_array_set_word32(DST_ARRAY, 0, res.v);
    return Data_constructor(DST_ARRAY);
}

Data subtract(Data l, Data r) {
    int32_t l_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);
    int32_t r_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);

    float32_t l_float = {.v = l_int,};
    float32_t r_float = {.v = l_int,};
    float32_t res = f32_sub(l_float, r_float);

    bit_array_set_word32(DST_ARRAY, 0, res.v);
    return Data_constructor(DST_ARRAY);
}

Data multiply(Data l, Data r) {
    int32_t l_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);
    int32_t r_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);

    float32_t l_float = {.v = l_int,};
    float32_t r_float = {.v = l_int,};
    float32_t res = f32_mul(l_float, r_float);

    bit_array_set_word32(DST_ARRAY, 0, res.v);
    return Data_constructor(DST_ARRAY);
}

Data divide(Data l, Data r) {
    int32_t l_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);
    int32_t r_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);

    float32_t l_float = {.v = l_int,};
    float32_t r_float = {.v = l_int,};
    float32_t res = f32_div(l_float, r_float);

    bit_array_set_word32(DST_ARRAY, 0, res.v);
    return Data_constructor(DST_ARRAY);
}

//Data mod(Data l, Data r) {
//    bit_array_add(l.val, DST_ARRAY, r.val);
//    return Data_constructor(DST_ARRAY);
//}

Data pow(Data l, Data r) {
    int32_t val = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);
    int32_t exp = bit_array_get_wordn(r.val, 0, r.val->num_of_bits);

    int32_t result = 1;
    for (int i = 0; i < exp; i++) {
        result *= val;
    }

    bit_array_set_word32(DST_ARRAY, 0, result);
    return Data_constructor(DST_ARRAY);
}

Data not_op(Data l) {
    bit_array_not(DST_ARRAY, l.val);
    return Data_constructor(DST_ARRAY);
}

Data and_op(Data l, Data r) {
    bit_array_and(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data or_op(Data l, Data r) {
    bit_array_or(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data xor_op(Data l, Data r) {
    bit_array_xor(DST_ARRAY, l.val, r.val);
    return Data_constructor(DST_ARRAY);
}

Data shiftLeft(Data l, Data amount) {
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    bit_array_shift_left(DST_ARRAY, bit_array_get_wordn(amount.val, 0, amount.val->num_of_bits), 0);
    return Data_constructor(DST_ARRAY);
}

Data shiftRight(Data l, Data amount) {
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    bit_array_shift_right(DST_ARRAY, bit_array_get_wordn(amount.val, 0, amount.val->num_of_bits), 0);
    return Data_constructor(DST_ARRAY);
}

Data rotateLeft(Data l, Data amount) {
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    bit_array_cycle_left(DST_ARRAY, bit_array_get_wordn(amount.val, 0, amount.val->num_of_bits));
    return Data_constructor(DST_ARRAY);
}

Data rotateRight(Data l, Data amount) {
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    bit_array_cycle_right(DST_ARRAY, bit_array_get_wordn(amount.val, 0, amount.val->num_of_bits));
    return Data_constructor(DST_ARRAY);
}

bool equals(Data l, Data r) {
    int32_t l_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);
    int32_t r_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);

    float32_t l_float = {.v = l_int,};
    float32_t r_float = {.v = l_int,};

    return f32_eq(l_float, r_float);
}

int32_t compare(Data l, Data r) {
    if (equals(l, r)) {
        return 0;
    }

    int32_t l_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);
    int32_t r_int = bit_array_get_wordn(l.val, 0, l.val->num_of_bits);

    float32_t l_float = {.v = l_int,};
    float32_t r_float = {.v = l_int,};

    if (f32_lt(l_float, r_float)) {
        return -1;
    } else {
        return 1;
    }
}

char* toString(Data l) {
    //return bit_array_word2str(l, l.val->num_of_bits, str);
    return "Not implement";
}

char* toHexString(Data l) {
    //return bit_array_word2str(l, l.val->num_of_bits, str);
    return "Not implement";
}

bool isSigned(Data l) {
    return false;
}

Data zeroExtend(Data l) {
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    return Data_constructor(DST_ARRAY);
}

Data rotateLeft(Data l) {
    bit_array_copy(DST_ARRAY, 0 ,l.val, 0, l.val->num_of_bits);
    Data_constructor(DST_ARRAY);
}

Location Location_concat(Data l, Data r) {
    return Location_constructor(bit_array_concat(l.val, r.val));
}