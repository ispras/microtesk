#ifndef LOCATION_H_INCLUDED
#define LOCATION_H_INCLUDED

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "BitArray/bit_array.h"

typedef struct Data Data;

struct Data {
    BIT_ARRAY* val;
};

Data Data_constructor(BIT_ARRAY* val) {
    Data data = {
        .val = val,
    };

    return data;
}

//------------------------------------------------------------------

typedef struct Location Location;

struct Location {
    BIT_ARRAY* storage;
    void (*store)(Location* loc, Data data);
    Data (*load)(Location* loc);
};


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
typedef struct Memory Memory;

struct Memory {
    Location* locations;
    Location (*access)(Memory mem, int index);
    int size;
    char name[24];
};


Memory* Memory_constructor(char* mem_name, int mem_size) {
    Memory* mem = (Memory*) malloc(sizeof(Memory));
    mem->locations = (Location*) malloc(sizeof(Location) * mem_size);
    mem->size = mem_size;


    for (int i = 0; i < mem_size; i++) {
        mem->locations[i] = *Location_constructor(bit_array_create(32));
    }
    strncpy(mem->name, mem_name, 24);

    return mem;
}

typedef struct IsaPrimitive IsaPrimitive;

struct IsaPrimitive {
    void (*init)(IsaPrimitive*, void*);
    void (*action)(IsaPrimitive*, void*, void*);
    char* (*syntax)(IsaPrimitive*, void*);
    char* (*image)(IsaPrimitive*, void*);
    Location* (*access)(IsaPrimitive*, void*, void*);
    IsaPrimitive **args; //REG
    int argc;
    int size;
    BIT_ARRAY* value;
};


Location* access_mem_i(Memory* mem, int index) {
    if (index < mem->size) {
        return &mem->locations[index];
    } else {
        return Location_constructor(bit_array_create(32));
    }
}

Location* access_mem(Memory* mem) {
    return access_mem_i(mem, 0);
}


Location* access_mem_data(Memory* mem, Data data) {
    return Location_constructor(bit_array_create(32));
}

Location* access_isa_params(IsaPrimitive* isa, void* pe__, void* vars__) {
    return isa->access(isa, pe__, vars__);
}

Location* access_isa(IsaPrimitive* isa) {
    return Location_constructor(isa->value);
}


#define ACCESS2(_1, _2) _Generic((_2), \
    int: access_mem_i, \
    Data: access_mem_data \
    )(_1, _2)

#define ACCESS3(...) access_isa_params(__VA_ARGS__)

#define ACCESS1(_1) _Generic((_1), \
    Memory*: access_mem, \
    IsaPrimitive*: access_isa \
    )(_1)

#define GET_ACCESS_MACRO(_1,_2,_3,NAME,...) NAME
#define ACCESS(...) GET_ACCESS_MACRO(__VA_ARGS__, ACCESS3, ACCESS2, ACCESS1)(__VA_ARGS__)


char* image(IsaPrimitive* isa, void* vars__) {
    return isa->image(isa, vars__);

}

char* syntax(IsaPrimitive* isa, void* vars__) {
    return isa->syntax(isa, vars__);
}

char* text(IsaPrimitive* isa, void* vars__) {
    return "TEXT";
}

void action(IsaPrimitive* isa, void* pe__, void* vars__) {
    isa->action(isa, pe__, vars__);
}

Location* bitField(Location* loc, int start, int end) {\
    if (start > end) {
        start = start ^ end;
        end = end ^ start;
        start = start ^ end;
    }
    return Location_constructor(bit_array_field(load(loc).val, start, end));
}

Location* Location_concat(Location* l, Location* r) {
    return Location_constructor(bit_array_concat(load(l).val, load(r).val));
}

void execute(IsaPrimitive* isa, void* pe__, void* vars__) {
    isa->action(isa, pe__, vars__);
}

int bigIntegerValue(Data data) {
    return 42;
}

Data Data_valueOf(char* type, int val) {
    return Data_constructor(bit_array_create(32));
}

IsaPrimitive* Immediate(Location* loc) {
    IsaPrimitive* isa = (IsaPrimitive*) malloc(sizeof(IsaPrimitive));
    isa->value = loc->storage;
    return isa;
}

void init(void* arg) {
    return;
}

void init_isa_data(IsaPrimitive* isa, void* tempVars) {
    isa->init(isa, tempVars);
}

#define INIT2(...) init_isa_data(__VA_ARGS__)

#define INIT1(_1) _Generic((_1), \
    void*: init_void \
    )(_1)

#define GET_INIT_MACRO(_1,_2,NAME,...) NAME
#define INIT(...) GET_INIT_MACRO(__VA_ARGS__, INIT2, INIT1)(__VA_ARGS__)


#endif

