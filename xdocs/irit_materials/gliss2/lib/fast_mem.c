/*
 *	$Id: fast_mem.c,v 1.8 2009/11/26 09:01:17 casse Exp $
 *	fast_mem module implementation
 *
 *	This file is part of OTAWA
 *	Copyright (c) 2008, IRIT UPS.
 *
 *	GLISS is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	GLISS is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with OTAWA; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * @defgroup memory Memory Module
 * A memory module is used to simulate the memory behaviour.
 * This module is mandatory and is only currently implemented by
 * the @ref fast_mem .
 *
 * @author M. Finnet, P. Sainrat, H. Casse
 *
 * @page fast_mem	fast_mem module
 *
 * This is the default memory module of GLISS2 and the adaptation of the old
 * memory module of GLISS 1. It implements a fast two-level indrect table
 * to access memory pages.
 */

#define little	0
#define big		1
#include <gliss/config.h>

/**
 * @def gliss_address_t
 * This type represents a 32-bit address in memory space.
 * @ingroup memory
 */

/**
 * @typedef gliss_memory_t
 * This type is used to represent a memory space.
 * @ingroup memory
 */

#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <memory.h>
#include <gliss/mem.h>

#ifndef NDEBUG
#	define assertp(c, m)	\
		if(!(c)) { \
			fprintf(stderr, "assertiion failure %s:%d: %s", __FILE__, __LINE__, m); \
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
/*
#if TARGET_ENDIANNESS == HOST_ENDIANNESS
#	info "endianness equals"
#else
#	info "endianness different"
#endif*/


/* MEMORY REPRESENTATION */

/* WARNING: constant below be multiple of 8 bytes ! */

/* memory page size */
#define MEMORY_PAGE_SIZE 4096
/* primary hash table size */
#define PRIMARYMEMORY_HASH_TABLE_SIZE 4096
/* secondary hash table size */
#define SECONDARYMEMORY_HASH_TABLE_SIZE 16

/*
 * Memory is allocated dynamically when needed.
 * The memory is organized as:
 *
 *  |-------------|
 *  |             |
 *  |-------------|
 *  |             |
 *  |-------------|
 *  |             |
 *  |-------------|
 *  |      -------|------->|-------|
 *  |-------------|        |   ----|---> list of pairs(address +
 *  |             |        |-------|                   MEMORY_PAGE_SIZE bytes)
 *  |-------------|        |       |
 *  |             |        |-------|
 *  |-------------|          ....
 *  |             |
 *  |-------------|
 *      ....
 *   ^ primary hastable       ^ secondary hashtable
 *
 * the hash method works as :
 *
 * memory address is hashed like that
 *
 *  ..----|----|------------|------------|
 *     4    3        2            1
 * 1 -> the address inside the page
 * 2 -> the address corresponding to the primary hash code
 * 3 -> the address corresponding to the secondary hash code
 * 4 -> inside the "list of pairs" described overhead
 */
typedef struct memory_page_table_entry_t  {
	gliss_address_t addr;
	struct memory_page_table_entry_t *next;
	uint8_t *storage;
} memory_page_table_entry_t;

typedef struct  {
	memory_page_table_entry_t *pte[SECONDARYMEMORY_HASH_TABLE_SIZE];
} secondary_memory_hash_table_t;

struct gliss_memory_t {
	void* image_link; /* link to a generic image data resource of the memory
	                     it permits to fetch informations about image structure
	                     via an optionnal external system */
    secondary_memory_hash_table_t *primary_hash_table[PRIMARYMEMORY_HASH_TABLE_SIZE];
};
typedef struct gliss_memory_t memory_64_t;


/**
 * Compute hash level 1.
 * @param addr	Address to hash.
 * @return		Hash value.
 */
static uint32_t mem_hash1(gliss_address_t addr)  {
    return (addr / MEMORY_PAGE_SIZE) % PRIMARYMEMORY_HASH_TABLE_SIZE;
}


/**
 * Compute hash level 2.
 * @param addr	Address to hash.
 * @return		Hash value.
 */
static uint32_t mem_hash2(gliss_address_t addr)  {
    return (addr / MEMORY_PAGE_SIZE / PRIMARYMEMORY_HASH_TABLE_SIZE) % SECONDARYMEMORY_HASH_TABLE_SIZE;
}


/**
 * Build a new memory handler.
 * @return	Memory handler or NULL if there is not enough memory.
 * @ingroup memory
 */
gliss_memory_t *gliss_mem_new(void) {
    memory_64_t *mem;
    mem = (memory_64_t *)malloc(sizeof(memory_64_t));
    if (mem!=NULL){
        memset(mem->primary_hash_table,0,sizeof(mem->primary_hash_table));
        mem->image_link = NULL;
    }
    return (gliss_memory_t *)mem;
}


/**
 * Free and delete the given memory.
 * @param memory	Memory to delete.
 * @ingroup memory
 */
void gliss_mem_delete(gliss_memory_t *memory) {
	int i,j;
	secondary_memory_hash_table_t *secondary_hash_table;
	memory_page_table_entry_t *pte;
	memory_page_table_entry_t *nextpte;

	/* get right type */
	memory_64_t *mem64 = (memory_64_t *)memory;

	for (i=0; i<PRIMARYMEMORY_HASH_TABLE_SIZE;i++)  {
		secondary_hash_table = mem64->primary_hash_table[i];
		if(secondary_hash_table) {
			for(j=0;j<SECONDARYMEMORY_HASH_TABLE_SIZE;j++) {
				pte=secondary_hash_table->pte[j];
				while (pte) {
					nextpte=pte->next;
					free(pte->storage);
					free(pte);	/* freeing each page */
					pte=nextpte;
				}
			}
			free(secondary_hash_table); /* freeing each secondary hash table */
		}
	}
	free(mem64); /* freeing the primary hash table */
}


/**
 * Copy the current memory.
 * @param memory	Memory to copy.
 * @return			Copied meory or null if there is not enough memory.
 * @ingroup memory
 */
gliss_memory_t *gliss_mem_copy(gliss_memory_t *memory) {
	int i,j;
	memory_64_t *mem = memory, *target;

	/* allocate memory */
	target = gliss_mem_new();
	if(target == NULL)
		return NULL;

	/* copy memory */
	for(i=0;i<PRIMARYMEMORY_HASH_TABLE_SIZE;i++) {
		secondary_memory_hash_table_t *secondary_hash_table = mem->primary_hash_table[i];
		if(secondary_hash_table) {
			for(j=0;j<SECONDARYMEMORY_HASH_TABLE_SIZE;j++) {
				memory_page_table_entry_t *pte=secondary_hash_table->pte[i];
				if(pte) {
					do {
						gliss_mem_write(target, pte->addr, pte->storage, MEMORY_PAGE_SIZE);
					} while((pte=pte->next) != 0);
				}
			}
		}
	}
	return target;
}


/**
 * Look for a page in memory.
 * @param mem	Memory to work on.
 * @param addr	Address of the looked page.
 * @ingroup memory
 */
static memory_page_table_entry_t *mem_search_page(memory_64_t *mem, gliss_address_t addr) {
	uint32_t h1;
	uint32_t h2;
	secondary_memory_hash_table_t *secondary_hash_table;
	memory_page_table_entry_t *pte;

	/* computes the first adress of the page */
	addr = addr - (addr%MEMORY_PAGE_SIZE);
	h1 = mem_hash1(addr);
	secondary_hash_table = mem->primary_hash_table[h1];

	/* if the secondary hash table exists */
	if(secondary_hash_table) {

		h2 = mem_hash2(addr);
		pte = secondary_hash_table->pte[h2];

		/* search the page entry */
		if(pte) {
			do  {
				if(pte->addr==addr)
                	return pte;
			} while((pte=pte->next)!=0);
		}
	}
	return 0;
}


/**
 * Get a secondary page table.
 * @parm mem	Memory to work on.
 * @param addr	Address of the page.
 */
static secondary_memory_hash_table_t* mem_get_secondary_hash_table(
	memory_64_t *mem,
	gliss_address_t addr)
{
	uint32_t h1;
	secondary_memory_hash_table_t* secondary_hash_table;

	/* try to fetch the secondary hashtable */
	h1 = mem_hash1(addr);
	secondary_hash_table = mem->primary_hash_table[h1];

	/* if the secondary hashtable does'nt exists */
	if(!secondary_hash_table) {
		/* allocation of the secondary hashtable */
		secondary_hash_table = (secondary_memory_hash_table_t *)
			calloc(sizeof(secondary_memory_hash_table_t),1);

		assertp(secondary_hash_table != NULL,
			"Failed to allocate memory in mem_get_secondary_hash_table\n");
		mem->primary_hash_table[h1]=secondary_hash_table;
	}

	return secondary_hash_table;
}


/**
 * Get the page matching the given address and create it if it does not exist.
 * @parm mem	Memory to work on.
 * @param addr	Address of the page.
 */
static memory_page_table_entry_t *mem_get_page(memory_64_t *mem, gliss_address_t addr) {
	memory_page_table_entry_t *pte;
	uint32_t h2; /* secondary hash table entry # value */
	secondary_memory_hash_table_t *secondary_hash_table;


	/* serach the page */
	addr = addr - (addr%MEMORY_PAGE_SIZE);
	pte = mem_search_page(mem,addr);

	/* if the page doesn't yet exists */
	if(!pte)  {
		secondary_hash_table = mem_get_secondary_hash_table(mem, addr);
		h2 = mem_hash2(addr);

		/* allocation of the page entry descriptor */
		pte = (memory_page_table_entry_t *)malloc(sizeof(memory_page_table_entry_t));
		assertp(pte != NULL, "Failed to allocate memory in mem_get_page\n");
		pte->addr = addr;

		/* allocation of the page */
#		ifndef GLISS_NO_PAGE_INIT
			pte->storage = (uint8_t *)calloc(MEMORY_PAGE_SIZE,1);
#		else
			pte->storage = (uint8_t *)malloc(MEMORY_PAGE_SIZE);
#		endif
		assertp(pte->storage != NULL, "Failed to allocate memory in mem_get_page\n");

		/* adding the memory page to the list of memory page size entry*/
		pte->next = secondary_hash_table->pte[h2];
		secondary_hash_table->pte[h2]=pte;
	}
	return pte;
}


/**
 * Write a buffer into memory.
 * @param memory	Memory to write into.
 * @param address	Address in memory to write to.
 * @param buffer	Buffer address in host memory.
 * @param size		Size of the buffer to write.
 * @ingroup memory
 */
void gliss_mem_write(gliss_memory_t *memory, gliss_address_t address, void *buffer, size_t size) {
	if(size>0) {
		memory_64_t *mem = (memory_64_t *)memory;
		uint32_t offset = address % MEMORY_PAGE_SIZE;
		memory_page_table_entry_t *pte = mem_get_page(mem, address);
        uint32_t sz = MEMORY_PAGE_SIZE - offset;
        if(size > sz) {
			memcpy(pte->storage+offset, buffer, sz);
			size -= sz;
			address += sz;
			buffer = (uint8_t *)buffer + sz;
			if(size>=MEMORY_PAGE_SIZE) {
				do {
					pte = mem_get_page(mem, address);
					memcpy(pte->storage, buffer, MEMORY_PAGE_SIZE);
					size -= MEMORY_PAGE_SIZE;
					address += MEMORY_PAGE_SIZE;
					buffer = (uint8_t *)buffer + MEMORY_PAGE_SIZE;
				} while(size >= MEMORY_PAGE_SIZE);
			}
			if(size > 0) {
				pte=mem_get_page(mem, address);
				memcpy(pte->storage, buffer, size);
			}
        }
		else
			memcpy(pte->storage + offset, buffer, size);
    }
}


/**
 * Read the memory into the given buffer.
 * @param memory	Memory to read in.
 * @param address	Address of the data to read.
 * @param buffer	Buffer to write data in.
 * @param size		Size of the data to read.
 * @ingroup memory
 */
void gliss_mem_read(gliss_memory_t *memory, gliss_address_t address, void *buffer, size_t size) {
	if(size > 0) {
		memory_64_t *mem = (memory_64_t *) memory;
		uint32_t offset = address % MEMORY_PAGE_SIZE;
		memory_page_table_entry_t *pte = mem_get_page(mem, address);
		uint32_t sz = MEMORY_PAGE_SIZE - offset;
		if(size > sz) {
			memcpy(buffer, pte->storage + offset, sz);
			size -= sz;
            address += sz;
			buffer = (uint8_t *)buffer + sz;
			if(size >= MEMORY_PAGE_SIZE) {
				do {
					pte = mem_get_page(mem, address);
					memcpy(buffer, pte->storage, MEMORY_PAGE_SIZE);
					size -= MEMORY_PAGE_SIZE;
					address += MEMORY_PAGE_SIZE;
					buffer = (uint8_t *)buffer + MEMORY_PAGE_SIZE;
				} while(size >= MEMORY_PAGE_SIZE);
			}
			if(size>0) {
				pte = mem_get_page(mem, address);
				memcpy(buffer, pte->storage, size);
			}
		}
		else
			memcpy(buffer, pte->storage + offset, size);
    }
}


/**
 * Read an 8-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint8_t gliss_mem_read8(gliss_memory_t *memory, gliss_address_t address) {
	memory_64_t *mem = (memory_64_t *)memory;
	gliss_address_t offset = address % MEMORY_PAGE_SIZE;
	memory_page_table_entry_t *pte = mem_get_page(mem, address);
	return pte->storage[offset];
}


/**
 * Read a 16-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint16_t gliss_mem_read16(gliss_memory_t *memory, gliss_address_t address) {
	memory_64_t *mem = (memory_64_t *)memory;
	union {
		uint8_t bytes[2];
		uint16_t half;
	} val;
	uint8_t a;

	/* get page */
    gliss_address_t offset = address % MEMORY_PAGE_SIZE;
    memory_page_table_entry_t *pte=mem_get_page(mem, address);
    uint8_t *p = pte->storage + offset;

	/* aligned ? */
	if((address & 0x00000001) == 0)
#		if HOST_ENDIANNESS == TARGET_ENDIANNESS
			return *(uint16_t *)p;
#		else
			val.half = *(uint16_t *)p;
#		endif

	/* unaligned ! */
	else
#		if HOST_ENDIANNESS == TARGET_ENDIANNESS
			{ memcpy(val.bytes, p, 2); return val.half; }
#		else
			memcpy(val.bytes, p, 2);
#		endif

	/* invert */
	a = val.bytes[0];
	val.bytes[0] = val.bytes[1];
	val.bytes[1] = a;
	return val.half;
}


