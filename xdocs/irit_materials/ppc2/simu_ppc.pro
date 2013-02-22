OTHER_FILES += 
HEADERS += src/platform.h \
    src/fetch_table.h \
    src/decode_table.h \
    src/code_table.h \
    include/ppc/sysparm.h \
    include/ppc/syscall.h \
    include/ppc/mem.h \
    include/ppc/macros.h \
    include/ppc/loader.h \
    include/ppc/id.h \
    include/ppc/grt.h \
    include/ppc/fpi.h \
    include/ppc/fetch.h \
    include/ppc/exception.h \
    include/ppc/error.h \
    include/ppc/env.h \
    include/ppc/decode.h \
    include/ppc/config.h \
    include/ppc/code.h \
    include/ppc/api.h
SOURCES += src/syscall.c \
    src/sysparm.c \
    src/fetch.c \
    src/exception.c \
    src/fpi.c \
    src/grt.c \
    src/loader.c \
    src/mem.c \
    src/error.c \
    src/env.c \
    src/disasm.c \
    src/decode.c \
    src/code.c \
    src/api.c \
    sim/ppc-sim.c \
    test/gliss1/comp.c
