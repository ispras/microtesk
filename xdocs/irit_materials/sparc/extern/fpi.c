/*
 *	$Id: fpi.c,v 1.1 2009/04/09 08:17:28 casse Exp $
 *	floating point arithmetic module interface
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

#include <sparc/fpi.h>
#include <math.h>

typedef union {
	uint32_t u;
	float f;
} f32_t;

typedef union {
	uint64_t u;
	double f;
} f64_t;

/* arithmetic functions (32 and 64 bits for the moment) */
float fpi_add32(float x, float y) { /*
f32_t xx, yy, zz;
xx.f = x;
yy.f = y;
zz.f = x+y;
printf("fadd32, %g + %g = %g (%08X, %08X ; %08X)\n", x, y, x+y, xx.u, yy.u, zz.u);*/return x + y; }
float fpi_sub32(float x, float y) { /*
f32_t xx, yy, zz;
xx.f = x;
yy.f = y;
zz.f = x-y;
printf("fsub32, %g - %g = %g (%08X, %08X ; %08X)\n", x, y, x-y, xx.u, yy.u, zz.u);*/return x - y; }
float fpi_mul32(float x, float y) {  /*
f32_t xx, yy, zz;
xx.f = x;
yy.f = y;
zz.f = x*y;
printf("fmul32, %g * %g = %g (%08X, %08X ; %08X)\n", x, y, x*y, xx.u, yy.u, zz.u);*/return x * y; }
float fpi_div32(float x, float y) {   /*
f32_t xx, yy, zz;
xx.f = x;
yy.f = y;
zz.f = x/y;
printf("fdiv32, %g / %g = %g (%08X, %08X ; %08X)\n", x, y, x/y, xx.u, yy.u, zz.u);*/return x / y; }
float fpi_sqrt32(float x) {  /*
f32_t xx, yy;
xx.f = x;
yy.f = sqrtf(x);
printf("fsqrt32(%g) = %g (%08X ; %08X)\n", x, sqrtf(x), xx.u, yy.u);*/return sqrtf(x); }

double fpi_add64(double x, double y) { /*
f64_t xx, yy, zz;
xx.f = x;
yy.f = y;
zz.f = x+y;
printf("fadd64, %g + %g = %g (%08X, %08X ; %08X)\n", x, y, x+y, xx.u, yy.u, zz.u);*/return x + y; }
double fpi_sub64(double x, double y) { /*
f64_t xx, yy, zz;
xx.f = x;
yy.f = y;
zz.f = x-y;
printf("fsub64, %g - %g = %g (%08X, %08X ; %08X)\n", x, y, x-y, xx.u, yy.u, zz.u);*/return x - y; }
double fpi_mul64(double x, double y) { /*
f64_t xx, yy, zz;
xx.f = x;
yy.f = y;
zz.f = x*y;
printf("fmul64, %g * %g = %g (%08X, %08X ; %08X)\n", x, y, x*y, xx.u, yy.u, zz.u);*/return x * y; }
double fpi_div64(double x, double y) { /*
f64_t xx, yy, zz;
xx.f = x;
yy.f = y;
zz.f = x/y;
printf("fdiv64, %g / %g = %g (%016llX, %016llX ; %016llX)\n", x, y, x/y, xx.u, yy.u, zz.u);*/return x / y; }
double fpi_sqrt64(double x) {   /*
f64_t xx, yy;
xx.f = x;
yy.f = sqrt(x);
printf("fsqrt64(%g) = %g (%08X ; %08X)\n", x, sqrt(x), xx.u, yy.u);*/return sqrt(x); }
