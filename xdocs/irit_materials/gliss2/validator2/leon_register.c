#include <stdlib.h>
#include <string.h>

#include "leon_register.h"
#include "interface_code.h"

/* the python script, as given, cannot "render" a "moving" register
 * like the windowed ones of the leon */

uint32_t get_reg(PROC(_state_t) * st, unsigned int idx)
{
	/* return R[CWP * NWINDOWS + idx] unless idx indicates a global register
	 * NWINDOWS == 8 in the leon */
	if (idx < 8)
		return st->R[idx];
	return st->R[(((st->PSR & 0x1F) << 4) + idx - 8) % (8<<4) + 8];
}

void set_reg(PROC(_state_t) * st, unsigned int idx, uint32_t val)
{
	/* affects R[CWP * NWINDOWS + idx] unless idx indicates a global register
	 * NWINDOWS == 8 in the leon */
	if (idx == 0)
		return;
	if (idx < 8)
		st->R[idx] = val;
	st->R[(((st->PSR & 0x1F) << 4) + idx - 8) % (8<<4) + 8] = val;
}

#define REG(win, reg)	((st)->R[(((reg) - 8 + ((win) << 4))) % (8<<4)])

void dump_all_windows(PROC(_state_t) * st)
{
	int i;
	/* 1st globals */
	printf("GLOBALS\n");
	for (i = 0; i < 8; i = i + 4)
		printf("%4d-%4d: %08X %08X %08X %08X\n", i, i + 4, st->R[i], st->R[i+1], st->R[i+2], st->R[i+3]);

	/*for (cwp = 0; cwp < 8; cwp++)
	{
		printf("WINDOW %d\n        OUTS    LOCALS     INS\n", cwp);
		for (i = 0; i < 8; i++)
			printf("%6d: %08X %08X %08X\n", i, REG(cwp, 8+i), REG(cwp, 16+i), REG(cwp, 24+i));
	}*/
	printf("WINDOWED\n");
	for (i = 8; i < 128; i = i + 4)
	{
		printf("%4d-%4d: %08X %08X %08X %08X\n", i, i + 3, st->R[i], st->R[i + 1], st->R[i + 2], st->R[i + 3]);
	}
}

void dump_float_registers(PROC(_state_t) * st)
{
	union {
		double f;
		uint64_t u;
		uint32_t u32[2];
		float f32[2];
	} val;
	
	int i;
	printf("gliss float registers:\n");
	for (i=0; i<31; i=i+2) {
		val.f32[0] = st->F[i];
		val.f32[1] = st->F[i + 1];
//	printf("%f,%f => %f, %f\n", st->F[0], st->F[1], val.f32[0], val.f32[1]);
		printf("F[%d]=%g(%08X)  F[%d]=%g(%08X) D[%d]=%g(%016llX)\n", i, val.f32[0], val.u32[0], i+1, val.f32[1], val.u32[1], i, val.f, val.u);
	}
/*
	char buf[3000];
	send_gdb_cmd("-data-list-register-names\n", buf, 0);
	match_gdb_output(buf, "^done", IS_ERROR, "When trying to get register name list, ");
	printf("gdb registers:\n%s", buf);
	send_gdb_cmd("-data-list-register-values r\n", buf, 0);
	match_gdb_output(buf, "^done", IS_ERROR, "When trying to get register value list, ");
	send_gdb_cmd("-data-list-register-values N\n", buf, 0);
	match_gdb_output(buf, "^done", IS_ERROR, "When trying to get register value list, ");
	printf("gdb values:\n%s", buf);*/
}



/* !!TODO!! generate the following code automatically from register description */

/* identifiers for each register bank monitored */

#define REG_R	0
#define REG_F	1
#define REG_PSR	2
#define REG_FSR	3
#define REG_Y	4
#define	REG_TBR	5
#define	REG_WIM	6
#define	REG_PC	7
#define	REG_nPC	8


/* function called at initialization,
 * desc contains a register name as given in validator.cfg,
 * bank will indicate the GLISS2 bank to access,
 * idx will be the optional index as given in validator.cfg, or 0 if no index is present (single register) */
void get_gliss_reg_addr(char *desc, PROC(_state_t) * st, int *bank, int *idx)
{
	/*  let's hope we have only simple reg name or indexed by integer */
	
	/* search an index */
	char *idx_ptr = desc;
	while (*idx_ptr && (*idx_ptr != '['))
		idx_ptr++;
	if (*idx_ptr)
		*idx = strtoul(idx_ptr + 1, 0, 0);
	else
		*idx = 0;
	
	/* from here it should be auto generated */
	if (strncmp("R", desc, 1) == 0)
		*bank = REG_R;
	else if (strncmp("PSR", desc, 3) == 0)
		*bank = REG_PSR;
	else if (strncmp("FSR", desc, 3) == 0)
		*bank = REG_FSR;
	else if (strncmp("F", desc, 1) == 0)
		*bank = REG_F;
	else if (strncmp("Y", desc, 1) == 0)
		*bank = REG_Y;
	else if (strncmp("TBR", desc, 3) == 0)
		*bank = REG_TBR;
	else if (strncmp("WIM", desc, 3) == 0)
		*bank = REG_WIM;
	else if (strncmp("PC", desc, 2) == 0)
		*bank = REG_PC;
	else if (strncmp("nPC", desc, 3) == 0)
		*bank = REG_nPC;
}


/* after init, each register (even those in an array) will have a different idx
 * which will allow IDing the GLISS2 bank and optional real index */
uint64_t get_gliss_reg(PROC(_state_t) * st, int idx)
{
	switch (reg_infos[idx].gliss_reg) {
	case REG_R:
		return get_reg(st, reg_infos[idx].gliss_idx);
	case REG_F: {
		union {
			float f;
			uint32_t u;
		} v;
		v.f = st->F[reg_infos[idx].gliss_idx];
		return v.u;
	}
	case REG_PSR:
		return st->PSR;
	case REG_FSR:
		return st->FSR;
	case REG_Y:
		return st->Y;
	case REG_TBR:
		return st->TBR;
	case REG_WIM:
		return st->WIM;
	case REG_PC:
		return st->PC;
	case REG_nPC:
		return st->nPC;
	}
}
