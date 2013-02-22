/*
 * GLISS V2 -- used registers header template
 * Copyright (c) 2010, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of GLISS V2.
 *
 * GLISS V2 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GLISS V2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GLISS V2; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
#ifndef $(PROC)_USED_REGS
#define $(PROC)_USED_REGS

#include "api.h"

/* register definition */
#define $(PROC)_REG_COUNT	$(used_regs_count)
$(foreach registers)
$(if !aliased)
$(if array)
#define $(PROC)_REG_$(NAME)(i)	((i) + $(used_reg_index))
$(else)
#define $(PROC)_REG_$(NAME)		$(used_reg_index)
$(end)
$(end)
$(end)

/* storage definition */
#define $(PROC)_REG_READ_MAX		$(used_regs_read_max)
#define $(PROC)_REG_WRITE_MAX		$(used_regs_write_max)
typedef int $(proc)_used_regs_read_t[$(PROC)_REG_READ_MAX + 1];
typedef int $(proc)_used_regs_write_t[$(PROC)_REG_WRITE_MAX + 1];

/* function declaration */
void $(proc)_used_regs($(proc)_inst_t *inst, $(proc)_used_regs_read_t regs, $(proc)_used_regs_write_t wrs);

#endif /* $(PROC)_USED_REGS */
