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


void Location_store(Location loc, Data data);
Data Location_load(Location loc);


Location Location_constructor(BIT_ARRAY* val) {
    Location loc = {
        .storage = val,
        .store = Location_store,
        .load = Location_load,
    };

    return loc;
}

void Location_store(Location loc, Data data) {
    memcpy(loc.storage, data.val, sizeof(BIT_ARRAY));
}

Data Location_load(Location loc) {
    Data data = Data_constructor(loc.storage);
    return data;
}

mem.access(mem).load()

//------------------------------------------------------------------

typedef struct Memory {
    struct Location* locations;
    struct Location (*access)(struct Memory mem, int index);
    int size;
    char name[24];
    
} Memory;

Location Memory_access(Memory mem, int index);

Memory Memory_constructor(char* mem_name, int mem_size) {
    Memory mem = {
        .locations = (Location*) malloc(sizeof(Location) * mem_size),
        .access = Memory_access,
        .size = mem_size,
    };

    for (int i = 0; i < mem_size; i++) {
        mem.locations[i] = Location_constructor(bit_array_create(32));
    }
    strncpy(mem.name, mem_name, 24);

    return mem;
}

Location Memory_access(Memory mem, int index) {
    if (index < mem.size) {
        return mem.locations[index];
    } else {
        return Location_constructor(bit_array_create(32));
    }
}
