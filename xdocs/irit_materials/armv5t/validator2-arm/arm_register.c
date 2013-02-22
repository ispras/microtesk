#include <stdlib.h>
#include <string.h>

#include "arm_register.h"
#include "interface_code.h"

/* rendering of bankable ARM registers */

static unsigned int real_idx(PROC(_state_t) * st, unsigned int idx)
{
	if (idx <= 7)
		return idx;
	else if (idx <= 12) {
		if ((st->Ucpsr & 0x1F) == 17) /* mode == FIQ */
			return idx + 8;
		else
			return idx;
	} else if (idx <= 14) {
		switch (st->Ucpsr & 0x1F) {
			case 19:	return idx + 10;
			case 23:	return idx + 12;
			case 17:	return idx + 8;
			case 18:	return idx + 16;
			case 27:	return idx + 14;
			default:	return idx;
		}
	} else
		return 15;

}

uint32_t get_arm_reg(PROC(_state_t) * st, unsigned int idx)
{
	return st->GPR[real_idx(st, idx)];
}

void set_arm_reg(PROC(_state_t) * st, unsigned int idx, uint32_t val)
{
	st->GPR[real_idx(st, idx)] = val;
}



#define REG_R		0
#define REG_UCPSR	1

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

	if (strncmp("R", desc, 3) == 0)
		*bank = REG_R;
	else if (strncmp("Ucpsr", desc, 3) == 0)
		*bank = REG_UCPSR;
}


uint64_t get_gliss_reg(PROC(_state_t) * st, int idx)
{
	switch (reg_infos[idx].gliss_reg) {
	case REG_R:
		return get_arm_reg(st, reg_infos[idx].gliss_idx);
	case REG_UCPSR:
		return st->Ucpsr;
	}
}
