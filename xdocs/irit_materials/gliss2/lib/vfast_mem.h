/*
 *	$Id: fast_mem.h,v 1.4 2009/01/21 07:30:54 casse Exp $
 *	fast_mem module interface
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
#ifndef GLISS_VFAST_MEM_H
#define GLISS_VFAST_MEM_H

#include <stdint.h>
#include <stddef.h>
/*#include "config.h"*/

#if defined(__cplusplus)
    extern  "C" {
#endif

#define GLISS_MEM_STATE
#define GLISS_MEM_INIT(s)
#define GLISS_MEM_DESTROY(s)

#define GLISS_VFAST_MEM
#ifdef GLISS_NO_PAGE_INIT
#	define GLISS_NOBITS_INIT
#endif

typedef uint32_t gliss_address_t;
typedef struct gliss_memory_t gliss_memory_t;

/* creation function */
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

#endif	/* GLISS_VFAST_MEM_H */
