#!/usr/bin/python
def write_c_create_var_chunk(defline):
	cfile1.write('''
	/* Create register ''' + defline[0] + '''*/
	send_gdb_cmd("-var-create ''' + defline[0] + ''' * $''' + defline[0] + '''\\n", drive_gdb_reply_buffer, display_replies);
        match_gdb_output(drive_gdb_reply_buffer, "^done,name=", IS_FATAL, "When creating variable for register ''' + defline[0] + ''' ," );
	''')

def write_c_init_gdb_regs_chunk(defline):
	cfile5.write('''
	sprintf(gcmd, "-var-assign ''' + defline[0] + ''' ''')
        if defline[2] == 'lli':
                cfile5.write('''%lld''')
        elif defline[2] == 'lf':
                cfile5.write('''%f''')
        elif defline[2] == 'li':
                cfile5.write('''%ld''')
        elif defline[2] == 'f':
                cfile5.write('''%f''')
        elif defline[2] == 'llu':
                cfile5.write('''%llu''')
        elif defline[2] == 'lu':
                cfile5.write('''%lu''')
	cfile5.write(''' \\n", real_state->''' + defline[1] + ''');
	send_gdb_cmd(gcmd, drive_gdb_reply_buffer, display_replies);
	''')
	

def write_c_dump_regs_chunk(defline):
	cfile4.write('''
	printf(" ''' + defline[0] + '''\\t''')
        if defline[2] == 'lli':
                cfile4.write('''%lld\\t\\t%lld''')
        elif defline[2] == 'lf':
                cfile4.write('''%f\\t\\t%f''')
        elif defline[2] == 'li':
                cfile4.write('''%ld\\t\\t%ld''')
        elif defline[2] == 'f':
                cfile4.write('''%f\\t\\t%f''')
        elif defline[2] == 'llu':
                cfile4.write('''%llu\\t\\t%llu''')
        elif defline[2] == 'lu':
                cfile4.write('''%lu\\t\\t%lu''')

	cfile4.write('''\\t\\t\\t''')
	if defline[2] == 'lli':
                cfile4.write('''%lld\\t\\t%lld''')
        elif defline[2] == 'lf':
                cfile4.write('''%f\\t\\t%f''')
        elif defline[2] == 'li':
                cfile4.write('''%ld\\t\\t%ld''')
        elif defline[2] == 'f':
                cfile4.write('''%f\\t\\t%f''')
        elif defline[2] == 'llu':
                cfile4.write('''%llu\\t\\t%llu''')
        elif defline[2] == 'lu':
                cfile4.write('''%lu\\t\\t%lu''')

        cfile4.write('''\\n",  gliss_last_''' + defline[0] + ''', gliss_'''+ defline[0] + ''', gdb_last_''' + defline[0] + ''', gdb_'''+ defline[0] +''');''')

def write_c_read_var_chunk(defline):
	cfile2.write('''
	/*Read register ''' + defline[0] + '''*/
        send_gdb_cmd("-var-evaluate-expression ''' + defline[0]  + '''\\n", drive_gdb_reply_buffer, display_replies);
        read_gdb_output_register_value_''' + defline[2] + '''(drive_gdb_reply_buffer, &gdb_''' + defline[0] + ''');

	gliss_''' + defline[0] + ''' = (''')

	if defline[2] == 'lli':
                cfile2.write('''long long int''')
        elif defline[2] == 'lf':
                cfile2.write('''double''')
        elif defline[2] == 'li':
                cfile2.write('''long int''')
        elif defline[2] == 'f':
                cfile2.write('''float''')
        elif defline[2] == 'llu':
                cfile2.write('''unsigned long long int''')
        elif defline[2] == 'lu':
                cfile2.write('''unsigned long int''')

	cfile2.write(''') real_state->''' + defline[1] + ''';


        if(display_values) printf(" ''' + defline[0] + ''' = ''')
        if defline[2] == 'lli':
                cfile2.write('''gdb %lld\\tgliss %lld''')
        elif defline[2] == 'lf':
                cfile2.write('''gdb %f\\tgliss %f''')
        elif defline[2] == 'li':
                cfile2.write('''gdb %ld\\tgliss %ld''')
        elif defline[2] == 'f':
                cfile2.write('''gdb %f\\tgliss %f''')
        elif defline[2] == 'llu':
                cfile2.write('''gdb %llu\\tgliss %llu''')
        elif defline[2] == 'lu':
                cfile2.write('''gdb %lu\\tgliss %lu''')

	cfile2.write('''\\n", gdb_''' + defline[0] + ''', gliss_''' + defline[0] + ''');	

''')

