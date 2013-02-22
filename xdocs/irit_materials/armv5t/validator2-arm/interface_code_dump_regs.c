/*** This file was generated automatically by generate_interface_code.py. ***/
#ifndef INTERFACE_CODE_DUMP_REGS_C
#define INTERFACE_CODE_DUMP_REGS_C

#include "interface_code.h"
#include <stdio.h>
	
void dump_regs()
{
	int i;

	for (i = 0; i < NUM_REG; i++)
	{
		char buf[100];
		
		/* let's display 32 or 64 bits */
		if (reg_infos[i].size == 64)
			printf(" %s\t%016llX|%016llX  ||  %016llX|%016llX\n", reg_infos[i].name, reg_infos[i].gliss_last, reg_infos[i].gliss, reg_infos[i].gdb_last, reg_infos[i].gdb);
		else
			printf(" %s\t%08llX|%08llX  ||  %08llX|%08llX\n", reg_infos[i].name, reg_infos[i].gliss_last, reg_infos[i].gliss, reg_infos[i].gdb_last, reg_infos[i].gdb);
	}
}

#endif /* INTERFACE_CODE_DUMP_REGS */

