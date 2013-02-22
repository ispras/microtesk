#####################
# Makefile GraphIRG #
#####################




##############
# PARAMETERS #
##############

# Ocaml Compiler
OCC=ocamlc
# Ocaml Doc Compiler
DOCC=ocamldoc -html
# Doc path
DOCP=doc

# TOD path
TOD=./tod

# IRG path
IRG=../irg

# Includes
INC=-I $(TOD) -I $(IRG)



#########
# RULES #
#########

all: graphirg.cma graphirg

# Linking

graphirg: $(IRG)/irg.cma graphirg.cma main.cmo
	@echo "### Linking graphirg/graphirg ###"
	$(OCC) $(INC) str.cma $(IRG)/irg.cma graphirg.cma main.cmo -o graphirg
	chmod ug+x graphirg
	@echo ""

graphirg.cma: $(TOD)/tod.cma $(IRG)/irg.cma graphirg.cmo
	(cd $(TOD) ; make)
	(cd $(IRG) ; make)
	@echo "### Linking graphirg/graphirg.cma ###"
	$(OCC) $(INC) str.cma $(TOD)/tod.cma $(IRG)/irg.cma graphirg.cmo -a -o graphirg.cma
	@echo ""

# Compilation

main.cmo: main.ml graphirg.cmo $(IRG)/irg.cma
	@echo "### Compile graphirg/main.ml ###"
	$(OCC) $(INC) -c main.ml -o main.cmo 
	@echo ""
	

graphirg.cmo: graphirg.ml
	@echo "### Compile graphirg/graphirg.ml ###"
	$(OCC) $(INC) -c graphirg.ml -o graphirg.cmo 
	@echo ""

# Recusive call
$(IRG)/irg.cma: 
	(cd $(IRG) ; make)
$(TOD)/tod.cma: 
	(cd $(TOD) ; make)

# Documentation 
doc: graphirg.ml 
	@echo "### Make Documentation ###"
	mkdir -p $(DOCP)
	$(DOCC) $(INC) $(TOD)/tod.cma $(IRG)/irg.cma graphirg.cmo -d $(DOCP)
	@echo ""

# Cleaning
clean: 
	(cd $(TOD) ; make clean)
	(cd $(IRG) ; make clean)
	@echo "### Clean graphirg ###"
	rm -rf *.cmo *.cmi $(DOCP)
	@echo ""