def write_c_compare_regs_chunk(defline):
	cfile3.write('''
	/*Compare values for register '''+ defline[0]  +''' */
	''')
	cfile3.write(''' 
		if((gliss_'''+defline[0] + ''' != gdb_''' + defline[0] + ''')''')
	cfile3.write( ''' && ((gliss_last_'''+defline[0]+ ''' != gliss_'''+defline[0]+ '''&&  gdb_last_''' + defline[0] + ''' !=gdb_''' + defline[0] + ''') || !instr_count )''')	

	cfile3.write(''')
		{
		if ( instr_count ) 
			fprintf(stdout, "\\n\\nAfter 0x%08x (instruction #%i), register '''+ defline[0] + ''' differs\\n", gdb_pc, instr_count);
		else 
			fprintf(stdout, "\\n\\nAt initialization (0x%08x), register '''+ defline[0] + ''' differs\\n", gdb_pc);

		printf("GLISS\\t\\t\\t\\t\\t\\tGDB\\n\\tBEFORE\\t\\tAFTER\\t\\t\\tBEFORE\\t\\tAFTER\\n\\n");
		if ( ! display_full_dumps )
			{
			printf(" ''' + defline[0] + '''\\t''')
        if defline[2] == 'lli':
                cfile3.write('''%lld\\t\\t%lld''')
        elif defline[2] == 'lf':
                cfile3.write('''%f (0x%llx)\\t\\t%f (0x%llx)''')
        elif defline[2] == 'li':
                cfile3.write('''%ld\\t\\t%ld''')
        elif defline[2] == 'f':
                cfile3.write('''%f (0x%llx)\\t\\t%f (0x%llx)''')
        elif defline[2] == 'llu':
                cfile3.write('''%llu\\t\\t%llu''')
        elif defline[2] == 'lu':
                cfile3.write('''%lu\\t\\t%lu''')

	cfile3.write('''\\t\\t\\t''')
	if defline[2] == 'lli':
                cfile3.write('''%lld\\t\\t%lld''')
        elif defline[2] == 'lf':
                cfile3.write('''%f (0x%llx)\\t\\t%f (0x%llx)''')
        elif defline[2] == 'li':
                cfile3.write('''%ld\\t\\t%ld''')
        elif defline[2] == 'f':
                cfile3.write('''%f\\t\\t%f''')
        elif defline[2] == 'llu':
                cfile3.write('''%llu\\t\\t%llu''')
        elif defline[2] == 'lu':
                cfile3.write('''%lu\\t\\t%lu''')

	if defline[2] != 'lf':
		cfile3.write('''\\n",  gliss_last_''' + defline[0] + ''', gliss_'''+ defline[0] + ''', gdb_last_''' + defline[0] + ''', gdb_'''+ defline[0] +''');''')
	else:
		cfile3.write('''\\n", gliss_last_''' + defline[0] + ''', *(unsigned long long int *)(& gliss_last_''' + defline[0] + '''), gliss_''' + defline[0] + ''', *(unsigned long long int *)(&gliss_''' + defline[0] + '''), gdb_last_''' + defline[0] + ''', *(unsigned long long int *)(&gdb_last_''' + defline[0]  + '''), gdb_''' + defline[0] + ''', *(unsigned long long int *)(&gdb_'''+  defline[0] + '''));''')
	cfile3.write('''
//	printf("Rerun with --full-dumps to get a complete dump of registers\\n");
			}
			else
			{
			dump_regs();
			}
		fprintf(stdout, "Assembly follows\\n");
		send_gdb_cmd("-data-disassemble -s \\"$pc - 4\\" -e \\"$pc\\" -- 0\\n", drive_gdb_reply_buffer, 0);
		disasm_error_report(drive_gdb_reply_buffer, real_state, instr);
		}
			
	gdb_last_''' + defline[0] + ''' = gdb_''' + defline[0] + ''';
	gliss_last_'''+defline[0]+ ''' = gliss_'''+defline[0] + ''';
	''');
	
