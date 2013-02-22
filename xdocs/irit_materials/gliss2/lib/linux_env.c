/*
 *	linux environment module implementation
 *
 *	This file is part of GLISS V2
 *	Copyright (c) 2010, IRIT UPS.
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

#include <string.h>
#include <gliss/env.h>
#include <gliss/syscall.h>
#include "platform.h"



/* stack and system initialization for PowerPC linux-like systems,
   with respect to the System V ABI PowerPC Processor Supplement */


#define STACKADDR_DESCENDING_DEFAULT 0x80000000
/* no size advised, to my knowledge */
#define STACKSIZE_DEFAULT 0x1000000

/* default page size */
#ifndef GLISS_PAGE_SIZE
#	define GLISS_PAGE_SIZE	4096
#endif


/**
 * initialize the stack pointer with the default value,
 * checking code and data may not overlap with the forecast stack size.
 * @param	loader	Loader containing code and data size informations.
 * @return		the initial stack pointer value, 0 if there's an error (overlapping)
 */
static gliss_address_t gliss_stack_pointer_init(gliss_loader_t *loader)
{
	/*if (loader == 0)
		return 0;

	uint32_t data_max = loader->Data.size + loader->Data.address;
	uint32_t code_max = loader->Text.size + loader->Text.address;
	uint32_t addr_max = (data_max > code_max) ? data_max : code_max;
*/
	/* check if code/data and stack don't overlap */
/*	if (addr_max >= STACKADDR_DESCENDING_DEFAULT - STACKSIZE_DEFAULT)
		return 0;*/

	return STACKADDR_DESCENDING_DEFAULT;
}


#define MASK(n)		((1 << n) -1)
/* round up to the next 16 byte boundary */
#define ALIGN(v, a)	((v + MASK(a)) & ~MASK(a))

#define PUSH8(v, sp)		{ gliss_mem_write8(memory, sp, v); sp++; }
#define PUSH32(v, sp)		{ gliss_mem_write32(memory, sp, v); sp += 4; }
#define PUSH64(v, sp)		{ memcpy(&tmp, &v, sizeof(uint64_t)); gliss_mem_write64(memory, sp, tmp); sp += 8; }
#define AT_NULL	0


/**
 * system initialization of the stack, program arguments, environment and auxilliar
 * vectors are written in the stack. some key addresses are stored for later register initialization.
 * @param	loader	informations about code and data size.
 * @param	platform	platform whose memory contains the stack to initialize.
 * @param	env	contains the data to write in the stack, the addresses of written data are stored in it by the function.
 *
 */
