#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ppc/api.h>
#include <loader.h>
#include "iss_include.h"

extern char **environ;

/*** GLISS2 simulator ***/
ppc_sim_t *sim = 0;
ppc_state_t *state = 0, *save_state = 0;
ppc_platform_t *platform = 0;
ppc_loader_t *loader = 0;

int gliss2_prepare(int argc, char **argv) {
	ppc_address_t addr_start = 0;
	ppc_address_t addr_exit = 0;
	char *c_ptr = 0;
	Elf32_Sym *Elf_sym = 0;
	ppc_sym_t sym_exit = 0, sym_start = 0;
	int i;

	/* open the exec file */
	loader = ppc_loader_open(argv[1]);
	if(loader == NULL) {
		fprintf(stderr, "ERROR: no more resources\n");
		return 2;
	}

	/* find the _start symbol if no start address is given */
	addr_start = 0;
	Elf_sym = ppc_loader_first_sym(loader, &sym_start);
	while (sym_start >= 0)
	{
		Elf_sym = ppc_loader_next_sym(loader, &sym_start);
		if (Elf_sym)
		{
			c_ptr = ppc_loader_name_of_sym(loader, sym_start);
			if (strcmp(c_ptr, "_start") == 0)
			{
				/* we found _start */
				addr_start = Elf_sym->st_value;
				break;
			}
		}
	}

	/* start address found ? */
	if (addr_start == 0) {
		fprintf(stderr, "ERROR: cannot find the \"_start\" symbol and no start address is given.\n");
		return 2;
	}
	else
		printf("_start found at %08X\n", addr_start);

	/* find the _exit symbol if no exit address is given */
	addr_exit = 0;
	Elf_sym = ppc_loader_first_sym(loader, &sym_exit);
	while (sym_exit >= 0)
	{
		Elf_sym = ppc_loader_next_sym(loader, &sym_exit);
		if (Elf_sym)
		{
			c_ptr = ppc_loader_name_of_sym(loader, sym_exit);
			if (strcmp(c_ptr, "_exit") == 0)
			{
				/* we found _exit */
				addr_exit = Elf_sym->st_value;
				break;
			}
		}
	}

	/* exit address found ? */
	if (addr_exit == 0) {
		fprintf(stderr, "ERROR: cannot find the \"_exit\" symbol and no exit address is given.\n");
		return 2;
	}
	else
		printf("_exit found at %08X\n", addr_exit);

	/* cleanup first opening */
	/*ppc_loader_close(loader);*/

	/* make the platform */
	platform = ppc_new_platform();
	if(platform == NULL)  {
		fprintf(stderr, "ERROR: no more resources\n");
		return 2;
	}

	/* load the image in the platform */
	ppc_loader_load(loader, ppc_get_memory(platform, PPC_MAIN_MEMORY));

	/* make the state depending on the platform */
	state = ppc_new_state(platform);
	if (state == NULL)  {
		fprintf(stderr, "ERROR: no more resources\n");
		return 2;
	}

	/* make the simulator */
	sim = ppc_new_sim(state, addr_start, addr_exit);
	if (sim == NULL) {
		fprintf(stderr, "ERROR: no more resources\n");
		return 2;
	}

	/* prepare process configuration */
	ppc_env_t env;
	env.argc = argc - 1;
	env.argv = argv + 1;
	env.argv_addr = 0;
	env.envp = environ;
	env.envp_addr = 0;
	env.auxv = 0;
	env.auxv_addr = 0;
	env.stack_pointer = 0;

	/* system initialization (argv, env , auxv) */
	ppc_stack_fill_env(loader, ppc_get_memory(platform, PPC_MAIN_MEMORY), &env);
	ppc_registers_fill_env(&env, state);
	ppc_loader_close(loader);

	return 0;
}

void gliss2_cleanup(void) {
	ppc_delete_sim(sim);
}

void gliss2_step(void) {
	int i;

	/* save the current state */
	if(save_state)
		ppc_delete_state(save_state);
	save_state = ppc_copy_state(state);

	/* simulate */
	ppc_step(sim);
}

int gliss2_ended(void) {
	return ppc_is_sim_ended(sim);
}


ppc_address_t gliss2_pc(void) {
	return state->NIA;
}

int equals2(int param, int i) {
	switch(param) {
	case PPC_GPR_T: return state->GPR[i] == save_state->GPR[i];
	default: return 0;
	}
}


/*** GLISS1 simulator ***/
state_t s1;
state_t *real_state = 0, *save_real_state = &s1;

int gliss1_prepare(int argc, char **argv, char **envp) {
    code_t buff_instr[20];
    long int instruction_number;
    void *system_list[3];
    void *loader_list[4];
    int page_size_system;
    FILE *verbose_system;
    char * libs_path[]=	{
    	"/home/specmgr/Compilateur/powerpc/target/powerpc-linux-gnu/lib",
    	 NULL
    };
    int nb_bits=0; /* 0 => par defaut */
    int nb_mem=0;  /* 0 => par defaut */
    void *mem_list[2+1];

    page_size_system=4096;
    verbose_system=NULL;

    /* Set System Parameters (Stack...) */
	system_list[0] = &page_size_system;
    system_list[1] = verbose_system;
    system_list[2] = NULL;

    /* Loader args */
    loader_list[0]=&(argv[1]);   	/* pointe sur les parametres commencant par
                                       le nom du prog */
    loader_list[1]=envp;            /* environnement */
    loader_list[2]="../../gel/src"; /* gel plugin path */
    loader_list[3]=libs_path;

    /* Memory args */
    mem_list[0]=&nb_mem;
    mem_list[1]=&nb_bits;
    mem_list[2]=NULL;

    /* Init Emulator */
    real_state=iss_init(mem_list,loader_list,system_list,NULL,NULL);
    return real_state == 0;
}

