MODULE=vfast_mem
CFLAGS=-I . -g3

all: test

test: test.o mem.o

mem.o test.o: gliss/mem.h

gliss/mem.h: ../../lib/$(MODULE).h gliss
	cp $< $@

mem.c: ../../lib/$(MODULE).c
	cp $< $@

gliss:
	mkdir gliss

clean:
	rm -rf mem.c gliss/mem.h *.o

distclean:
	rm -rf test
