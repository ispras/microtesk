################
# Makefile TOD #
################




##############
# PARAMETERS #
##############

# Ocaml Compiler
OCC=ocamlc
# Ocaml Doc Compiler
DOCC=ocamldoc -html
# Doc path
DOCP=doc


#########
# RULES #
#########

all: tod.cma

tod.cma: tod.cmo
	@echo "### Linking tod/tod.cma ###"
	$(OCC) tod.cmo -a -o tod.cma
	@echo ""

tod.cmo: tod.ml
	@echo "### Compile tod/tod.ml ###"
	$(OCC) -c tod.ml -o tod.cmo
	@echo ""

doc: tod.ml
	@echo "### Make Documentation ###"
	mkdir -p tod-doc
	ocamldoc -html tod.ml -d tod-doc
	@echo ""

clean:
	@echo "### Clean tod ###"
	rm -rf tod-doc tod.cmo
	@echo ""

