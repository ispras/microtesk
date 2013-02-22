/*** This file was generated automatically by generate_interface_code.py. ***/
#ifndef INTERFACE_CODE_READ_VARS_C
#define INTERFACE_CODE_READ_VARS_C

#include "interface_code.h"
#include "internal.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include GLISS_REG_H


/* replace all ',' and '"' by a space */
static void cut_string(char *list)
{
	while (*list)
	{
		if ((*list == ',') || (*list == '"'))
			*list = ' ';
		list++;
	}
}

/* returns the number of occurrence of c in s */
static int count_in_string(char *s, char c)
{
	int cpt = 0;
	while (*s)
		if (*s++ == c)
			cpt++;
	return cpt;
}

/* will contain the number of regs gdb deals with */
static int nb_regs = 0;

/* return the index in size_data for the next reg name in the "cut" string */
/* *cnt is inremented each time we find a valid reg or we skip a not tracked reg */
static int get_next_reg_name_and_size(char **name_list, int *cnt)
{
	while (1)
	{
		while (**name_list == ' ')
			(*name_list)++;
		
		if (**name_list == ']')
			return -1;
	
		int cpt = 0;
		while (*(*name_list + cpt)  != ' ')
			cpt++;
	
		int i;
		for (i = 0; i<NUM_REG; i++)
		{
			if (size_data[i].size)
			{
				if (strncmp(*name_list, size_data[i].name, cpt) == 0)
				{
					*name_list += cpt;
					(*cnt)++;
					return i;
				}
			}
		}
		/* reg not found, skip it */
		*name_list += cpt;
		(*cnt)++;
	}
}

static int get_next_reg_value(char **val_list, uint64_t *v)
{
	/* values are smtg like {number="<gdb_idx>",value="<hexa_value>"} */
	char *ptr;

	ptr = strstr(*val_list, "value=\"");
	/* end of list? */
	if (!ptr)
		return -1;
	/* read the value */
	*val_list = ptr + 7;
	*v = strtoull(*val_list, 0, 0);
	return 0;
}


void init_gdb_regs(char * drive_gdb_reply_buffer)
{
	send_gdb_cmd("-data-list-register-names\n", drive_gdb_reply_buffer, display_replies);
	match_gdb_output(drive_gdb_reply_buffer, "^done", IS_ERROR, "When trying to get register name list, ");
	/* find beginning of register name list and "clean" the list */
	while (*drive_gdb_reply_buffer++ != '[');
	/* each reg name is surrounded by 2 " */
	nb_regs = count_in_string(drive_gdb_reply_buffer, '\"') / 2;
	cut_string(drive_gdb_reply_buffer);
	
	/* fill in reg_infos */
	int idx, i, gdb_idx = -1;
	for (i=0; i<NUM_REG; i++)
	{
//		printf("before:#%s#\n", drive_gdb_reply_buffer);
		idx = get_next_reg_name_and_size(&drive_gdb_reply_buffer, &gdb_idx);
//		printf("after :#%s#\n", drive_gdb_reply_buffer);
		if (idx < 0)
		{
			reg_infos[i].size = 0;
			i--;
			continue;
		}
		/* reg_infos is sorted the same way as the output of "-data-list-register-names"
		 * there may be some regs missing because not in validator.cfg */
		reg_infos[i].name = size_data[idx].name;
		reg_infos[i].size = size_data[idx].size;
		reg_infos[i].gdb_idx = gdb_idx;
		reg_infos[i].relative = size_data[idx].relative;
		get_gliss_reg_addr(size_data[idx].gliss_reg, real_state, &reg_infos[i].gliss_reg, &reg_infos[i].gliss_idx);
	}
}
	
void read_vars_this_instruction(char * drive_gdb_reply_buffer)
{
	/* will temporary hold register values which are thus limited to 64 bits */
	uint64_t *reg_val;

	/* first read gdb regs in raw format (all in hexa with full length) */
	send_gdb_cmd("-data-list-register-values r\n", drive_gdb_reply_buffer, display_replies);
	match_gdb_output(drive_gdb_reply_buffer, "^done", IS_ERROR, "When trying to get register value list, ");

	/* find beginning of register value list */
	while (*drive_gdb_reply_buffer++ != '[');
	
	/* store value in tmp array */
	reg_val = malloc(nb_regs * sizeof(uint64_t));
	int i = 0;
	while (i < nb_regs && (get_next_reg_value(&drive_gdb_reply_buffer, &reg_val[i++]) != -1));
	
	/* read gdb and gliss value for each reg */
	for (i=0; i<NUM_REG; i++)
	{
		reg_infos[i].gdb = reg_val[reg_infos[i].gdb_idx];
		reg_infos[i].gliss = get_gliss_reg(real_state, i);
	}
	
	free(reg_val);

	if(display_values)
	{
		for (i = 0; i < NUM_REG; i++)
			if (reg_infos[i].size == 64)
				printf(" %s = gdb %016llX  gliss %016llX\n", reg_infos[i].name, reg_infos[i].gdb, reg_infos[i].gliss);
			else
				printf(" %s = gdb %08llX  gliss %08llX\n", reg_infos[i].name, reg_infos[i].gdb, reg_infos[i].gliss);
		printf("\n");
	}
}

#endif /* INTERFACE_CODE_READ_VARS */