/**
 * Read a 32-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint32_t gliss_mem_read32(gliss_memory_t *memory, gliss_address_t address) {
	memory_64_t *mem = (memory_64_t *)memory;
	union {
		uint8_t bytes[4];
		uint32_t word;
	} val;
	uint8_t a;

	/* get page */
    gliss_address_t offset = address % MEMORY_PAGE_SIZE;
    memory_page_table_entry_t *pte=mem_get_page(mem, address);
    uint8_t *p = pte->storage + offset;

	/* aligned ? */
	if((address & 0x00000003) == 0)
#		if HOST_ENDIANNESS == TARGET_ENDIANNESS
			return *(uint32_t *)p;
#		else
			val.word = *(uint32_t *)p;
#		endif

	/* unaligned ! */
	else
#		if HOST_ENDIANNESS == TARGET_ENDIANNESS
			{ memcpy(val.bytes, p, 4); return val.word; }
#		else
			memcpy(val.bytes, p, 4);
#		endif

	/*Faster byte swapping
    a = (val.word & 0xFF000000u) >> (3*8) |
        (val.word & 0x00FF0000u) >> (1*8) |
        (val.word & 0x0000FF00u) << (1*8) |
        (val.word & 0x000000FFu) << (3*8);

    */
    // Slower byte swapping :
	/* invert */
	a = val.bytes[0];
	val.bytes[0] = val.bytes[3];
	val.bytes[3] = a;
	a = val.bytes[1];
	val.bytes[1] = val.bytes[2];
	val.bytes[2] = a;
	return val.word;
}


