# $Id: Makefile,v 1.15 2009/07/31 09:09:42 casse Exp $
include ../Makefile.head

OCAMLYACC_FLAGS = -v

ifndef NDEBUG
OCAML_FLAGS+=-g
OCAML_LDFLAGS+=-g
endif

LIB_SOURCES = \
	config.ml \
	IdMaker.ml \
	irg.ml \
	instantiate.ml \
	lexer.mll \
	sem.ml \
	iter.ml \
	parser.mly \
	irgUtil.ml
$(eval $(call ocaml_byte_code_lib,irg,$(LIB_SOURCES)))

print_irg_LIBS=unix str irg
$(eval $(call ocaml_prog,print_irg,print_irg.ml))

mkirg_LIBS=unix str irg
$(eval $(call ocaml_prog,mkirg,mkirg.ml))

CLEAN += config.ml

config.ml:
	echo "let install_dir = \"$(PREFIX)\"" > config.ml
	echo "let source_dir = \"$(PWD)/..\"" >> config.ml

include ../Makefile.tail
