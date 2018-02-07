TESTS = aligned_calls\
        block_random\
        block\
        branch_generation1\
        branch_generation2\
        branch_generation\
        bubble_sort\
        buffer_preparator\
        constraint_control_flow\
        constraint_immediate\
        constraint\
        coverage\
        data_files\
        data\
        data_streams\
        dependencies\
        endless_loop\
        euclid\
        external_labels2\
        external_labels3\
        external_labels\
        groups\
        insertion_sort\
        int_divide\
        int_exception\
        int_exception1\
        int_exception2\
        int_exception3\
        int_exception_branch\
        int_sqrt4\
        int_sqrt\
        jump\
        load_store\
        memory_object\
        memory_simulation\
        memory_situation2\
        memory_situation\
        memory_subsystem\
        min_max\
        multiple_engines\
        page_table\
        permutator\
        pseudo\
        random_immediate\
        random\
        random_registers\
        random_sequence\
        random_situation\
        register_allocation\
        selfchecks\
        testdata

all: $(TESTS)

$(TESTS):
	sh run.sh $@

clean:
	rm *.s *.o *.bin *.elf *.stdout *.stderr -f