def write_c_relative_compare_regs_chunk(defline):
	cfile3.write('''
	/*Compare relative values (changes)s for register '''+ defline[0]  +''' */
	''')
	cfile3.write(''' 
		if((gliss_'''+defline[0] + '''- gliss_last_''' + defline[0] + ''' != gdb_''' + defline[0] + ''' - gdb_last_''' + defline[0] + ''' && instr_count)''')

	cfile3.write(''')
		{
		fprintf(stdout, "\\n\\nAfter 0x%08x (instruction #%i), register '''+ defline[0] + ''' has not changed by the same amount: ''')
	if defline[2] == 'lli':
                cfile3.write('''gdb %+lld\\tgliss %+lld''')
        elif defline[2] == 'lf':
                cfile3.write('''gdb %+f\\tgliss %+f''')
        elif defline[2] == 'li':
                cfile3.write('''gdb %+ld\\tgliss %+ld''')
        elif defline[2] == 'f':
                cfile3.write('''gdb %+f\\tgliss %+f''')
        elif defline[2] == 'llu':
                cfile3.write('''gdb %+llu\\tgliss %+llu''')
        elif defline[2] == 'lu':
                cfile3.write('''gdb %+lu\\tgliss %+llu''')


	cfile3.write('''\\n", gdb_pc, instr_count, gdb_''' + defline[0] +  '''- gdb_last_''' + defline[0] + ''', gliss_''' + defline[0]+ ''' - gliss_last_'''+defline[0]+''');
		dump_regs();
		fprintf(stdout, "Assembly follows\\n");
		send_gdb_cmd("-data-disassemble -s \\"$pc - 4\\" -e \\"$pc\\" -- 0\\n", drive_gdb_reply_buffer, 0);
		disasm_error_report(drive_gdb_reply_buffer, real_state, instr);
		}
	gdb_last_''' + defline[0] + ''' = gdb_''' + defline[0] + ''';
	gliss_last_'''+defline[0]+ ''' = gliss_'''+defline[0] + ''';
	''');


def write_h_chunk(defline):
	hfile.write('''
	/* Definition of register ''' + defline[0] + '''*/
	''')
	if defline[2] == 'lli':
		hfile.write('''long long int''')
	elif defline[2] == 'lf':
		hfile.write('''double''')
	elif defline[2] == 'li':
		hfile.write('''long int''')
	elif defline[2] == 'f':
		hfile.write('''float''')
	elif defline[2] == 'llu':
		hfile.write('''unsigned long long int''')
	elif defline[2] == 'lu':
		hfile.write('''unsigned long int''')
	

	hfile.write(''' gliss_''' + defline[0] + ''',''')
	hfile.write(''' gdb_''' + defline[0] + ''', gliss_last_''' + defline[0] + ''', gdb_last_''' + defline[0] + ''';
	''')
		
		
	



import os
from optparse import OptionParser
parser = OptionParser()
parser.add_option("-i", "--input-file", dest="ifilename",
                  help="filename to read the configuration from", default="validator.cfg")
parser.add_option("-p", "--proc-name", dest="proc_name",
			help="name of the processor in the nmp description", default="ppc")

