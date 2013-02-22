#ifndef GLISS_MEM16_H
#define GLISS_MEM16_H

#include <stdint.h>
#include <stddef.h>
/*#include "config.h"*/

#if defined(__cplusplus)
    extern  "C" {
#endif

#define GLISS_MEM_STATE
#define GLISS_MEM_INIT(s)
#define GLISS_MEM_DESTROY(s)

#define GLISS_MEM16


typedef uint32_t gliss_address_t;
typedef struct gliss_memory_t gliss_memory_t;

/* creation/copy/destruction functions */
gliss_memory_t *gliss_mem_new(void);
void gliss_mem_delete(gliss_memory_t *memory);
gliss_memory_t *gliss_mem_copy(gliss_memory_t *memory);

/* read functions */
uint8_t gliss_mem_read8(gliss_memory_t *, gliss_address_t);
uint16_t gliss_mem_read16(gliss_memory_t *, gliss_address_t);
uint32_t gliss_mem_read32(gliss_memory_t *, gliss_address_t);
uint64_t gliss_mem_read64(gliss_memory_t *, gliss_address_t);
float gliss_mem_readf(gliss_memory_t *, gliss_address_t);
double gliss_mem_readd(gliss_memory_t *, gliss_address_t);
long double gliss_mem_readld(gliss_memory_t *, gliss_address_t);
void gliss_mem_read(gliss_memory_t *memory, gliss_address_t, void *buf, size_t size);


/* write functions */
void gliss_mem_write8(gliss_memory_t *, gliss_address_t, uint8_t);
void gliss_mem_write16(gliss_memory_t *, gliss_address_t, uint16_t);
void gliss_mem_write32(gliss_memory_t *, gliss_address_t, uint32_t);
void gliss_mem_write64(gliss_memory_t *, gliss_address_t, uint64_t);
void gliss_mem_writef(gliss_memory_t *, gliss_address_t, float);
void gliss_mem_writed(gliss_memory_t *, gliss_address_t, double);
void gliss_mem_writeld(gliss_memory_t *, gliss_address_t, long double);
void gliss_mem_write(gliss_memory_t *memory, gliss_address_t, void *buf, size_t size);

#if defined(__cplusplus)
}
#endif

#endif	/* GLISS_MEM16_H */
