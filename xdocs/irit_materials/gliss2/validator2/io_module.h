#ifndef IO_MODULE_H
#define IO_MODULE_H

/* emulator library must be generated with the memory module "io_mem" */
#include <leon/mem.h>


void gdb_callback(leon_address_t addr, int size, void *data, int type_access);

void debug_callback(leon_address_t addr, int size, void *data, int type_access);

#endif /* IO_MODULE_H */
