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
 * This module is mandatory and is currently implemented by
 * the @ref fast_mem and @ref vfast_mem .
 *
 * @author R. Vaillant
 *
 * @page vfast_mem	vfast_mem module
 *
 * This module aims is to be faster than the default module fast_mem
 * (vfast_mem for Very Fast Memory)
 * Unlike fast_mem.c the hashtable has only one level depth.
 * And endianness are handle differently
 */

#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <memory.h>
#include <gliss/mem.h>
#include <gliss/config.h>

#define little	0
#define big		1

//#define STATS		// define it to collect and print statistics

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


/** MEMORY REPRESENTATION :
 *
 *  Memory is allocated dynamically when needed.
 *  The memory is organized as:
 *
 *  |-------------|
 *  |             |
 *  |-------------|
 *  |             |
 *  |-------------|
 *  |             |
 *  |-------------|
 *  |      x------|-------> (address; bytes mem[MEM_PAGE_SIZE])
 *  |-------------|                         |
 *  |             |                         v
 *  |-------------|         (address; bytes mem[MEM_PAGE_SIZE])
 *  |             |                        ....
 *  |-------------|
 *  |             |
 *  |-------------|
 *        ....
 *         ^                                ^
 *     Hastable                       list of pairs
 *
 *
 *  the hash method works as
 *
 *  memory address is hashed like that :
 *
 *   4bits       16bits          12bits
 *  |-----|------------------|------------|
 *     3           2               1
 *
 * 1 -> the address inside the page
 * 2 -> the address corresponding to the hashtable
 * 3 -> inside the "list of pairs" described overhead
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
 !  The bottom line is : if your are writting something into the memory that is not     !
 !  in the host endianness you need to byte swap it using mem8 or mem buffer operations !
 *																						*
 * SPECIAL: write accross pages															*
 * 		write 4-bits x=ABCD at address p:PAGE-1											*
 * 		x[0]=A, x[1]=B, x[2]=C, x[3]=D													*
 * 	both big endian:																	*
 * 		mem[PAGE-1]=A, mem[PAGE]=B, mem[PAGE + 1]=C, mem[PAGE + 2]=D					*
 * 		<=> p[PAGE-1]=A, p+1[0]=B, p+1[1]=C, p+2[2]=C									*
 * 		<=> mem_write(PAGE - 1, &x, 4)													*
 *	host big, target little:															*
 * 		mem[PAGE-1]=D, mem[PAGE]=C, mem[PAGE + 1]=B, mem[PAGE + 2]=A					*
 * 		<=> p[0]=D, p+1[PAGE - 1]=C, p+1[PAGE - 2]=B, p+1[PAGE - 3]=A					*
 * 		<=> mem_write(PAGE - 1, &x, 4)													*
 *                                                                                      *
 ***************************************************************************************/

// Macros -------------------------------------------------------------------------------

// Fast modulo : FMOD(A, PW2) <=> A % PW2
// PW2 must be a power two
#define FMOD(A, PW2) ((A) & ((PW2) - 1u))

/// WARNING: constant below must be a power of two ! (i.e 2^n)
#define MEM_PAGE_SIZE    4096 // memory page size
#define HASHTABLE_SIZE  65536 // hashtable size
/// constant below are the corresponding exponents "n" :
#define PW_MEM_PAGE_SIZE   12
#define PW_HASHTABLE_SIZE  16

// Compute hash level 1. Corresponding to the 2nd field of addr
// hash1 is the index of the first hashtable
#define COMPUTE_HASH1(addr) FMOD( ((addr) >> PW_MEM_PAGE_SIZE), HASHTABLE_SIZE )

// Memory structures --------------------------------------------------------------------

typedef struct page_entry_t
{
    gliss_address_t      addr;
    struct page_entry_t* next;
    uint8_t*             storage;//[MEM_PAGE_SIZE];// ça change quoi de faire un tableau

} page_entry_t;


/**
 * @typedef gliss_memory_t
 * This type is used to represent a memory space.
 * @ingroup memory
 */
typedef struct gliss_memory_t
{
	void* image_link; /* link to a generic image data resource of the memory
	                     it permits to fetch informations about image structure
	                     via an optionnal external system */
    page_entry_t* hashtable[HASHTABLE_SIZE];
#	ifdef STATS
	int stats_pages[HASHTABLE_SIZE];
	int stats_accesses[HASHTABLE_SIZE];
#	endif
} memory_64_t;

// Functions ----------------------------------------------------------------------------

