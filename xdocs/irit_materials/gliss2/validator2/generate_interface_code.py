#!/usr/bin/python

# now we process lines written this way:
# gdb_name	gliss_name	size (in bits)	[relative]

C_size_allowed = [8, 16, 32, 64]

def C_size(typ):
	x = int(typ)
	if x in C_size_allowed:
		return typ
	else:
		print('Error: size allowed for registers are 8*<a_power_of_two> bits upto 64 bits (like the basic C int types).\n')
		print('The problematic size is: {0}'.format(typ))
		exit()



def write_c_chunk(defline):
	cfile.write('''	/* Definition of register {0} */
	{{"{0}", {1}, "{2}"'''.format(defline[0], C_size(defline[2]), defline[1]))
	if len(defline) == 4:
		if defline[3] == 'relative':
			cfile.write(''', 1},
''')
	else:
		cfile.write(''', 0},
''')
		
	


import string
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
hfile = open('interface_code.h', 'w')
cfile = open('interface_code.c', 'w')



hfile.write('''/*** This file was generated automatically by generate_interface_code.py. ***/
#ifndef INTERFACE_CODE_H
#define INTERFACE_CODE_H

#include GLISS_API_H
#include "internal.h"

typedef union
{{
	float f;
	uint32_t u;
}} f32_t;

typedef union
{{
	double f;
	uint64_t u;
}} f64_t;

typedef struct
{{
	char *name;
	int gdb_idx;
	int size;
	/* can deal with register up to 64 bits */
	uint64_t gliss, gdb, gliss_last, gdb_last;
	/* description of corresponding gliss register and its index in bank */
	int gliss_reg;
	int gliss_idx;
	int relative;
}} reg_t;

typedef struct
{{
	char *name;
	int size;
	char *gliss_reg;
	int relative;
}} reg_data_t;


void read_vars_this_instruction(char *);
void compare_regs_this_instruction(char *, {0}_state_t *, {0}_inst_t *, int);	
void dump_regs();
void init_gdb_regs(char *);
'''.format(proc_name))

cfile.write(
'''#include "interface_code.h"

reg_t reg_infos[NUM_REG + 1];

reg_data_t size_data[NUM_REG + 1] = {
''')

nb_line = 0;
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
		nb_line = nb_line + 1
		write_c_chunk(defline)



cfile.write('''	/* end of list */
	{{"", 0, "", 0}}
}};

'''.format(nb_line))


hfile.write(
'''

#define	NUM_REG	{0}

extern reg_data_t size_data[NUM_REG + 1];
extern reg_t reg_infos[NUM_REG + 1];

#endif /* INTERFACE_CODE_H */

'''.format(nb_line)
)


hfile.close
cfile.close
infile.close
