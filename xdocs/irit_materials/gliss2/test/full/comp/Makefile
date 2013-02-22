
CFLAGS=-I../include -I../src -g -I$(HOME)/otawa/ppc/include
LIBADD =  $(shell bash ../src/ppc-config --libs) -L$(HOME)/otawa/ppc/lib -lemul

all: comp

comp: comp.o
	libtool --mode=link gcc $(LDFLAGS) -o $@ $^ $(LIBADD) $(HOME)/otawa/gel/src/libgel.la

%.o : %.c
	$(CC) $(CFLAGS) -c $<

clean:
	rm -f comp comp.o
