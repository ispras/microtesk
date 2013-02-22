# base directory containing the GLISS2 description for the given processor
GLISS_ARCH=/home/barre/bzr_projects/leon/
# irg file for processor description, usually in $(GLISS_ARCH) directory
ARCH_IRG=$(GLISS_ARCH)/leon.irg
# file with description of GLISS2 registers and corresponding gdb ones
CFG=./validator.cfg

# GNU target corresponding to the given processor,
# should be the prefix of a gdb command suitable for the processor
GNU_TARGET=sparc-elf
# processor specific gdb command
GDB_NAME=/home/barre/leon-tools/sparc-elf-4.4.2/bin/$(GNU_TARGET)-gdb

# name of the emulated processor
PROC_NAME=leon
# processor specific files, contain register access functions and structures initialization.
# as an examples, the ones for the LEON are given
PROC_SRC= $(PROC_NAME)_register.c
PROC_INCL= $(PROC_NAME)_register.h
# processor specific initialization, usually needed because some regs
# are not initialized the same way in GLISS as in GDB.
# macro with same name called in init_gliss() from main.c
PROC_INIT_CODE={ \
	/* set gliss stack pointer with same value as gdb */ \
	/* sp is o6, also r14, fp is i6, also r30, fsr is 0 */ \
	/* TODO, see if tsim put something in stack */ \
	/* only zeroes are in the stack up to the fp addr, then garbage (looks like undefined) */ \
	/* temp debug, set psr to the same value as gdb */ \
	real_state->PSR = 0xF30010E0; \
	set_reg(real_state, 14, sp); \
	set_reg(real_state, 30, fp); \
	real_state->WIM = 2; \
	real_state->FSR = 0; \
	}
PROC_CFLAGS=  -DGLISS_REG_H=\"$(PROC_INCL)\" -DPROC_INIT_CODE='$(PROC_INIT_CODE)'

#do not modify below this line
SRCS = main.c io_module.c gdb_interface.c interface_code_dump_regs.c \
	interface_code_compare_regs.c log.c interface_code_read_vars.c interface_code.c $(PROC_SRC)
INCL = io_module.h all_inc.h  interface_code.h  internal.h $(PROC_INCL)

OBJS = $(SRCS:.c=.o)
CC = gcc
CFLAGS = -W -Wall -g -I$(GLISS_ARCH)/include -DGNU_TARGET=\"$(GNU_TARGET)\" -DGDB_NAME=\"$(GDB_NAME)\" -DGLISS_API_H=\<$(PROC_NAME)/api.h\> -DPROC'(x)'=$(PROC_NAME)\#\#x $(PROC_CFLAGS)
LDFLAGS = -L$(GLISS_ARCH)/src
LINK = libtool --mode=link $(CC) $(LINKFLAGS) -o $@

%.o: %.c
	$(CC) -o $@ -c $< $(CFLAGS)

all : validator2-$(PROC_NAME)

validator2-$(PROC_NAME) : interface_code.h $(INCL) $(CFG) $(OBJS)
	$(LINK) $(OBJS) -lm  -l$(PROC_NAME) $(LDFLAGS) 

clean:
	rm -f $(OBJS)

distclean: clean
	rm -f validator2-$(PROC_NAME) interface_code.h interface_code.c


interface_code.o: interface_code.c
interface_code.c: interface_code.h
interface_code.h: generate_interface_code.py $(CFG)
	 python generate_interface_code.py -i $(CFG) -p $(PROC_NAME)