/**
 * Read a 64-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint64_t gliss_mem_read64(gliss_memory_t *memory, gliss_address_t address) {
	memory_64_t *mem = (memory_64_t *)memory;
	union {
		uint8_t bytes[8];
		uint64_t dword;
	} val;
	uint8_t a;

	/* get page */
    gliss_address_t offset = address % MEMORY_PAGE_SIZE;
    memory_page_table_entry_t *pte=mem_get_page(mem, address);
    uint8_t *p = pte->storage + offset;

	/* aligned ? */
	if((address & 0x00000007) == 0)
#		if HOST_ENDIANNESS == TARGET_ENDIANNESS
			return *(uint64_t *)p;
#		else
			val.dword = *(uint64_t *)p;
#		endif

	/* unaligned ! */
	else
#		if HOST_ENDIANNESS == TARGET_ENDIANNESS
			{ memcpy(val.bytes, p, 8); return val.dword; }
#		else
			memcpy(val.bytes, p, 8);
#		endif

	/* invert */
	a = val.bytes[0];
	val.bytes[0] = val.bytes[7];
	val.bytes[7] = a;
	a = val.bytes[1];
	val.bytes[1] = val.bytes[6];
	val.bytes[6] = a;
	a = val.bytes[2];
	val.bytes[2] = val.bytes[5];
	val.bytes[5] = a;
	a = val.bytes[3];
	val.bytes[3] = val.bytes[4];
	val.bytes[4] = a;
	return val.dword;
}


