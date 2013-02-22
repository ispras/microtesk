CLEAN=include src disasm sim
GFLAGS=	-m loader:old_elf \
	-m fetch:fetch32 \
	-m decode:decode32 \
	-m inst_size:inst_size \
	-m code:code \
	-a disasm.c
GEP_DIR=../../gep


all: lib


src include: test.nml
	$(GEP_DIR)/gep $(GFLAGS) test.nml -S

lib: src src/config.h

toto-disasm:
	cd disasm; make

toto-sim:
	cd sim; make

src/config.h: config.tpl
	test -d src || mkdir src
	cp config.tpl src/config.h

src/disasm.c:
	$(GEP_DIR)/gliss-disasm test.nml -o $@ -c

clean:
	rm -rf $(CLEAN)
