/*
 *	syscall-embedded module interface
 *
 *	This file is part of OTAWA
 *	Copyright (c) 2011, IRIT UPS.
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
#ifndef GLISS_SYSCALL_EMBEDDED_H
#define GLISS_SYSCALL_EMBEDDED_H

#include "api.h"
#include "mem.h"

#if defined(__cplusplus)
    extern  "C" {
#endif

#define GLISS_SYSCALL_STATE
#define GLISS_SYSCALL_INIT(pf)
#define GLISS_SYSCALL_DESTROY(pf)

void gliss_syscall(gliss_inst_t *inst, gliss_state_t *state);
void gliss_set_brk(gliss_platform_t *pf, gliss_address_t address);

#if defined(__cplusplus)
}
#endif

#endif /* GLISS_SYSCALL_LINUX_H */