/**
 * Read a float value.
 * @param memory	Memory to work with.
 * @param address	Address of float to read.
 * @return			Read float.
 * @ingroup memory
 */
float gliss_mem_readf(gliss_memory_t *memory, gliss_address_t address) {
	union {
		uint32_t i;
		float f;
	} val;
	val.i = gliss_mem_read32(memory, address);
	return val.f;
}



/**
 * Read a double float value.
 * @param memory	Memory to work with.
 * @param address	Address of float to read.
 * @return			Read float.
 * @ingroup memory
 */
double gliss_mem_readd(gliss_memory_t *memory, gliss_address_t address) {
	union {
		uint64_t i;
		double f;
	} val;
	val.i = gliss_mem_read64(memory, address);
	return val.f;
}


/**
 * Read a long double float value.
 * @param memory	Memory to work with.
 * @param address	Address of float to read.
 * @return			Read float.
 * @ingroup memory
 */
long double gliss_mem_readld(gliss_memory_t *memory, gliss_address_t address) {
	assertp(0, "not implemented !");
}


/**
 * Write an 8-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write8(gliss_memory_t *memory, gliss_address_t address, uint8_t val) {
	memory_64_t *mem = (memory_64_t *)memory;
	gliss_address_t offset;
	memory_page_table_entry_t *pte;
	offset = address % MEMORY_PAGE_SIZE;
	pte = mem_get_page(mem, address);
	pte->storage[offset] = val;
}


/**
 * Write a 16-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write16(gliss_memory_t *memory, gliss_address_t address, uint16_t val) {
	memory_64_t *mem = (memory_64_t *)memory;
	gliss_address_t offset;
	union val_t {
		uint8_t bytes[2];
		uint16_t half;
	} *p = (union val_t *)&val;
	uint16_t *q;

	/* compute address */
	memory_page_table_entry_t *pte;
	offset = address % MEMORY_PAGE_SIZE;
	pte = mem_get_page(mem, address);
	q = (uint16_t *)(pte->storage + offset);

	/* invert ? */