(options, args) = parser.parse_args()

proc_name = options.proc_name

infile = open(options.ifilename, 'r')
cfile1 = open('interface_code_create_vars.c', 'w')
cfile2 = open('interface_code_read_vars.c', 'w')
cfile3 = open('interface_code_compare_regs.c', 'w')
cfile4 = open('interface_code_dump_regs.c', 'w')
cfile5 = open('interface_code_init_gdb_regs.c', 'w')
hfile = open('interface_code.h', 'w')

cfile1.write('''/*** This file was generated automatically by generate_interface_code.py. ***/
#define INTERFACE_CODE_CREATE_VARS_C
#include "all_inc.h"
#include "interface_code.h"

void create_gdb_vars(char * drive_gdb_reply_buffer)
	{
''')


cfile2.write('''/*** This file was generated automatically by generate_interface_code.py. ***/
#define INTERFACE_CODE_READ_VARS_C
#include "all_inc.h" 
#include "interface_code.h"
	
void read_vars_this_instruction(char * drive_gdb_reply_buffer)
	{


''')

cfile3.write('''/*** This file was generated automatically by generate_interface_code.py. ***/
#define INTERFACE_CODE_COMPARE_REGS_C
#define ISS_DISASM
#include "all_inc.h" 
#include "interface_code.h"
	
void compare_regs_this_instruction(char * drive_gdb_reply_buffer, '''+proc_name+'''_state_t * real_state, '''+proc_name+'''_inst_t * instr, int instr_count)
	{

''')

cfile4.write('''/*** This file was generated automatically by generate_interface_code.py. ***/
#define INTERFACE_CODE_DUMP_REGS_C
#include "all_inc.h" 
#include "interface_code.h"
	
void dump_regs()
	{
''')

cfile5.write('''/*** This file was generated automatically by generate_interface_code.py. ***/
#define INTERFACE_CODE_INIT_GDB_REGS_C
#include "all_inc.h" 
#include "interface_code.h"
	
void init_gdb_regs(char * drive_gdb_reply_buffer)
	{
	char gcmd[4096];
''')

hfile.write('''/*** This file was generated automatically by generate_interface_code.py. ***/

	void create_gdb_vars(char *);
	void read_vars_this_instruction(char *);
	void compare_regs_this_instruction(char *, '''+proc_name+'''_state_t *, '''+proc_name+'''_inst_t *, int);	
	void dump_regs();
	void init_gdb_regs(char *);
''')

for a in infile:
	a.lstrip() #on vire les espaces au debut
	comment_index = a.find('#')
	if comment_index != -1:
		a = a[0:comment_index]
	if len(a) > 0:
		defline = a.split()
		if len(defline) != 3 and len(defline) != 4:
			print('''ERROR: Line ''' + a + ''' has incorrect format, skipping\n''')
			continue
		if len(defline) == 3:
			write_c_compare_regs_chunk(defline)
			write_c_dump_regs_chunk(defline)
		if len(defline) == 4:
			if defline[3] == 'relative':
				write_c_relative_compare_regs_chunk(defline)
				
		write_c_create_var_chunk(defline)
		write_c_read_var_chunk(defline)
		write_c_init_gdb_regs_chunk(defline)
		write_h_chunk(defline)



cfile1.write('''
	}

#undef INTERFACE_CODE_CREATE_VARS

''')
cfile2.write('''
	if(display_values) printf("\\n");
	}

#undef INTERFACE_CODE_READ_VARS

''')
cfile3.write('''
	}
#undef INTERFACE_CODE_COMPARE_REGS

''')
cfile4.write('''
	}
#undef INTERFACE_CODE_DUMP_REGS

''')
cfile5.write('''
	}
#undef INTERFACE_CODE_INIT_GDB_REGS

''')


hfile.write('\n')
cfile1.close
cfile2.close
cfile3.close
cfile4.close
hfile.close
infile.close
