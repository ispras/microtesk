# Makefile for tricore using GLISS V2

# configuration
GLISS_PREFIX=../gliss2
WITH_DISASM=1	# comment it to prevent disassembler building
WITH_SIM=1		# comment it to prevent simulator building
#WITH_DYNLIB=1	# uncomment to link with a shared object

MEMORY=vfast_mem
#LOADER=old_elf


# files
ARCH=tricore
GOALS=
ifdef WITH_DISASM
GOALS+=$(ARCH)-disasm
endif
ifdef WITH_SIM
GOALS+=$(ARCH)-sim
endif

SUBDIRS=src sim disasm
CLEAN=$(ARCH).nml $(ARCH).irg
DISTCLEAN=include src disasm sim

GFLAGS=\
	-m mem:$(MEMORY) \
	-m code:code \
	-m env:void_env \
	-m loader:old_elf \
	-a disasm.c \
	-a used_regs.c
# correct fetch and decode will be chosen auto
# except if you want one of the optimized decode (for the moment)
#	-m fetch:fetch \
#	-m decode:decode \

MAIN_NMP=$(ARCH).nmp
NMP = $(shell find nmp -name "*.nmp")
ifdef WITH_DYNLIB
MFLAGS=WITH_DYNLIB=1
endif

# targets
all: lib $(GOALS)

$(ARCH).nml: $(NMP)
	(cd nmp; pwd; ../$(GLISS_PREFIX)/gep/gliss-nmp2nml.pl $(MAIN_NMP) ../$@)

$(ARCH).irg: $(ARCH).nml
	$(GLISS_PREFIX)/irg/mkirg $< $@

src include: $(ARCH).irg
	$(GLISS_PREFIX)/gep/gep $(GFLAGS) $< -S

lib: src include/$(ARCH)/config.h src/disasm.c src/used_regs.c
	(cd src; make $(MFLAGS))

$(ARCH)-disasm:
	cd disasm; make

$(ARCH)-sim:
	cd sim; make

include/$(ARCH)/config.h: config.tpl
	test -d include/$(ARCH) || mkdir include/$(ARCH)
	cp config.tpl include/$(ARCH)/config.h

src/used_regs.c: $(ARCH).irg
	$(GLISS_PREFIX)/gep/gliss-used-regs $<

src/disasm.c: $(ARCH).irg
	$(GLISS_PREFIX)/gep/gliss-disasm $< -o $@ -c

distclean: clean
	-for d in $(SUBDIRS); do test -d $$d && (cd $$d; make distclean || exit 0); done
	-rm -rf $(DISTCLEAN)

clean: only-clean
	-for d in $(SUBDIRS); do test -d $$d && (cd $$d; make clean || exit 0); done

only-clean:
	-rm -rf $(CLEAN)
