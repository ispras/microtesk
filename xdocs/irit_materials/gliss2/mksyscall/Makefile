# $Id: Makefile,v 1.1 2009/07/15 07:49:42 casse Exp $
include ../Makefile.head

SOURCES = \
	mksyscall.ml

OCAML_INC += ../../frontc/frontc
OCAML_LIB += unix ../../frontc/frontc/frontc

$(eval $(call ocaml_prog,mksyscall,$(SOURCES)))

include ../Makefile.tail
