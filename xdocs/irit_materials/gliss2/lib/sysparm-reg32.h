/*
 *	$Id: sysparm-reg32.h,v 1.1 2009/01/21 07:30:55 casse Exp $
 *	sysparm-reg32 module interface
 *
 *	This file is part of OTAWA
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
#ifndef GLISS_SYSPARM_REG32_H
#define GLISS_SYSPARM_REG32_H

#include <stdint.h>
#include <gliss/config.h>

#if defined(__cplusplus)
    extern  "C" {
#endif

#define GLISS_SYSPARM_STATE
#define GLISS_SYSPARM_INIT(s)
#define GLISS_SYSPARM_DESTROY(s)

/* gliss_sysparm_t structure */
typedef int gliss_sysparm_t;
#define gliss_sysparm_init(parm, state) { parm = 0; }
#define gliss_sysparm_destroy(parm, state)
#define gliss_sysparm_pop8(parm, state) \
	GLISS_SYSPARM_REG32_REG(state, parm++) 
#define gliss_sysparm_pop16(parm, state) \
	GLISS_SYSPARM_REG32_REG(state, parm++) 
#define gliss_sysparm_pop32(parm, state) \
	GLISS_SYSPARM_REG32_REG(state, parm++)
#define gliss_sysparm_return(state, result) \
	GLISS_SYSPARM_REG32_RETURN(state, result)
#define gliss_sysparm_succeed(state) \
	GLISS_SYSPARM_REG32_SUCCEED(state)
#define gliss_sysparm_failed(state) \
	GLISS_SYSPARM_REG32_FAILED(state)

#if defined(__cplusplus)
}
#endif

#endif	/* GLISS_SYSPARM_REG32_H */