#	if HOST_ENDIANNESS != TARGET_ENDIANNESS
	{
		uint8_t a = p->bytes[0];
		p->bytes[0] = p->bytes[1];
		p->bytes[1] = a;
	}
#	endif

	/* aligned ? */
	if((address & 0x00000001) == 0)
		*q = p->half;
	else
		memcpy(q, p->bytes, 2);
}


/**
 * Write a 32-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write32(gliss_memory_t *memory, gliss_address_t address, uint32_t val) {
	memory_64_t *mem = (memory_64_t *)memory;
	gliss_address_t offset;
	union val_t {
		uint8_t bytes[4];
		uint32_t word;
	} *p = (union val_t *)&val;
	uint32_t *q;

	/* compute address */
	memory_page_table_entry_t *pte;
	offset = address % MEMORY_PAGE_SIZE;
	pte = mem_get_page(mem, address);
	q = (uint32_t *)(pte->storage + offset);

	/* invert ? */
#	if HOST_ENDIANNESS != TARGET_ENDIANNESS
	{
		uint8_t a = p->bytes[0];
		p->bytes[0] = p->bytes[3];
		p->bytes[3] = a;
		a = p->bytes[1];
		p->bytes[1] = p->bytes[2];
		p->bytes[2] = a;
	}
#	endif

	/* aligned ? */
	if((address & 0x00000003) == 0)
		*q = p->word;
	else
		memcpy(q, p->bytes, 4);
}


