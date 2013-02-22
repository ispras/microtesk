.PHONY: NMP

all: NMP.html gliss.html

%.html: %.thot
	../../thot/thot.py -t html $< -D HTML_ONE_FILE_PER=chapter

gliss.html: $(wildcard gliss-*.thot)

clean:
	rm -rf gliss*.html
