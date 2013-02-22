/*
 *	$Id: fpi.h,v 1.1 2009/04/09 08:17:28 casse Exp $
 *	syscall-linux module interface
 *
 *	This file is part of GLISS2
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
 *	along with GLISS2; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
#ifndef GLISS_PPC_FPI_H
#define GLISS_PPC_FPI_H

#include <fenv.h>
#include "../include/gliss/api.h"

#if defined(__cplusplus)
extern  "C" {
#endif

#define GLISS_FPI_STATE
#define GLISS_FPI_INIT(s)
#define GLISS_FPI_DESTROY(s)

#define FPI_TONEAREST   FE_TONEAREST
#define FPI_TOWARDZERO  FE_TOWARDZERO
#define FPI_UPWARD      FE_UPWARD
#define FPI_DOWNWARD    FE_DOWNWARD
        /* Functions */
#define fpi_setround(mode)    fesetround(mode)
#define fpi_getround()        fegetround()

/* Values */
#define FPI_INEXACT     FE_INEXACT
#define FPI_DIVBYZERO   FE_DIVBYZERO
#define FPI_UNDERFLOW   FE_UNDERFLOW
#define FPI_OVERFLOW    FE_OVERFLOW
#define FPI_INVALID     FE_INVALID
#define FPI_ALLEXCEPT   FE_ALL_EXCEPT

/* Functions */
#define fpi_clearexcept(flag) feclearexcept(flag)  /* clear a set of exceptions */
#define fpi_raiseexcept(flag) feraiseexcept(flag)  /* 'set' a set of exceptions */
#define fpi_testexcept(flag)  fetestexcept(flag)   /* test a set of exceptions */

/* 'Not a Number' functions */
#define fpi_isnan64(x) isnan(x)
#define fpi_isnan32(x) isnan(x)
#define fpi_isnan80 #error "Function not implemented ! Sorry\n"

#if defined(__cplusplus)
}
#endif

#endif /* GLISS_PPC_FPI_H */
