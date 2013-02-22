#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <memory.h>
#include <gliss/mem.h>
#include <gliss/config.h>

#define little	0
#define big	1

//#define STATS		// define it to collect and print statistics

#ifndef NDEBUG
#	define assertp(c, m)	\
if(!(c)) { \
           fprintf(stderr, "assertion failure %s:%d: %s", __FILE__, __LINE__, m); \
           abort(); }
#else
#	define assertp(c, m)
#endif


#ifndef TARGET_ENDIANNESS
#	error "TARGET_ENDIANNESS must be defined !"
#endif

#ifndef HOST_ENDIANNESS
#	error "HOST_ENDIANNESS must be defined !"
#endif


/**
 * @defgroup memory Memory Module
 * A memory module is used to simulate the memory behaviour.
 * This module is mandatory and is currently implemented by
 * the @ref fast_mem and @ref vfast_mem .
 *
 * @page mem16	mem16 module
 *
 * handles a 16-bit addressed memory (addresses go from 0 to 2^16 - 1) in a fast way
 */


/** ENDIANNESS HANDLING *****************************************************************
 *                                                                                      *
 *  To avoid byte swapping for each read and write function (when endianness differs)   *
 *  the vfast_mem module read and write backwards into the memory.                      *
 *  For instance when target endiannes != host endianness read8() looks like :          *
 *  read8(addr){ return mem[SIZE_MEM-1-addr] }                                          *
 *                                                                                 ^    *
 *  WARNING : this means that you need to exclusively use byte memorie operations / \   *
 *  when loading a programm into the memory, if endianness differs.              / ! \  *
 *                                                                              *-----* *
 *  For example with an ".elf" target written in big endian                             *
 *  and an host in little endian :                                                      *
 *                                                                                      *
 *  ELF buffer before swapping:            After swapping                               *
 *  adr   ->  0123 4567                      7654 3210                                  *
 *  bytes -> |ABCD|ABCD|                    |DCBA|DCBA|                                 *
 *                                                                                      *
 *  Then every read8/32/64/write8/32/64 can be perform with the host endianness.        *
 *                                                                                      *
 !  The bottom line is : if your are writing something into the memory that is not      !
 !  in the host endianness you need to byte swap it using mem8 or mem buffer operations !
 *                                                                                      *
 ***************************************************************************************/



#define MEM16_SIZE	0x10000
#define ADDR_MASK	0xFFFF
#define TRUNC_ADDR(a)	((a) & ADDR_MASK)

/* memory structure */

typedef struct gliss_memory_t
{
	uint8_t *storage;
} memory_16_t;



/***************************************/
/* creation/destruction/copy functions */
/***************************************/


/**
 * Builds a new memory handler.
 * @return	Memory handler or NULL if there is not enough memory.
 * @ingroup memory
 */
gliss_memory_t *gliss_mem_new(void)
{
	unsigned int i;
	memory_16_t *mem;

	mem = malloc(sizeof(memory_16_t));
	assertp(mem != NULL, "no more memory")
	/* allocate 2^16 bytes right away, no paging system needed */
	mem->storage = malloc(MEM16_SIZE * sizeof(uint8_t));

	return mem;
}


/**
 * Frees and deletes the given memory.
 * @param memory	Memory to delete.
 * @ingroup memory
 */
void gliss_mem_delete(gliss_memory_t *memory)
{
	if (memory) {
		free(memory->storage);
		free(memory);
	}
}


/**
 * Copies the current memory.
 * @param   memory	Memory to copy.
 * @return			Copied memory or null if there is not enough memory.
 * @ingroup memory
 */
gliss_memory_t *gliss_mem_copy(gliss_memory_t *memory)
{
	memory_16_t *target = gliss_mem_new();
	memcpy(target->storage, memory->storage, MEM16_SIZE);
	return target;
}


/*******************************/
/* read/write memory functions */
/*******************************/


/**
 * Writes a buffer into memory.
 * @param memory	Memory to write into.
 * @param address	Address in memory to write to.
 * @param buffer	Buffer address in host memory.
 * @param size		Size of the buffer to write.
 * @ingroup memory
 */
