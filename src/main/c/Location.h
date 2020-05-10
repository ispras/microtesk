#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "BitArray/bit_array.h"

typedef struct Data {
    BIT_ARRAY* val;
} Data;

Data Data_constructor(BIT_ARRAY* val) {
    Data data = {
        .val = val,
    };

    return data;
}

//------------------------------------------------------------------

typedef struct Location {
    BIT_ARRAY* storage;
    void (*store)(struct Location loc, Data data);
    Data (*load)(struct Location loc);
} Location;


void store(Location* loc, Data data);
Data load(Location* loc);


Location* Location_constructor(BIT_ARRAY* val) {
    Location* loc = (Location*) malloc(sizeof(Location));

    // Location loc = {
    //     .storage = val,
    //     .store = store,
    //     .load = load,
    // };

    loc->storage = val;
    loc->store = store;
    loc->load = load;

    return loc;
}

void store(Location* loc, Data data) {
    memcpy(loc->storage, data.val, sizeof(BIT_ARRAY));
}

Data load(Location* loc) {
    Data data = Data_constructor(loc->storage);
    return data;
}

//------------------------------------------------------------------

typedef struct Memory {
    struct Location* locations;
    struct Location (*access)(struct Memory mem, int index);
    int size;
    char name[24];

} Memory;


Memory* Memory_constructor(char* mem_name, int mem_size) {
    Memory* mem = malloc(sizeof(Memory));
    mem->locations = (Location*) malloc(sizeof(Location) * mem_size);
    mem->size = mem_size;


    for (int i = 0; i < mem_size; i++) {
        mem->locations[i] = *Location_constructor(bit_array_create(32));
    }
    strncpy(mem->name, mem_name, 24);

    return mem;
}

Location* access_mem_i(Memory* mem, int index) {
    if (index < mem->size) {
        return &mem->locations[index];
    } else {
        return Location_constructor(bit_array_create(32));
    }
}

Location* access_mem(Memory* mem) {
    access_mem_i(mem, 0);
}


Location* access_mem_data(Memory* mem, Data data) {
    return Location_constructor(bit_array_create(32));
}

typedef struct IsaPrimitive {
    void (*action)(struct IsaPrimitive*, PE*, TEMP*);
    int (*syntax)(struct IsaPrimitive*, TEMP*);
    char* (*image)(struct IsaPrimitive*, TEMP*);
    Location* (*access)(struct IsaPrimitive*, PE*, TEMP*);
    struct IsaPrimitive *args; //REG
    int argc;
    int size;
    int value;
} IsaPrimitive;

typedef struct TEMP {
    int value;
} TEMP;

typedef struct PE {
    int value;
} PE;


Location* access_isa(IsaPrimitive* isa, PE* pe__, TEMP* vars__) {
    return isa->access(isa, pe__, vars__);
}


#define FOO2(_1, _2) _Generic((_2), \
    int: access_mem_i, \
    Data: access_mem_data \
    )(_1, _2)

#define FOO3(...) access_isa(__VA_ARGS__)

#define FOO1(_1) access_mem(_1)

#define GET_ACCESS_MACRO(_1,_2,_3,NAME,...) NAME
#define access(...) GET_ACCESS_MACRO(__VA_ARGS__, FOO3, FOO2, FOO1)(__VA_ARGS__)


char* image(IsaPrimitive* isa, TEMP* vars__) {
    isa->image(isa, vars__);
}

char* syntax(IsaPrimitive* isa, TEMP* vars__) {
    isa->syntax(isa, vars__);
}

char* text(IsaPrimitive* isa, TEMP* vars__) {
    return "";
}

Location* bitField(Location* loc, int start, int end) {
    return Location_constructor(bit_array_create(1));
}

Location* Location_concat(Data l, Data r) {
    return Location_constructor(bit_array_concat(l.val, r.val));
}

void execute(IsaPrimitive* isa, PE* pe__, TEMP* vars__) {
    isa->action(isa, pe__, vars__);
}

int bigIntegerValue(Data data) {
    return 42;
}

char* toString(Data l) {
    //return bit_array_word2str(l, l.val->num_of_bits, str);
    return "Not implement";
}

char* toHexString(Data l) {
    //return bit_array_word2str(l, l.val->num_of_bits, str);
    return "Not implement";
}

Data Data_valueOf(char* type, int val) {
    return Data_constructor(bit_array_create(32));
}

