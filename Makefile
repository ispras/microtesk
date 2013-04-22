INCLUDE = models/tests
TEST    = models/tests/mainTest.nml
JARS    = ./jars

ifeq ($(shell uname), Linux)
    DELIMITER = :
else
    DELIMITER = ;
endif

MICROTESK_CLASSPATH = dist/microtesk.jar$(DELIMITER)$(LIB_CLASSPATH)

all:
	ant -lib $(JARS)

clean:
	ant -lib $(JARS) clean

run:
	java -classpath "$(MICROTESK_CLASSPATH)" ru.ispras.microtesk.MicroTESK --include $(INCLUDE) $(TEST)

translator:
	java -classpath "$(MICROTESK_CLASSPATH)" ru.ispras.microtesk.translator.simnml.SimnMLTranslator $(TEST)

junit:
	ant -lib $(JARS) junit

help:
	@echo "make [target], where target is all, run or help"
