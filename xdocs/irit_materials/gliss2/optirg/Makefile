# Makefile for optirg commands and library
include ../Makefile.head

# flags
ifndef NDEBUG
OCAML_FLAGS+=-g
OCAML_LDFLAGS+=-g
endif

IRG=../irg
OCAML_FLAGS += -I $(IRG)
OCAML_LDFLAGS += -I $(IRG)


# library
LIB_SOURCES = \
	formatlexer.ml \
	string_of_expr.ml \
	image_attr_size.ml \
	sw_fun.ml \
	irgp.ml \
	canon.ml \
	node_classes.ml \
	classfusion.ml \
	optirg.ml 
$(eval $(call ocaml_lib,liboptirg,$(LIB_SOURCES)))


# command
optirg_LIBS=unix str ../irg/irg liboptirg
$(eval $(call ocaml_prog,optirg,main.ml))

include ../Makefile.tail
