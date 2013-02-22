/*
 *	Embedded Environment
 *
 *	This file is part of OTAWA
 *	Copyright (c) 2011, IRIT UPS.
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
#ifndef GLISS_ENV_EMBEDDED_H
#define GLISS_ENV_EMBEDDED_H

#include "api.h"
#include "loader.h"
#include "config.h"

__BEGIN_DECLS

/* module name: env */
#define GLISS_ENV_STATE
#define GLISS_ENV_INIT(s)
#define GLISS_ENV_DESTROY(s)


/* system initialization (used internally during platform and state initialization) */
void gliss_stack_fill_env(gliss_loader_t *loader,  gliss_platform_t *platform, gliss_env_t *env);
void gliss_registers_fill_env(gliss_env_t *env, gliss_state_t *state);

__END_DECLS

#endif /* GLISS_ENV_EMBEDDED_H */
