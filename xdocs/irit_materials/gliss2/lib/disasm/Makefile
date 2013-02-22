
CFLAGS=-I../include -I../src -g -O3
LIBADD += $$(shell bash ../src/$(proc)-config --libs)

all: $(proc)-disasm

$(proc)-disasm: $(proc)-disasm.o
	$$(CC) $$(CFLAGS) -o $$@ $$^ $$(LIBADD)

clean:
	rm -rf $(proc)-disasm.o

distclean: clean
	rm -rf $(proc)-disasm