void gliss_stack_fill_env(gliss_loader_t *loader, gliss_platform_t *platform, gliss_env_t *env)
{
	uint32_t size;
	uint32_t init_size;
	uint32_t aligned_size;
	uint32_t align_padding;
	uint64_t tmp;
	int num_arg, num_env, num_aux, i, j, len;
	gliss_address_t addr_str;
	gliss_address_t stack_ptr, align_stack_addr, argv_ptr, envp_ptr, auxv_ptr;
	gliss_auxv_t auxv_null = {AT_NULL, 0};

	if ((platform==0) || (env==0))
		gliss_panic("param error in gliss_stack_fill_env");

	gliss_memory_t *memory = gliss_get_memory(platform, GLISS_MAIN_MEMORY);

	/* find the brk, useful for later */
	env->brk_addr = gliss_brk_init(loader);

	/* initialize stack pointer */
	env->stack_pointer = gliss_stack_pointer_init(loader);
	if (env->stack_pointer == 0)
		gliss_panic("code/data and stack are overlapping");

	/* compute initial stack size (arg, env., ..) */
	init_size = 0;

	/* count arguments */
	num_arg = 0;
	if(env->argv)
		for(; env->argv[num_arg] != NULL; num_arg++);

	/* count environment variables */
	num_env = 0;
	if(env->envp)
		for(; env->envp[num_env] != NULL; num_env++);

	/* count auxiliary vectors */
	num_aux = -1; /* 0; !!TODO!! */
	/*if(env->auxv)
		for(; env->auxv[num_aux].a_type != AT_NULL; num_aux++);*/

	/* compute memory required by arguments */
	size = 0;
	for (i = 0; i < num_arg; i++)
		size += strlen(env->argv[i]) + 1;

	/* compute memory required by environnement */
	for (i = 0; i < num_env; i++)
		size += strlen(env->envp[i]) + 1;

	/* we assume argc and each pointer is 32 bit long */
	/* and auxv_t is 64 bit long */
#define ADDR_SIZE	4
#define AUXV_SIZE	8

	/* compute used stack size */
	init_size = (num_arg + num_env + 2 ) * ADDR_SIZE + (num_aux + 1) * AUXV_SIZE + size + ADDR_SIZE;
	/* 16 byte alignment for PowerPC stack pointer after initialization */
	aligned_size = (init_size + 3) & ~3;	/* !!TODO!! alignment on 4 ou 8 */
	/* 0-bytes to write before data */
	align_padding = aligned_size - init_size;
	/* we will pad the top addresses of the written data with 0 to have an aligned sp */
	// TODO check if +1 needed
	align_stack_addr = env->stack_pointer - aligned_size;


	/*
	stack scheme		addresses
	=========================================

	strings			+++
	_________
	AT-NULL
	---------
	auxv_t[n]
	...
	auxv_t[0]
	_________
	0			^
	---------		|
	env[i]
	...
	env[0]
	_________
	0
	---------
	argv[argc-1]
	...
	argv[0]			---
	_________
	argc			<- sp after init
	_________

	*/


	stack_ptr = align_stack_addr;

	/* write argc */
	PUSH32(env->argc, stack_ptr);

	/* write argv[] pointers later */
	env->argv_addr = argv_ptr = stack_ptr;

	/* write envp[] pointers later*/
	env->envp_addr = envp_ptr = argv_ptr + (num_arg + 1) * ADDR_SIZE;

	/* write the auxiliary vectors */
	auxv_ptr = env->auxv_addr = envp_ptr + (num_env + 1) * ADDR_SIZE;
	// !DEBUG!!
	/*num_aux = 0;
	for (i = 0; i < num_aux; i++)
		PUSH64(env->auxv[i], auxv_ptr);
	* AT_NULL termination entry *
	PUSH64(auxv_null, auxv_ptr);*/

	/* write argv strings and put addresses in argv[i] */
	addr_str = auxv_ptr + (num_aux + 1) * AUXV_SIZE;
	for (i = 0; i < num_arg; i++)
	{
		len = strlen(env->argv[i]) + 1;
		/* store address of argv[i] in stack */
		PUSH32(addr_str, argv_ptr);
		for (j = 0 ; j < len ; j++)
			PUSH8(env->argv[i][j], addr_str);
	}
	/* NULL word termination */
	PUSH32(0, argv_ptr);

	/* write envp strings and put addresses in envp[i]  */
	for (i = 0; i < num_env; i++)
	{
		len = strlen(env->envp[i]) + 1;
		/* store address of envp[i] in stack */
		PUSH32(addr_str, envp_ptr);
		for (j = 0 ; j < len ; j++)
			PUSH8(env->envp[i][j], addr_str);
	}
	/* NULL word termination */
	PUSH32(0, envp_ptr);

	/* set the starting sp to the beginning of the written data (pointing to argc) */
	env->stack_pointer = align_stack_addr;
}



/**
 * Initialize a state's registers with systems value collected during stack initialization
 * @param	env	structure containing the values to put in specific registers
 * @param	state	the state whose registers will be initialized
 */
void gliss_registers_fill_env(gliss_env_t *env, gliss_state_t *state)
{
	if ((state == 0) || (env == 0))
		gliss_panic("param error in gliss_registers_fill_env");

	/* specific to PPC !!!!WARNING!!!! */

	/* r1 will hold the stack pointer */
	GLISS_SYSPARM_REG32_SP(state) = env->stack_pointer;

	/* argc goes in r3 */
	GLISS_GET_GPR(state, 3) = env->argc;

	/* r4 recieves the address of argv's pointer array */
	GLISS_GET_GPR(state, 4) = env->argv_addr;

	/* we do the same with r5 and envp */
	GLISS_GET_GPR(state, 5) = env->envp_addr;

	/* idem with r6 and auxv */
	/* GLISS_GET_GPR(state, 6) = env->auxv_addr; !!TODO!! */

	/* r7 contains a termination function pointer, 0 in our case */
	GLISS_GET_GPR(state, 7) = 0;
}

/**
 * Fix the position of the brk base (top address of memory).
 * @param pf		Platform to work with.
 * @param address	New address of the brk base.
 */
void gliss_set_brk(gliss_platform_t *pf, gliss_address_t address) {
	pf->brk_base = (address + GLISS_PAGE_SIZE - 1) & ~(GLISS_PAGE_SIZE - 1);
}
