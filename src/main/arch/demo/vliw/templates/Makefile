TESTS = combination\
        euclid\
        euclid_loop\
        fp_example\
        fp_exception\
        int_divide\
        int_example\
        int_exception\
        int_sqrt4\
        int_sqrt

all: $(TESTS)

$(TESTS):
	sh run.sh $@

clean:
	rm *.s *.o *.bin *.elf *.stdout *.stderr -f
