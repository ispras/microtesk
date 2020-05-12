#include <decoder.h>
#include <stdlib.h>

typedef struct Array Array;

struct Array
{
    Decoder** data;
    size_t size;
    size_t capacity;
};

Array* Array_constructor() {
    Array* arr = (Array*) malloc(sizeof(Array));
    arr->size = 0;
    arr->capacity = 0;
    arr->data = NULL;
    return arr;
}

void push_back(Array* arr, Decoder* decoder) {
    if (arr->size == arr->capacity) {
        arr->data = (Decoder**) realloc(arr->data, sizeof(Decoder*) * arr->capacity);
        arr->capacity = arr->capacity * 2 + 1;
    }
    arr->data[arr->size] = decoder;
    arr->size++;
}

void free_array(Array* arr) {
    free(arr->data);
    free(arr);
}