/**
 * Build a new memory handler.
 * @return	Memory handler or NULL if there is not enough memory.
 * @ingroup memory
 */
gliss_memory_t* gliss_mem_new(void)
{
    unsigned int i;
    memory_64_t*   mem;

    mem = (memory_64_t *)calloc(sizeof(memory_64_t), 1);
    assertp(mem != NULL, "no more memory")
    return (gliss_memory_t*) mem;
}

// --------------------------------------------------------------------------------------
/**
 * Free and delete the given memory.
 * @param memory	Memory to delete.
 * @ingroup memory
 */
void gliss_mem_delete(gliss_memory_t *memory)
{
    int i;
    page_entry_t* current;
    page_entry_t* tmp;

	// dump statistics if activated
#	ifdef STATS
	{
		int sum_pages = memory->stats_pages[0],
			sum_accesses = memory->stats_accesses[0],
			max_pages = memory->stats_pages[0],
			min_pages = memory->stats_pages[0],
			max_accesses = memory->stats_accesses[0],
			min_accesses = memory->stats_accesses[0];
		for(i = 1; i < HASHTABLE_SIZE; i++) {
			int pages = memory->stats_pages[i],
				accesses = memory->stats_accesses[i];
			sum_pages += pages;
			sum_accesses += accesses;
			if(pages < min_pages) min_pages = pages;
			if(pages > max_pages) max_pages = pages;
			if(accesses < min_accesses) min_accesses = accesses;
			if(accesses > max_accesses) max_accesses = accesses;
		}
		printf("pages: %f [%d, %d]\n", (float)sum_pages / HASHTABLE_SIZE, min_pages, max_pages);
		printf("accesses: %f [%d, %d]\n", (float)sum_accesses / HASHTABLE_SIZE, min_accesses, max_accesses);
	}
#	endif

    // get right type
	memory_64_t *mem64 = (memory_64_t *)memory;

    for (i=0; i<HASHTABLE_SIZE; i++)
    {
        current = mem64->hashtable[i];
        while ( current != NULL )
        {
            free(current->storage);
            tmp     = current;
            current = current->next;
            free(tmp);
        }
	}
    free(mem64); // freeing the primary hash table
}
// --------------------------------------------------------------------------------------

/**
 * Get the page matching the given address and create it if it does not exist.
 * @parm mem	Memory to work on.
 * @param addr	Address of the page.
 */
static page_entry_t* mem_get_page(memory_64_t* mem, gliss_address_t addr)
{
    uint32_t      hash1;
    page_entry_t* tmp;
    page_entry_t* entry;
    page_entry_t** h =  mem->hashtable;


    hash1 = COMPUTE_HASH1(addr);
    entry = h[hash1];

    addr = addr & ~(MEM_PAGE_SIZE-1u);

    if( entry == NULL )
    {
        entry = (page_entry_t*)malloc( sizeof(page_entry_t) );
        entry->storage = (uint8_t*)malloc(sizeof(uint8_t) * MEM_PAGE_SIZE);
#		ifndef GLISS_NO_PAGE_INIT
        	memset(entry->storage, 0, sizeof(uint8_t) * MEM_PAGE_SIZE); // TODO : Est-ton obligé de garantir ça ??
#		endif
        entry->next = NULL;
        entry->addr = addr;
        h[hash1] = entry;
#		ifdef STATS
			mem->stats_pages[hash1]++;
#		endif
        return entry;
    }

    if(entry->addr == addr) {
#		ifdef STATS
			mem->stats_accesses[hash1]++;
#		endif
        return entry;
	}

    while(entry->next != NULL)
    {
        if(entry->addr == addr) return entry;
        entry = entry->next;
    }

    if(entry->addr == addr)
        return entry;
    else
    {
        tmp = (page_entry_t*)malloc( sizeof(page_entry_t) );
        tmp->storage = (uint8_t*)malloc(sizeof(uint8_t) * MEM_PAGE_SIZE);
#		ifndef GLISS_NO_PAGE_INIT
        	memset(tmp->storage, 0, sizeof(uint8_t) * MEM_PAGE_SIZE); // TODO : Est-ton obligé de garantir ça ??
#		endif
        entry->next = tmp;
        tmp->next   = NULL;
        tmp->addr   = addr;
#		ifdef STATS
			mem->stats_pages[hash1]++;
			mem->stats_accesses[hash1]++;
#		endif
        return tmp;
    }

    return NULL;
}