void gliss1_cleanup(void) {
}

void gliss1_step(void) {
    code_t buff_instr[20];
    instruction_t *i;

	/* save state */
	memcpy(save_real_state, real_state, sizeof(state_t));
	/*iss_dump(stdout, real_state);*/

	/* emulation */
	iss_fetch(NIA(real_state), buff_instr);
	i = iss_decode(real_state, NIA(real_state), buff_instr);
	iss_complete(i, real_state);
	iss_free(i);
}

int gliss1_ended(void) {
	return !running;
}

ppc_address_t gliss1_pc(void) {
	return NIA(real_state);
}

int equals1(int param, int i) {
	switch(param) {
	case GPR_T: return real_state->gpr[i] == save_real_state->gpr[i];
	default: return 0;
	}
}


/*** stack compare ***/
char display(char c) {
	if(isprint(c))
		return c;
	else
		return '?';
}

void compare_stack(void) {
	address_t addr1 = real_state->gpr[1], top1 = 0x80000000;
	ppc_address_t addr2 = state->GPR[1], top2 = 0x80000000;
	printf("STACK COMPARISON\nSTACK1\t              \tSTACK2\n");

	while(addr1 < top1 || addr2 < top2) {

		// state 1
		if(addr1 < top1) {
			unsigned char	b0 = iss_mem_read8_little(real_state->M, addr1),
					b1 = iss_mem_read8_little(real_state->M, addr1 + 1),
					b2 = iss_mem_read8_little(real_state->M, addr1 + 2),
					b3 = iss_mem_read8_little(real_state->M, addr1 + 3);
			printf("%08lx %c%c%c%c %02x%02x%02x%02x\t",
				(unsigned long)addr1, display(b0), display(b1), display(b2), display(b3), b0, b1, b2, b3);
		}
		else
			printf("%08lx ???? XXXXXXXX\t", (unsigned long)addr1);
		addr1 += 4;

		// state 2
		if(addr2 < top2) {
			unsigned char	b0 = ppc_mem_read8(ppc_get_memory(platform, PPC_MAIN_MEMORY), addr2 + 0),
					b1 = ppc_mem_read8(ppc_get_memory(platform, PPC_MAIN_MEMORY), addr2 + 1),
					b2 = ppc_mem_read8(ppc_get_memory(platform, PPC_MAIN_MEMORY), addr2 + 2),
					b3 = ppc_mem_read8(ppc_get_memory(platform, PPC_MAIN_MEMORY), addr2 + 3);
			printf("%08lx %c%c%c%c %02x%02x%02x%02x\n",
				(unsigned long)addr2, display(b0), display(b1), display(b2), display(b3), b0, b1, b2, b3);
		}
		else
			printf("%08x ???? XXXXXXXX\t", addr2);
		addr2 += 4;
	}
}

void display_states(void) {
	int i;
	printf("BEFORE\tSTATE 1\t STATE2\tAFTER\tSTATE1 STATE2\n");
	for(i = 0; i < 32; i++)
		printf("r%d\t%08x %08x\t%08x %08x\n", i,
			save_real_state->gpr[i], save_state->GPR[i],
			real_state->gpr[i], state->GPR[i]);
}

/*** main program ***/
int main(int argc, char **argv) {
	int res, i;

	/* test argument count */
	assert(argc >= 2);

	/* preparation */
	res = gliss1_prepare(argc, argv, environ);
	if(res != 0)
		return res;
	res = gliss2_prepare(argc, argv);
	if(res != 0)
		return res;

	compare_stack();

	/* perform the simulation */
	while(1) {

		// test PC equality
		{
			ppc_address_t pc1 = gliss1_pc();
			ppc_address_t pc2 = gliss2_pc();
			if(pc1 != pc2) {
				fprintf(stderr, "ERROR: pc diverge (pc1 = %08x, pc2 = %08x)\n", pc1, pc2);
				return 1;
			}
		}

		// test end
		{
			int ended2 = gliss2_ended();
			int ended1 = gliss1_ended();
			if(ended1 != ended2) {
				fprintf(stderr, "ERROR: different ended result (1 -> %d, 2 -> %d)\n", ended1, ended2);
				return 1;
			}
			else if(ended1) {
				fprintf(stderr, "SUCCESS: co-simulation ended.\n");
				break;
			}
		}

		// simulation step
		printf("=== GLISS1 ===\n");
		gliss1_step();
		printf("=== GLISS2 ===\n");
		gliss2_step();
		printf("\n\n");

		// state comparison
		for(i = 0; i < 32; i++) {
			int eq1 = equals1(GPR_T, i);
			int eq2 = equals2(PPC_GPR_T, i);
			if(eq1 != eq2) {
				fprintf(stdout, "ERROR: difference in change for r%d (1: ", i);
				if(eq1)
					fprintf(stdout, "%08x", real_state->gpr[i]);
				else
					fprintf(stdout, "%08x -> %08x", save_real_state->gpr[i], real_state->gpr[i]);
				fprintf(stderr, ", ");
				if(eq2)
					fprintf(stdout, "%08x", state->GPR[i]);
				else
					fprintf(stdout, "%08x -> %08x", save_state->GPR[i], state->GPR[i]);
				fprintf(stdout, ")\n");
				display_states();
				return 1;
			}
			if((!eq1 || !eq2) && state->GPR[i] != real_state->gpr[i]) {
				fprintf(stdout, "ERROR: difference in value for r%d: 1->%08x, 2->%08x\n",
					i, real_state->gpr[i], state->GPR[i]);
				display_states();
				return 1;
			}
		}
	}

	/* cleanup */
	gliss1_cleanup();
	gliss2_cleanup();
	return 0;
}
