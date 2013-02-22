# $Id: Makefile,v 1.13 2009/09/15 07:50:48 casse Exp $
include ../../Makefile.head

OCAML_FLAGS=-I ../../irg -I ../../gep
ifndef NDEBUG
OCAML_FLAGS+=-g
OCAML_LDFLAGS+=-g
endif

# decode build
DECODE_SOURCES=\
	decode.ml
decode_LIBS = str unix  ../../irg/irg ../../gep/libgep
$(eval $(call ocaml_prog,decode,$(DECODE_SOURCES)))

decode: ../../irg/irg.cma ../../gep/libgep.cma

include ../../Makefile.tail
