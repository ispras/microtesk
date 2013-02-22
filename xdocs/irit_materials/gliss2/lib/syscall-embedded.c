/*
 *	syscall-linux module implementation
 *
 *	This file is part of GLISS V2
 *	Copyright (c) 2009, IRIT UPS.
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

#include <assert.h>
#include <gliss/api.h>

/**
 * Called to perform a system call.
 * In this platform (embedded), cause an assertion failure.
 * @param inst		Current instruction.
 * @param state		Current state.
 */
void gliss_syscall(gliss_inst_t *inst, gliss_state_t *state) {
	assert(0);
}


/**
 * Called to initialize brk base.
 * Do nothing here.
 * @param platform		Current platform.
 * @param address		Brk base address.
 */
/*void gliss_set_brk(gliss_platform_t *pf, gliss_address_t address) {
}*/
