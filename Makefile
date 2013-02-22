INCLUDE = models/tests
TEST    = models/tests/mainTest.nml

ifeq ($(shell uname), Linux)
    DELIMITER = :
else
    DELIMITER = ;
endif

LIB_CLASSPATH       = ../jars/ant-antlr3.jar$(DELIMITER)../jars/antlr-3.4-complete.jar$(DELIMITER)../jars/commons-cli-1.2.jar
MICROTESK_CLASSPATH = dist/microtesk.jar$(DELIMITER)$(LIB_CLASSPATH)

all:
	ant -lib "$(LIB_CLASSPATH)"

run:
	java -classpath "$(MICROTESK_CLASSPATH)" ru.ispras.microtesk.MicroTESK --include $(INCLUDE) $(TEST)

translator:
	java -classpath "$(MICROTESK_CLASSPATH)" ru.ispras.microtesk.translator.simnml.SimnMLTranslator $(TEST)

help:
	@echo "make [target], where target is all, run or help"