CFLAGS=-I../include
LIBADD=-L../src -lppc

SOURCES = main.c
OBJECTS = $(SOURCES:.c=.o)
CLEAN = OBJECTS main

all: main

main: $(OBJECTS)
	$(CC) -o $@ $^ $(LIBADD)

clean:
	rm *.o $(CLEAN)

main: ../src/libppc.a
