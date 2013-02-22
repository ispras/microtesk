# Makefile for the Leon architecture using GLISS V2

# configuration
GLISS_PREFIX=../../../gliss2
WITH_DISASM=1	# comment it to prevent disassembler building
WITH_SIM=1		# comment it to prevent simulator building

MEMORY=fast_mem
LOADER=old_elf
SYSCALL=syscall-linux


# files
GOALS=
ifdef WITH_DISASM
GOALS+=toto-disasm
endif
ifdef WITH_SIM
GOALS+=toto-sim
endif

SUBDIRS=src sim disasm
CLEAN=toto.nml toto.irg
DISTCLEAN=include src disasm sim

GFLAGS=\
	-m mem:$(MEMORY) \
	-m loader:$(LOADER) \
	-m syscall:$(SYSCALL) \
	-m sysparm:sysparm-reg32 \
	-m fetch:fetch32 \
	-m decode:decode32 \
	-m code:code \
	-m env:void_env \
	-a disasm.c

NMP =\
	toto.nmp


# targets
all: lib $(GOALS)

toto.nml: $(NMP)
	$(GLISS_PREFIX)/gep/gliss-nmp2nml.pl $< $@

toto.irg: toto.nml
	$(GLISS_PREFIX)/irg/mkirg $< $@

src include: toto.irg
	$(GLISS_PREFIX)/gep/gep $(GFLAGS) $< -S

lib: src include/toto/config.h src/disasm.c
	(cd src; make)

toto-disasm:
	cd disasm; make

toto-sim:
	cd sim; make

include/toto/config.h: config.tpl
	test -d include/toto || mkdir include/toto
	cp config.tpl include/toto/config.h

src/disasm.c: toto.irg
	$(GLISS_PREFIX)/gep/gliss-disasm $< -o $@ -c

distclean: clean
	for d in $(SUBDIRS); do test -d $$d && (cd $$d; make distclean || exit 1); done
	rm -rf $(DISTCLEAN)

clean: only-clean
	for d in $(SUBDIRS); do test -d $$d && (cd $$d; make clean || exit 1); done

only-clean:
	rm -rf $(CLEAN)
