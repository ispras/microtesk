# $Id: Makefile,v 1.11 2009/04/07 13:09:59 casse Exp $
include ../Makefile.head

OCAML_FLAGS=-I ../irg -I ../gep
ifndef NDEBUG
OCAML_FLAGS+=-g
OCAML_LDFLAGS+=-g
endif

# is_branch build
IS_BRANCH_SOURCES = \
	absint.ml \
	is_branch.ml
is_branch_LIBS = str unix ../irg/irg ../gep/libgep
$(eval $(call ocaml_prog,is_branch,$(IS_BRANCH_SOURCES)))
is_branch: ../irg/irg.cma ../gep/libgep.cma


# regs build
REG_SOURCES = \
	absint.ml \
	regSet.ml \
	state.ml \
	comput.ml \
	usedefs.ml \
	regs.ml
regs_LIBS = str unix ../irg/irg ../gep/libgep
$(eval $(call ocaml_prog,regs,$(REG_SOURCES)))
regs: ../irg/irg.cma ../gep/libgep.cma


include ../Makefile.tail