void gliss_mem_write(gliss_memory_t *memory, gliss_address_t address, void *buffer, size_t size)
{
	assert(size > 0);
	int i;
	gliss_address_t offset = TRUNC_ADDR(address);
	uint8_t *buf8 = (uint8_t *)buffer;
	
#if HOST_ENDIANNESS == TARGET_ENDIANNESS
        memcpy(memory->storage + offset, buffer, size);
#else
        for (i = 0; i < size; i++)
		memory->storage[MEM16_SIZE - offset - 1 - i] = buf8[i];
#endif
}


/**
 * Reads the memory into the given buffer.
 * @param memory	Memory to read in.
 * @param address	Address of the data to read.
 * @param buffer	Buffer to write data in.
 * @param size		Size of the data to read.
 * @ingroup memory
 */
void gliss_mem_read(gliss_memory_t *memory, gliss_address_t address, void *buffer, size_t size)
{
	assert(size > 0);
	int i;
	uint32_t offset = TRUNC_ADDR(address);
	uint8_t *buf8 = (uint8_t *)buffer;
	
#if HOST_ENDIANNESS == TARGET_ENDIANNESS
        memcpy(buffer, memory->storage + offset, size);
#else
        for (i = 0; i < size; i++)
		buf8[i] = memory->storage[MEM16_SIZE - offset - 1 - i];
#endif
}


/**
 * Reads an 8-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint8_t gliss_mem_read8(gliss_memory_t *mem, gliss_address_t address)
{
	gliss_address_t offset = TRUNC_ADDR(address);

#if HOST_ENDIANNESS != TARGET_ENDIANNESS
	return mem->storage[MEM16_SIZE - 1 - offset];
#else
	return mem->storage[offset];
#endif
}


/**
 * Reads a 16-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint16_t gliss_mem_read16(gliss_memory_t *mem, gliss_address_t address)
{
	union {
		uint8_t bytes[2];
		uint16_t half;
	} val;

	gliss_address_t offset = TRUNC_ADDR(address);

#if HOST_ENDIANNESS != TARGET_ENDIANNESS
	uint8_t* p = mem->storage + (MEM16_SIZE - 2 - offset);
#else
	uint8_t* p = mem->storage + offset;
#endif
	/* aligned ? */
	if (offset & 0x1 == 0)
		return *(uint16_t *)p;
	/* unaligned ! */
	else
	{
		memcpy(val.bytes, p, 2);
		return val.half;
	}
}


/**
 * Reads a 32-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint32_t gliss_mem_read32(gliss_memory_t *mem, gliss_address_t address)
{
	union {
		uint8_t bytes[4];
		uint32_t word;
	} val;

	gliss_address_t offset = TRUNC_ADDR(address);

#if HOST_ENDIANNESS != TARGET_ENDIANNESS
	uint8_t* p = mem->storage + (MEM16_SIZE - 4 - offset);
#else
	uint8_t* p = pte->storage + offset;
#endif
	/* aligned ? */
	if (offset & 0x3 == 0)
		return *(uint32_t *)p;
	/* unaligned ! */
	else
	{
		memcpy(val.bytes, p, 4);
		return val.word;
	}
}


/**
 * Reads a 64-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint64_t gliss_mem_read64(gliss_memory_t *mem, gliss_address_t address)
{
	union {
		uint8_t bytes[8];
		uint64_t dword;
	} val;

	gliss_address_t offset = TRUNC_ADDR(address);

#if HOST_ENDIANNESS != TARGET_ENDIANNESS
	uint8_t* p = mem->storage + (MEM16_SIZE - 8 - offset);
#else
	uint8_t* p = pte->storage + offset;
#endif
	/* aligned ? */
	if (offset & 0x7 == 0)
		return *(uint64_t *)p;
	/* unaligned ! */
	else
	{
		memcpy(val.bytes, p, 8);
		return val.dword;
	}
}


/**
 * Reads a float value.
 * @param memory	Memory to work with.
 * @param address	Address of float to read.
 * @return			Read float.
 * @ingroup memory
 */
float gliss_mem_readf(gliss_memory_t *memory, gliss_address_t address)
{
	union {
		uint32_t i;
		float f;
	} val;
	val.i = gliss_mem_read32(memory, address);
	return val.f;
}


