TESTS = block\
        block_random\
        bubble_sort\
        bubble_sort_386\
        euclid\
        random\
        random_immediate\
        random_registers\
        simple_template\
        simple_template2

all: $(TESTS)

$(TESTS):
	sh run.sh $@

clean:
	rm *.s *.o *.bin *.elf *.stdout *.stderr -f
