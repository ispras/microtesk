MEMORY=fast_mem
LOADER=old_elf
CFLAGS=-DTARGET_ENDIANNESS=little -DHOST_ENDIANNESS=big -Wall -g3
CLEAN=\
	grt.h \
	grt.c \
	memory.c \
	memory.h \
	loader.c \
	loader.h

all: main

main: main.o memory.o gliss.o loader.o

grt.h:
	ln -s ../../lib/grt.h

grt.c:
	ln -s ../../lib/grt.c

memory.h:
	ln -s ../../lib/$(MEMORY).h memory.h

memory.c:
	ln -s ../../lib/$(MEMORY).c memory.c

loader.h:
	ln -s ../../lib/$(LOADER).h loader.h

loader.c:
	ln -s ../../lib/$(LOADER).c loader.c

memory.o: memory.h
gliss.o: grt.h
loader.o: loader.h
main.o: memory.h grt.h loader.h

clean:
	rm *.o $(CLEAN)