// --------------------------------------------------------------------------------------

/**
 * Copy the current memory.
 * @param   memory	Memory to copy.
 * @return			Copied memory or null if there is not enough memory.
 * @ingroup memory
 */
gliss_memory_t* gliss_mem_copy(gliss_memory_t* memory)
{
    unsigned int i, j;
    page_entry_t* src_entry;
    page_entry_t* tgt_entry;

    memory_64_t* source = (memory_64_t*)memory;
    memory_64_t* target = (memory_64_t*)gliss_mem_new();

    for (i=0; i<HASHTABLE_SIZE; i++)
    {
        src_entry = source->hashtable[i];
        while ( src_entry != NULL )
        {
            tgt_entry = mem_get_page(target, src_entry->addr);
            for(j=0; j<MEM_PAGE_SIZE; j++)
                tgt_entry->storage[j] =src_entry->storage[j];

            src_entry = src_entry->next;
        }
    }

    return target;
}

// --------------------------------------------------------------------------------------

/**
 * Write a buffer into memory.
 * @param memory	Memory to write into.
 * @param address	Address in memory to write to.
 * @param buffer	Buffer address in host memory.
 * @param size		Size of the buffer to write.
 * @ingroup memory
 */
void gliss_mem_write(gliss_memory_t* memory, gliss_address_t address, void* buffer, size_t size)
{
    assert(size > 0);
	int i;
    uint32_t      offset = FMOD(address , MEM_PAGE_SIZE);
    uint32_t      sz     = MEM_PAGE_SIZE - offset;
    memory_64_t*  mem    = (memory_64_t *)memory;
    page_entry_t* pte    = mem_get_page(mem, address);;

    if(size > sz)
    {
#       if HOST_ENDIANNESS == TARGET_ENDIANNESS
        memcpy(pte->storage+offset, buffer, sz);
        size    -= sz;
        address += sz;
        buffer   = (uint8_t *)buffer + sz;

        while(size >= MEM_PAGE_SIZE)
        {
            pte = mem_get_page(mem, address);
            memcpy(pte->storage, buffer, MEM_PAGE_SIZE);
            size    -= MEM_PAGE_SIZE;
            address += MEM_PAGE_SIZE;
            buffer   = (uint8_t *)buffer + MEM_PAGE_SIZE;
        }

        if(size > 0)
        {
            pte = mem_get_page(mem, address);
            memcpy(pte->storage, buffer, size);
        }
#       else
		for(i=0; i<size; i++)
        {
            sz--;
            *((uint8_t*)(pte->storage) + sz) = *((uint8_t*)buffer + i);
            address += 1;

            if( sz == 0)
            {
                pte = mem_get_page(mem, address);
                sz  = MEM_PAGE_SIZE;
            }
        }
#       endif
    }
    else
    {
#       if HOST_ENDIANNESS == TARGET_ENDIANNESS
        memcpy(pte->storage + offset, buffer, size);
#       else
        for(i=0; i<size; i++)
            *((uint8_t*)(pte->storage)+sz-1-i) = *((uint8_t*)buffer + i);
#       endif
    }

}
// --------------------------------------------------------------------------------------

/**
 * Read the memory into the given buffer.
 * @param memory	Memory to read in.
 * @param address	Address of the data to read.
 * @param buffer	Buffer to write data in.
 * @param size		Size of the data to read.
 * @ingroup memory
 */
void gliss_mem_read(gliss_memory_t *memory, gliss_address_t address, void *buffer, size_t size)//////////////////////////////
{
    assert(size > 0);
	int i;
    uint32_t      offset = FMOD(address, MEM_PAGE_SIZE);
    uint32_t      sz     = MEM_PAGE_SIZE - offset;
    memory_64_t*  mem    = (memory_64_t *) memory;
    page_entry_t* pte    = mem_get_page(mem, address);


    if(size > sz)
    {
#       if HOST_ENDIANNESS == TARGET_ENDIANNESS
        memcpy(buffer, pte->storage + offset, sz);
        size    -= sz;
        address += sz;
        buffer   = (uint8_t *)buffer + sz;

        while(size >= MEM_PAGE_SIZE)
        {
            pte = mem_get_page(mem, address);
            memcpy(buffer, pte->storage, MEM_PAGE_SIZE);
            size    -= MEM_PAGE_SIZE;
            address += MEM_PAGE_SIZE;
            buffer   = (uint8_t *)buffer + MEM_PAGE_SIZE;
        }

        if(size>0)
        {
            pte = mem_get_page(mem, address);
            memcpy(buffer, pte->storage, size);
        }
#       else
        for(i=0; i<size; i++)
        {
            sz--;
            *((uint8_t*)buffer + i) = *((uint8_t*)(pte->storage) + sz);
            address += 1;

            if( sz == 0)
            {
                pte = mem_get_page(mem, address);
                sz  = MEM_PAGE_SIZE;
            }
        }
#       endif
    }

    else
    {
#       if HOST_ENDIANNESS == TARGET_ENDIANNESS
        memcpy(buffer, pte->storage + offset, size);
#       else
        for(i=0; i<size; i++)
             *((uint8_t*)buffer + i) = *((uint8_t*)(pte->storage)+sz-1-i);
#       endif
    }

}
// --------------------------------------------------------------------------------------

