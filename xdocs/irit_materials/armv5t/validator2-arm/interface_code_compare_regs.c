/*** This file was generated automatically by generate_interface_code.py. ***/
#ifndef INTERFACE_CODE_COMPARE_REGS_C
#define INTERFACE_CODE_COMPARE_REGS_C

#define ISS_DISASM
#include "interface_code.h"
#include "internal.h"
#include <stdio.h>


void compare_regs_this_instruction(char *drive_gdb_reply_buffer, PROC(_state_t) *real_state, PROC(_inst_t) *instr, int instr_count)
{
	int i;

	for (i = 0; i < NUM_REG; i++)
	{
		if (reg_infos[i].relative)
		{
			if (instr_count && ((reg_infos[i].gliss - reg_infos[i].gliss_last) != (reg_infos[i].gdb - reg_infos[i].gdb_last)))
			{
				/* warning: erroneous display if difference doesn't fit on 32 bits */
				if (reg_infos[i].size == 64)
					fprintf(stdout, "\n\nAfter 0x%08x (instruction #%i), register %s has not changed by the same amount: gdb %016llX\tgliss %016llX\n",
						gdb_pc, instr_count, reg_infos[i].name, reg_infos[i].gdb - reg_infos[i].gdb_last, reg_infos[i].gliss - reg_infos[i].gliss_last);
				else
					fprintf(stdout, "\n\nAfter 0x%08x (instruction #%i), register %s has not changed by the same amount: gdb %08llX\tgliss %08llX\n",
						gdb_pc, instr_count, reg_infos[i].name, reg_infos[i].gdb - reg_infos[i].gdb_last, reg_infos[i].gliss - reg_infos[i].gliss_last);
				dump_regs();
				fprintf(stdout, "Assembly follows\\n");
				send_gdb_cmd("-data-disassemble -s \"$pc\" -e \"$pc + 4\" -- 0\n", drive_gdb_reply_buffer, 0);
				disasm_error_report(drive_gdb_reply_buffer, real_state, instr, 1, 1);
			}
		}
		else
		if (reg_infos[i].gliss != reg_infos[i].gdb)
		{
			if ( instr_count ) 
				fprintf(stdout, "\n\nAfter 0x%08x (instruction #%i), register %s differs\n", gdb_pc, instr_count, reg_infos[i].name);
			else 
				fprintf(stdout, "\n\nAt initialization (0x%08x), register %s differs\n", gdb_pc, reg_infos[i].name);

			printf("GLISS\t\t\t\t\t\tGDB\nBEFORE               AFTER              BEFORE               AFTER\n\n");
			if ( ! display_full_dumps )
			{
				if (reg_infos[i].size == 64)
					printf(" %s\t%016llX|%016llX  ||  %016llX|%016llX\n", reg_infos[i].name, reg_infos[i].gliss_last, reg_infos[i].gliss, reg_infos[i].gdb_last, reg_infos[i].gdb);
				else
					printf(" %s\t%08llX|%08llX  ||  %08llX|%08llX\n", reg_infos[i].name, reg_infos[i].gliss_last, reg_infos[i].gliss, reg_infos[i].gdb_last, reg_infos[i].gdb);
				printf("Rerun with --full-dumps to get a complete dump of registers\n");
			}
			else
			{
				dump_regs();
			}
			fprintf(stdout, "Assembly follows\n");
			char buffer[100];
			sprintf(buffer, "-data-disassemble -s 0x%08X -e 0x%08X -- 0\n", gdb_pc, gdb_pc+4);
			send_gdb_cmd(buffer, drive_gdb_reply_buffer, 0);
			disasm_error_report(drive_gdb_reply_buffer, real_state, instr, 1, 1);
		}
		reg_infos[i].gdb_last = reg_infos[i].gdb;
		reg_infos[i].gliss_last = reg_infos[i].gliss;
	}
}
#endif /* INTERFACE_CODE_COMPARE_REGS */

