
GNU_TARGET=powerpc-linux
GDB_TARGET_PATH=/home/barre/gdb-ppc-linux/bin
GLISS_ARCH=/home/barre/gliss2-caml/gliss2/test/full
GDB_NAME=$(GDB_TARGET_PATH)/$(GNU_TARGET)-gdb
PROC_NAME=ppc


#do not modify below this line
SRCS = main.c gdb_interface.c log.c interface_code_create_vars.c interface_code_read_vars.c interface_code_compare_regs.c interface_code_dump_regs.c interface_code_init_gdb_regs.c
OBJS = $(SRCS:.c=.o)
CC = gcc
CFLAGS = -W -Wall -O3 -g -I$(GLISS_ARCH)/include -DGNU_TARGET=\"$(GNU_TARGET)\" -DGDB_NAME=\"$(GDB_NAME)\" -DGLISS_API_H=\"$(PROC_NAME)/api.h\" -DPROC'(x)'=$(PROC_NAME)\#\#x
#TODO change the next line when gliss2 library will be generated in the right place
LDFLAGS = -L$(GLISS_ARCH)/src
LINK = libtool --mode=link $(CC) $(LINKFLAGS) -o $(GLISS_ARCH)/$@

%.o: %.c
	$(CC) -o $@ -c $< $(CFLAGS)

all : validator-$(PROC_NAME)

validator-$(PROC_NAME) : interface_code_create_vars.c interface_code.h interface_code_read_vars.c interface_code_compare_regs.c interface_code_dump_regs.c interface_code_init_gdb_regs.c $(OBJS) $(GLISS_ARCH)/validator.cfg
	$(LINK) $(OBJS) -lm  -l$(PROC_NAME) $(LDFLAGS) 
#../gel/src/libgel_ppc.la $(LDFLAGS)

.PHONY : interface_code.h interface_code_read_vars.c interface_code_compare_regs.c interface_code_dump_regs.c interface_code_init_gdb_regs.c $(GLISS_ARCH)/validator.cfg

clean:
	rm -f *.o interface_code_*.c interface_code.h


interface_code_create_vars.c : generate_interface_code.py $(GLISS_ARCH)/validator.cfg
	python generate_interface_code.py -i $(GLISS_ARCH)/validator.cfg
