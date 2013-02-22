/*
 *	$Id: old_elf.h,v 1.10 2009/07/21 13:17:58 barre Exp $
 *	old_elf module interface
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
#ifndef GLISS_OLD_ELF_H
#define GLISS_OLD_ELF_H

#include "grt.h"
#include "mem.h"
#include "api.h"

#if defined(__cplusplus)
    extern  "C" {
#endif

#define GLISS_LOADER_STATE
#define GLISS_LOADER_INIT(s)
#define GLISS_LOADER_DESTROY(s)

/* gliss_loader_t type */
typedef struct gliss_loader_t gliss_loader_t;


/* loader management */
gliss_loader_t *gliss_loader_open(const char *path);
void gliss_loader_close(gliss_loader_t *loader);
void gliss_loader_load(gliss_loader_t *loader, gliss_platform_t *pf);
gliss_address_t gliss_loader_start(gliss_loader_t *loader);


/* system initialization (used internally during platform and state initialization) */
gliss_address_t gliss_brk_init(gliss_loader_t *loader);

/* section access */
typedef struct gliss_loader_sect_t {
	const char *name;
	gliss_address_t addr;
	int size;
	enum {
		GLISS_LOADER_SECT_UNKNOWN = 0,
		GLISS_LOADER_SECT_TEXT,
		GLISS_LOADER_SECT_DATA,
		GLISS_LOADER_SECT_BSS
	} type;
} gliss_loader_sect_t;
int gliss_loader_count_sects(gliss_loader_t *loader);
void gliss_loader_sect(gliss_loader_t *loader, int sect, gliss_loader_sect_t *data);

/* symbol access */
typedef struct {
	const char *name;
	gliss_address_t value;
	int size;
	int sect;
	enum {
		GLISS_LOADER_SYM_NO_TYPE,
		GLISS_LOADER_SYM_DATA,
		GLISS_LOADER_SYM_CODE
	} type;
	enum {
		GLISS_LOADER_NO_BINDING,
		GLISS_LOADER_LOCAL,
		GLISS_LOADER_GLOBAL,
		GLISS_LOADER_WEAK
	} bind;
} gliss_loader_sym_t;
int gliss_loader_count_syms(gliss_loader_t *loader);
void gliss_loader_sym(gliss_loader_t *loader, int sym, gliss_loader_sym_t *data);



#if defined(__cplusplus)
}
#endif

#endif	/* GLISS_OLD_ELF_H */
