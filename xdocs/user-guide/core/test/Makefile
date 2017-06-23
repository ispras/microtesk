MAIN    = MicroTESK.TestGuide.tex
SOURCE  = $(MAIN) overview.tex utils.tex sequence.tex data.tex engines.tex memconfig.tex biblio.tex
INCLUDE = memfonts.sty memlays.sty memsty.sty titlepages.sty memnoidxnum.tex trims-example.tex

SRC_DIR = src
INC_DIR = ../../../include/memoir/doc-src/

SRC_FILES = $(addprefix $(SRC_DIR), $(SOURCE))
INC_FILES = $(addprefix $(INC_DIR), $(INCLUDE))

all: $(SOURCE) $(INCLUDE)
	pdflatex $(MAIN)
	pdflatex $(MAIN)

$(SOURCE):
	cp $(SRC_DIR)/$@ $@

$(INCLUDE):
	cp $(INC_DIR)/$@ $@

clean:
	rm -rf *.*

