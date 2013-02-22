# $Id: Makefile,v 1.3 2009/07/31 09:09:42 casse Exp $
include Makefile.head

SUBDIRS=irg optirg gep

include Makefile.tail

DOCS = \
	irg/irg.ml \
	irg/sem.ml \
	irg/iter.ml \
	gep/toc.ml \
	gep/app.ml
DOCFLAGS = \
	-I irg -I gep -I optirg

autodoc: autodoc-force

autodoc-force:
	test -d autodoc || mkdir autodoc
	ocamldoc -html -d autodoc $(DOCFLAGS) $(DOCS)
