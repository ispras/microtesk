MAIN    = MicroTESK.InstallGuide.tex
SOURCE  = $(MAIN) install.tex usage.tex options.tex
INCLUDE = memfonts.sty memlays.sty memsty.sty titlepages.sty memnoidxnum.tex

SRC_DIR = src
INC_DIR = ../include/memoir/doc-src/

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