/**
 * Reads a double float value.
 * @param memory	Memory to work with.
 * @param address	Address of float to read.
 * @return			Read float.
 * @ingroup memory
 */
double gliss_mem_readd(gliss_memory_t *memory, gliss_address_t address)
{
	union {
		uint64_t i;
		double f;
	} val;
	val.i = gliss_mem_read64(memory, address);
	return val.f;
}


/**
 * Reads a long double float value.
 * @param memory	Memory to work with.
 * @param address	Address of float to read.
 * @return			Read float.
 * @ingroup memory
 */
long double gliss_mem_readld(gliss_memory_t *memory, gliss_address_t address)
{
	assertp(0, "not implemented !");
}


/**
 * Writes an 8-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write8(gliss_memory_t* mem, gliss_address_t address, uint8_t val)
{
	gliss_address_t offset = TRUNC_ADDR(address);

#if HOST_ENDIANNESS != TARGET_ENDIANNESS
	mem->storage[MEM16_SIZE - 1 - offset] = val;
#else
	mem->storage[offset] = val;
#endif
}


/**
 * Writes a 16-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write16(gliss_memory_t* mem, gliss_address_t address, uint16_t val)
{
	union val_t {
		uint8_t bytes[2];
		uint16_t half;
	} *p = (union val_t *)&val;
	uint16_t *q;

	gliss_address_t offset = TRUNC_ADDR(address);

#if HOST_ENDIANNESS != TARGET_ENDIANNESS
	q = (uint16_t *)(mem->storage + (MEM16_SIZE - 2 - offset));
#else
	q = (uint16_t *)(mem->storage + offset);
#endif
	/* aligned ? */
	if (offset & 0x1 == 0)
		*q = p->half;
	/* unaligned ! */
	else
		memcpy(q, p->bytes, 2);
}


/**
 * Writes a 32-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write32(gliss_memory_t* mem, gliss_address_t address, uint32_t val)
{
	union val_t {
		uint8_t bytes[4];
		uint32_t word;
	} *p = (union val_t *)&val;
	uint32_t* q;

	gliss_address_t offset = TRUNC_ADDR(address);

#if HOST_ENDIANNESS != TARGET_ENDIANNESS
	q = (uint32_t *)(mem->storage + (MEM16_SIZE - 4 - offset));
#else
	q = (uint32_t *)(mem->storage + offset);
#endif
	/* aligned ? */
	if (offset & 0x3 == 0)
		*q = p->word;
	/* unaligned ! */
	else
		memcpy(q, p->bytes, 4);
}


/**
 * Writes a 64-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write64(gliss_memory_t *mem, gliss_address_t address, uint64_t val)
{
	union val_t {
		uint8_t bytes[8];
		uint64_t dword;
	} *p = (union val_t *)&val;
	uint64_t* q;

	gliss_address_t offset = TRUNC_ADDR(address);

#if HOST_ENDIANNESS != TARGET_ENDIANNESS
	q = (uint64_t *)(mem->storage + (MEM16_SIZE - 8 - offset));
#else
	q = (uint64_t *)(mem->storage + offset);
#endif
	/* aligned ? */
	if (offset & 0x7 == 0)
		*q = p->dword;
	/* unaligned ! */
	else
		memcpy(q, p->bytes, 8);
}


/**
 * Writes a float in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write float to.
 * @param val		Float to write.
 * @ingroup memory
 */
void gliss_mem_writef(gliss_memory_t *memory, gliss_address_t address, float val)
{
	union {
		uint32_t i;
		float f;
	} v;
	v.f = val;
	gliss_mem_write32(memory, address, v.i);
}


/**
 * Writes a double float in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write float to.
 * @param val		Float to write.
 * @ingroup memory
 */
void gliss_mem_writed(gliss_memory_t *memory, gliss_address_t address, double val)
{
	union {
		uint64_t i;
		double f;
	} v;
	v.f = val;
	gliss_mem_write64(memory, address, v.i);
}


/**
 * Writes a double float in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write float to.
 * @param val		Float to write.
 * @ingroup memory
 */
void gliss_mem_writeld(gliss_memory_t *memory, gliss_address_t address, long double val)
{
	assertp(0, "not implemented");
}
