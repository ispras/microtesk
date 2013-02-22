GLISS_ARCH=$(PWD)/../
ARCH_IRG=$(GLISS_ARCH)/arm.irg
GLISS2_PREFIX=$(PWD)/../../gliss2
GLISS2_ATTR=$(GLISS2_PREFIX)/gep/gliss-attr
NMP2NML=$(GLISS2_PREFIX)/gep/gliss-nmp2nml.pl

GNU_TARGET=arm-elf
GDB_TARGET_PATH=/home/traces/Arm/bin
GDB_NAME=$(GDB_TARGET_PATH)/$(GNU_TARGET)-gdb
PROC_NAME=arm


#do not modify below this line
SRCS = main.c gdb_interface.c interface_code_dump_regs.c $(PROC_NAME)_register.c \
	interface_code_compare_regs.c log.c interface_code_read_vars.c interface_code.c
INCL = all_inc.h interface_code.h internal.h $(PROC_NAME)_register.h

OBJS = $(SRCS:.c=.o)
CC = gcc
CFLAGS = -W -Wall -g -I$(GLISS_ARCH)/include -DGNU_TARGET=\"$(GNU_TARGET)\" -DGDB_NAME=\"$(GDB_NAME)\" -DGLISS_API_H=\<$(PROC_NAME)/api.h\> -DGLISS_REG_H=\"$(PROC_NAME)_register.h\" -DPROC'(x)'=$(PROC_NAME)\#\#x 
#TODO change the next line when gliss2 library will be generated in the right place
LDFLAGS = -L$(GLISS_ARCH)/src
LINK = libtool --mode=link $(CC) $(LINKFLAGS) -o $@

%.o: %.c
	$(CC) -o $@ -c $< $(CFLAGS)

all : validator2-$(PROC_NAME)

validator2-$(PROC_NAME) : interface_code.h $(INCL) $(GLISS_ARCH)/validator.cfg $(OBJS) ../src/lib$(PROC_NAME).a
	$(LINK) $(OBJS) -lm  -l$(PROC_NAME) $(LDFLAGS) 

clean:
	rm -f $(OBJS)

distclean: clean
	rm -f validator2-$(PROC_NAME) otawa_used_regs.h used_regs.nml interface_code.h interface_code.c


#otawa_used_regs.h: used_regs.tpl $(ARCH_IRG) used_regs.nml
#	$(GLISS2_ATTR) -o $@ -a otawa_used_regs -d "tmp_var[0] = END_REG; return 0;" -f -t $< -e used_regs.nml $(ARCH_IRG)

#used_regs.nml: used_regs.nmp 
#	$(NMP2NML) $< $@


interface_code.o: interface_code.c
interface_code.c: interface_code.h
interface_code.h: generate_interface_code.py $(GLISS_ARCH)/validator.cfg
	 python generate_interface_code.py -i $(GLISS_ARCH)/validator.cfg -p $(PROC_NAME)


#leon_reg: interface_code.h leon_register.h
# replace state->R[i] by get_win_reg(state, i), we suppose the name of the state has only minuscules and '_'
# and that the index is a pure decimal number. we need to do it only for the following 2 files
#	sed -i 's/\([a-z_]\+\)->R\[\([0-9]\+\)\]/get_win_reg(\1, \2)/g' interface_code_read_vars.c
