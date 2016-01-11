ARTICLE=nml_reference_manual

all: ${ARTICLE}.pdf

${ARTICLE}.pdf: ${ARTICLE}.tex
	pdflatex ${ARTICLE}
	pdflatex ${ARTICLE}
	acroread ${ARTICLE}.pdf &

clean:
	rm -rf *.dvi *.log *.aux
