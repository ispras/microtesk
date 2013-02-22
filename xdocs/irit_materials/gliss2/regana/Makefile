# $Id: Makefile,v 1.1 2009/07/31 09:09:43 casse Exp $
include ../Makefile.head

OCAML_FLAGS=-I ../irg -I ../gep
ifndef NDEBUG
OCAML_FLAGS+=-g
OCAML_LDFLAGS+=-g
endif

# is_branch build
regana_SOURCES = \
	regana.ml
regana_LIBS = str unix ../irg/irg ../gep/libgep
$(eval $(call ocaml_prog,regana,$(regana_SOURCES)))
regana: ../irg/irg.cma ../gep/libgep.cma

include ../Makefile.tail
