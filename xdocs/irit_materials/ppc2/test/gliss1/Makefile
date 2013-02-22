PPC2_PREFIX=$(PWD)/../..
PPC_PREFIX=$(PWD)/../../../ppc
GEL_PREFIX=$(PWD)/../../../gel


CFLAGS=\
	-I$(PPC2_PREFIX)/include \
	-I$(PPC_PREFIX)/src \
	-I$(PPC_PREFIX)/extern \
	-I$(GEL_PREFIX)/include \
	-g3 -Wall
LIBADD =  $(shell bash $(PPC2_PREFIX)/src/ppc-config --libs) -L$(PPC_PREFIX)/lib -lemul

all: comp

comp: comp.o
	libtool --mode=link gcc $(LDFLAGS) -o $@ $^ $(LIBADD) $(GEL_PREFIX)/src/libgel.la

comp: $(PPC2_PREFIX)/src/libppc.a

%.o : %.c
	$(CC) $(CFLAGS) -c $<

clean:
	rm -f comp comp.o
