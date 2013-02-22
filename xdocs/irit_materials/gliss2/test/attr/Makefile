all: cst.c fun.c

cst.c:
	../../gep/gliss-attr ppc.nml -o $@ -a otawa_kind -d 0 -t kind.c

fun.c:
	../../gep/gliss-attr ppc.nml -o $@ -a otawa_kind -f -d "return 0;" -t kind-fun.c

clean:
	rm -rf cst.c fun.c