/**
 * Read an 8-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint8_t gliss_mem_read8(gliss_memory_t *mem, gliss_address_t address)
{
    page_entry_t* pte    = mem_get_page(mem, address);
    gliss_address_t offset = FMOD(address, MEM_PAGE_SIZE);

#   if HOST_ENDIANNESS != TARGET_ENDIANNESS
    return pte->storage[MEM_PAGE_SIZE-1 - offset];
#   else
    return pte->storage[offset];
#endif
}
// --------------------------------------------------------------------------------------

/**
 * Read a 16-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint16_t gliss_mem_read16(gliss_memory_t *mem, gliss_address_t address) {

	/* get page */
    gliss_address_t offset = FMOD(address, MEM_PAGE_SIZE);
    page_entry_t*   pte    = mem_get_page(mem, address);

#   if HOST_ENDIANNESS != TARGET_ENDIANNESS
    uint8_t* p = pte->storage + (MEM_PAGE_SIZE-2 - offset);
#   else
    uint8_t* p = pte->storage + offset;
#   endif

    // aligned or cross-page ?
    if(!((offset & 0x1) | ((offset + 1) & MEM_PAGE_SIZE)))
        return *(uint16_t *)p;
	else {
		union {
			uint8_t bytes[2];
			uint16_t half;
		} val;
        gliss_mem_read(mem, address, val.bytes, 2);
        return val.half;
    }
}
// --------------------------------------------------------------------------------------

/**
 * Read a 32-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint32_t gliss_mem_read32(gliss_memory_t *mem, gliss_address_t address)
{

	/* get page */
    gliss_address_t offset = FMOD(address, MEM_PAGE_SIZE);
    page_entry_t* pte    = mem_get_page(mem, address);

#   if HOST_ENDIANNESS != TARGET_ENDIANNESS
    uint8_t* p = pte->storage + (MEM_PAGE_SIZE-4 - offset);
#   else
    uint8_t* p = pte->storage + offset;
#   endif

    // aligned ?
    if(!((offset & 0x3) | ((offset + 3) & MEM_PAGE_SIZE)))
        return *(uint32_t *)p;
    // unaligned !
    else
    {
		union {
			uint8_t bytes[4];
			uint32_t word;
		} val;
		gliss_mem_read(mem, address, val.bytes, 4);
        return val.word;
    }
}
// --------------------------------------------------------------------------------------

/**
 * Read a 64-bit integer.
 * @param memory	Memory to work with.
 * @param address	Address of integer to read.
 * @return			Read integer.
 * @ingroup memory
 */
uint64_t gliss_mem_read64(gliss_memory_t *mem, gliss_address_t address) {

	/* get page */
    page_entry_t* pte    = mem_get_page(mem, address);
    gliss_address_t offset = FMOD(address, MEM_PAGE_SIZE);

#   if HOST_ENDIANNESS != TARGET_ENDIANNESS
    uint8_t* p = pte->storage + (MEM_PAGE_SIZE-8 - offset);
#   else
    uint8_t* p = pte->storage + offset;
#endif

    // aligned or cross-page ?
    if(!((offset & 0x7) | ((offset + 7) & MEM_PAGE_SIZE)))
        return *(uint64_t *)p;
    // unaligned !
    else
    {
		union {
			uint8_t bytes[8];
			uint64_t dword;
		} val;
        gliss_mem_read(mem, address, val.bytes, 8);
        return val.dword;
    }
}
// --------------------------------------------------------------------------------------

/**
 * Read a float value.
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
// --------------------------------------------------------------------------------------


/**
 * Read a double float value.
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
// --------------------------------------------------------------------------------------

/**
 * Read a long double float value.
 * @param memory	Memory to work with.
 * @param address	Address of float to read.
 * @return			Read float.
 * @ingroup memory
 */
