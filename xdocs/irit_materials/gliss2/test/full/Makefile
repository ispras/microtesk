CLEAN=include src disasm sim ppc.nml ppc.irg
GFLAGS=-m \
	sysparm:sysparm-reg32 \
	-m loader:old_elf \
	-m syscall:syscall-linux \
	-m fetch:fetch32 \
	-m decode:decode32 \
	-m inst_size:inst_size \
	-m code:code \
	-m exception:extern/exception \
	-m fpi:extern/fpi \
	-a disasm.c

NMP =\
	nmp/ppc.nmp \
	nmp/oea_instr.nmp \
	nmp/uisa_fp_instr.nmp  \
	nmp/vea_instr.nmp


all: lib ppc-disasm ppc-sim

ppc.irg: nmp/ppc.nmp
	../../irg/mkirg $< $@

ppc.nml: nmp/ppc.nmp
	../../gep/gliss-nmp2nml.pl $< > $@

src include: ppc.irg
	../../gep/gep $(GFLAGS) $<	 -S

lib: src src/config.h src/disasm.c
	(cd src; make)

ppc-disasm:
	cd disasm; make

ppc-sim:
	cd sim; make

src/config.h: config.tpl
	test -d src || mkdir src
	cp config.tpl src/config.h

src/disasm.c: ppc.irg
	../../gep/gliss-disasm $< -o $@ -c

clean:
	rm -rf $(CLEAN)