/**
 * Write a 64-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write64(gliss_memory_t *memory, gliss_address_t address, uint64_t val) {
	memory_64_t *mem = (memory_64_t *)memory;
	gliss_address_t offset;
	union val_t {
		uint8_t bytes[8];
		uint64_t dword;
	} *p = (union val_t *)&val;
	uint64_t *q;

	/* compute address */
	memory_page_table_entry_t *pte;
	offset = address % MEMORY_PAGE_SIZE;
	pte = mem_get_page(mem, address);
	q = (uint64_t *)(pte->storage + offset);

	/* invert ? */
#	if HOST_ENDIANNESS != TARGET_ENDIANNESS
	{
		uint8_t a = p->bytes[0];
		p->bytes[0] = p->bytes[7];
		p->bytes[7] = a;
		a = p->bytes[1];
		p->bytes[1] = p->bytes[6];
		p->bytes[6] = a;
		a = p->bytes[2];
		p->bytes[2] = p->bytes[5];
		p->bytes[5] = a;
		a = p->bytes[3];
		p->bytes[3] = p->bytes[4];
		p->bytes[4] = a;
	}
#	endif

	/* aligned ? */
	if((address & 0x00000007) == 0)
		*q = p->dword;
	else
		memcpy(q, p->bytes, 8);
}


/**
 * Write a float in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write float to.
 * @param val		Float to write.
 * @ingroup memory
 */
void gliss_mem_writef(gliss_memory_t *memory, gliss_address_t address, float val) {
	union {
		uint32_t i;
		float f;
	} v;
	v.f = val;
	gliss_mem_write32(memory, address, v.i);
}


/**
 * Write a double float in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write float to.
 * @param val		Float to write.
 * @ingroup memory
 */
void gliss_mem_writed(gliss_memory_t *memory, gliss_address_t address, double val) {
	union {
		uint64_t i;
		double f;
	} v;
	v.f = val;
	gliss_mem_write64(memory, address, v.i);
}


/**
 * Write a double float in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write float to.
 * @param val		Float to write.
 * @ingroup memory
 */
void gliss_mem_writeld(gliss_memory_t *memory, gliss_address_t address, long double val) {
	assertp(0, "not implemented");
}