long double gliss_mem_readld(gliss_memory_t *memory, gliss_address_t address)
{
	assertp(0, "not implemented !");
}
// --------------------------------------------------------------------------------------

/**
 * Write an 8-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write8(gliss_memory_t* mem, gliss_address_t address, uint8_t val)
{
    gliss_address_t offset;
    page_entry_t*   pte;
    pte    = mem_get_page(mem, address);
    offset = FMOD(address, MEM_PAGE_SIZE);


#   if HOST_ENDIANNESS != TARGET_ENDIANNESS
    pte->storage[MEM_PAGE_SIZE-1 - offset] = val;
#   else
    pte->storage[offset] = val;
#   endif
}
// --------------------------------------------------------------------------------------

/**
 * Write a 16-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write16(gliss_memory_t* mem, gliss_address_t address, uint16_t val)
{
    gliss_address_t offset;
    uint16_t* q;

	/* compute address */
    page_entry_t* pte;
    pte = mem_get_page(mem, address);
    offset = FMOD(address, MEM_PAGE_SIZE);

#   if HOST_ENDIANNESS != TARGET_ENDIANNESS
    q = (uint16_t *)(pte->storage + MEM_PAGE_SIZE-2 - offset);
#   else
    q = (uint16_t *)(pte->storage + offset);
# endif

    // aligned or inter-page ?
    if(!((offset & 0x1) | ((offset + 1) & MEM_PAGE_SIZE)))
        *q = val;
    else {
		union val_t {
			uint8_t bytes[2];
			uint16_t half;
		} *p = (union val_t *)&val;
		gliss_mem_write(mem, address, p->bytes, 2);
	}
}
// --------------------------------------------------------------------------------------

/**
 * Write a 32-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write32(gliss_memory_t* mem, gliss_address_t address, uint32_t val) {
	uint32_t* q;
    gliss_address_t offset;
    page_entry_t*   pte;

	/* compute address */
    pte    = mem_get_page(mem, address);
    offset = FMOD(address, MEM_PAGE_SIZE);

#   if HOST_ENDIANNESS != TARGET_ENDIANNESS
    q      = (uint32_t *)(pte->storage + MEM_PAGE_SIZE-4 - offset);
#   else
    q      = (uint32_t *)(pte->storage + offset);
#   endif

    // aligned or cross-page ?
    if(!((offset & 0x3) | ((offset + 3) & MEM_PAGE_SIZE)))
        *q = val;
    else {
		union val_t {
			uint8_t bytes[4];
			uint32_t word;
		} *p = (union val_t *)&val;
        gliss_mem_write(mem, address, p->bytes, 4);
	}
}
// --------------------------------------------------------------------------------------

/**
 * Write a 64-bit integer in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write integer to.
 * @param val		Integer to write.
 * @ingroup memory
 */
void gliss_mem_write64(gliss_memory_t *mem, gliss_address_t address, uint64_t val)
{
    gliss_address_t offset;
    uint64_t* q;

	/* compute address */
    page_entry_t *pte;
    pte = mem_get_page(mem, address);
    offset = FMOD(address, MEM_PAGE_SIZE);

#   if HOST_ENDIANNESS != TARGET_ENDIANNESS
    q = (uint64_t *)(pte->storage + MEM_PAGE_SIZE-8 - offset);
#   else
    q = (uint64_t *)(pte->storage + offset);
#   endif

    // aligned or cross-page?
    if(!((offset & 0x7) | ((offset + 7) & MEM_PAGE_SIZE)))
        *q = val;
    else {
		union val_t {
			uint8_t bytes[8];
			uint64_t dword;
		} *p = (union val_t *)&val;
        gliss_mem_write(mem, address, p->bytes, 8);
	}
}
// --------------------------------------------------------------------------------------

/**
 * Write a float in memory.
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

// --------------------------------------------------------------------------------------

/**
 * Write a double float in memory.
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
// --------------------------------------------------------------------------------------

/**
 * Write a double float in memory.
 * @param memory	Memory to write in.
 * @param address	Address to write float to.
 * @param val		Float to write.
 * @ingroup memory
 */
void gliss_mem_writeld(gliss_memory_t *memory, gliss_address_t address, long double val)
{
	assertp(0, "not implemented");
}

// End of vfast_mem.c -------------------------------------------------------------------
