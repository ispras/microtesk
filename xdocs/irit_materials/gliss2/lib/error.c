/*
 *	Error module.
 *
 *	This file is part of OTAWA
 *	Copyright (c) 2010, IRIT UPS.
 *
 *	GLISS2 is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	GLISS2 is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with GLISS2; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <gliss/error.h>

/**
 * Display the message and abort the simulator.
 * @param format	Format of the message.
 * @param ...		Parameters of the message.
 * @note This function never returns.
 */
void gliss_panic(const char *format, ...) {
	va_list args;
	fprintf(stderr, "PANIC: ");
	va_start(args, format);
	vfprintf(stderr, format, args);
	va_end(args);
	fputc('\n', stderr);
	abort();
}


/**
 * display the given error message.
 * @param state		Current state.
 * @param inst		Current instruction.
 * @param message	Message to display.
 */
void gliss_error(gliss_state_t *state, gliss_inst_t *inst, const char *format, ...) {
	va_list args;
	fprintf(stderr, "ERROR: ");
	va_start(args, format);
	vfprintf(stderr, format, args);
	va_end(args);
	fputc('\n', stderr);
}


/**
 * Process error from an unknown instruction.
 * @param state		Current state.
 * @param address	Faulty instruction address.
 */
void gliss_execute_unknown(gliss_state_t *state, gliss_address_t address) {
	gliss_dump_state(state, stderr);
	gliss_error(state, NULL, "unknown instruction at %08x", address);
	abort();
}

