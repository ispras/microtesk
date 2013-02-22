#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <fenv.h>


/* GLISS V2 */
#include <ppc/api.h>
#include <ppc/loader.h>
#include <ppc/api.h>
#include <ppc/env.h>

/* GLISS V1 */
#include <iss_include.h>


extern char **environ;

/*** GLISS2 simulator ***/
ppc_sim_t *sim = 0;
ppc_state_t *state = 0, *save_state = 0;
ppc_platform_t *platform = 0;
ppc_loader_t *loader = 0;
fenv_t fenv2;

int gliss2_prepare(int argc, char **argv) {
	ppc_address_t addr_start = 0;
	ppc_address_t addr_exit = 0;
	int i, found;

	/* open the exec file */
	loader = ppc_loader_open(argv[1]);
	if(loader == NULL) {
		fprintf(stderr, "ERROR: no more resources\n");
		return 2;
	}

	/* find the _start entry */
	addr_start = ppc_loader_start(loader);
	fprintf(stderr, "START=%08X\n", addr_start);

	/* find the _exit symbol if no exit address is given */
	found = 0;
	for(i = 0; i < ppc_loader_count_syms(loader); i++) {
		ppc_loader_sym_t sym;
		ppc_loader_sym(loader, i, &sym);
		if(strcmp(sym.name, "_exit") == 0) {
			addr_exit = sym.value;
			found = 1;
			break;
		}
	}
	if(!found) {
		fprintf(stderr, "ERROR: cannot find the \"_exit\" symbol and no exit address is given.\n");
		return 2;
	}
	else
		fprintf(stderr, "EXIT=%08X\n", addr_exit);

	/* build the program */
	platform = ppc_new_platform();
	if(platform == NULL)  {
		fprintf(stderr, "ERROR: no more resources\n");
		return 2;
	}
	ppc_load(platform, loader);

	/* make the state depending on the platform */
	state = ppc_new_state(platform);
	if (state == NULL)  {
		fprintf(stderr, "ERROR: no more resources\n");
		return 2;
	}

	/* system initialization (argv, env , auxv) */
	ppc_env_t env;
	env.argc = argc - 1;
	env.argv = argv + 1;
	env.argv_addr = 0;
	env.envp = environ;
	env.envp_addr = 0;
	env.auxv = 0;
	env.auxv_addr = 0;
	env.stack_pointer = 0;
	ppc_stack_fill_env(loader, platform, &env);
	ppc_registers_fill_env(&env, state);

	/* make the simulator */
	sim = ppc_new_sim(state, addr_start, addr_exit);
	if (sim == NULL) {
		fprintf(stderr, "ERROR: no more resources\n");
		return 2;
	}
	fegetenv(&fenv2);
	save_state = ppc_copy_state(state);

	ppc_loader_close(loader);
	return 0;
}

void gliss2_cleanup(void) {
	ppc_delete_sim(sim);
}

void gliss2_step(void) {

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
	return ppc_next_addr(sim);
}

/*int equals2(int param, int i) {
	switch(param) {
	case PPC_GPR_T: return state->GPR[i] == save_state->GPR[i];
	default: return 0;
	}
}*/


/*** GLISS1 simulator ***/
state_t s1;
state_t *real_state = 0, *save_real_state = &s1;
fenv_t fenv1;

int gliss1_prepare(int argc, char **argv, char **envp) {
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
    fegetenv(&fenv1);
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
	fprintf(stderr, "STACK COMPARISON\nSTACK1\t              \tSTACK2\n");

	while(addr1 < top1 || addr2 < top2) {

		// state 1
		if(addr1 < top1) {
			unsigned char	b0 = iss_mem_read8_little(real_state->M, addr1),
					b1 = iss_mem_read8_little(real_state->M, addr1 + 1),
					b2 = iss_mem_read8_little(real_state->M, addr1 + 2),
					b3 = iss_mem_read8_little(real_state->M, addr1 + 3);
			fprintf(stderr, "%08lx %c%c%c%c %02x%02x%02x%02x\t",
				(unsigned long)addr1, display(b0), display(b1), display(b2), display(b3), b0, b1, b2, b3);
		}
		else
			fprintf(stderr, "%08lx ???? XXXXXXXX\t", (unsigned long)addr1);
		addr1 += 4;

		// state 2
		if(addr2 < top2) {
			unsigned char	b0 = ppc_mem_read8(ppc_get_memory(platform, PPC_MAIN_MEMORY), addr2 + 0),
					b1 = ppc_mem_read8(ppc_get_memory(platform, PPC_MAIN_MEMORY), addr2 + 1),
					b2 = ppc_mem_read8(ppc_get_memory(platform, PPC_MAIN_MEMORY), addr2 + 2),
					b3 = ppc_mem_read8(ppc_get_memory(platform, PPC_MAIN_MEMORY), addr2 + 3);
			fprintf(stderr, "%08lx %c%c%c%c %02x%02x%02x%02x\n",
				(unsigned long)addr2, display(b0), display(b1), display(b2), display(b3), b0, b1, b2, b3);
		}
		else
			fprintf(stderr, "%08x ???? XXXXXXXX\t", addr2);
		addr2 += 4;
	}
}

void display_states(void) {
	int i;
	fprintf(stderr, "BEFORE\tSTATE 1\t STATE2\tAFTER\tSTATE1 STATE2\n");
	for(i = 0; i < 32; i++)
		fprintf(stderr, "r%d\t%08x %08x\t%08x %08x\n", i,
			save_real_state->gpr[i], save_state->GPR[i],
			real_state->gpr[i], state->GPR[i]);
	fprintf(stderr, "fpscr\t%08x %08x\t%08x %08x\n",
		save_real_state->fpscr, save_state->FPSCR,
		real_state->fpscr, state->FPSCR);
	for(i = 0; i < 32; i++)
		fprintf(stderr, "fr%d\t%016Lx %016Lx\t%016Lx %016Lx\n", i,
			*(uint64_t *)&save_real_state->fpr[i],
			*(uint64_t *)&save_state->FPR[i],
			*(uint64_t *)&real_state->fpr[i],
			*(uint64_t *)&state->FPR[i]);
	fprintf(stderr, "lr\t%08x %08x\t%08x %08x\n",
		save_real_state->lr, save_state->LR,
		real_state->lr, state->LR);
}

#define FR1		*(uint64_t *)&(real_state->fpr[i])
#define SFR1	*(uint64_t *)&(save_real_state->fpr[i])
#define FR2		*(uint64_t *)&(state->FPR[i])
#define SFR2	*(uint64_t *)&(save_state->FPR[i])
int compare_fpr(void) {
	int i;
	for(i = 0; i < 32; i++)
		if((FR1 != SFR1 || FR2 != SFR2) && FR1 != FR2) {
			display_states();
			fprintf(stderr, "DIFF: fpr[%d] -> %f ~ %f\n", i, real_state->fpr[i], state->FPR[i]);
			return -1;
		}
	return 0;
}

#define CHECK_BANK(id1, id2, size) \
	{ \
		int i; \
		for(i = 0; i < size; i++) \
			if( \
				(  save_state->id2[i] != state->id2[i] \
				|| save_real_state->id1[i] != real_state->id1[i]) \
			&&	state->id2[i] != real_state->id1[i]) { \
				display_states(); \
				fprintf(stderr, "DIFF: %s[%d]\n", #id1, i); \
				return 1; \
			} \
	}
#define CHECK_REG(id1, id2) \
	if( \
		(  save_state->id2 != state->id2 \
		|| save_real_state->id1 != real_state->id1) \
	&&	state->id2 != real_state->id1) { \
		display_states(); \
		fprintf(stderr, "DIFF: %s\n", #id1); \
		return 1; \
	}


int compare_fenv(void) {
	static int mask[] = {
		FE_DIVBYZERO,
		FE_INEXACT,
		FE_INVALID,
		FE_OVERFLOW,
		FE_UNDERFLOW
	};
	static char *names[] = {
		"FE_DIVBYZERO",
		"FE_INEXACT",
		"FE_INVALID",
		"FE_OVERFLOW",
		"FE_UNDERFLOW"
	};
	int i;
	int diff = 0;
	for(i = 0; i < sizeof(mask) / sizeof(int); i++) {
		int set1, set2;
		fesetenv(&fenv1);
		set1 = fetestexcept(mask[i]);
		fesetenv(&fenv2);
		set2 = fetestexcept(mask[i]);
		if((set1 == 0) != (set2 == 0)) {
			diff = 1;
			fprintf(stderr, "DIFF: fenv start: %s\n", names[i]);
		}
	}
	return diff;
}


/*** main program ***/
int main(int argc, char **argv) {
	int res;

	/* test argument count */
	assert(argc >= 2);

	/* preparation */
	res = gliss1_prepare(argc, argv, environ);
	if(res != 0)
		return res;
	res = gliss2_prepare(argc, argv);
	if(res != 0)
		return res;

	/* initial comparison */
	compare_stack();
	if(compare_fenv())
		return 1;

	/* perform the simulation */
	while(1) {

		// test PC equality
		{
			ppc_address_t pc1 = gliss1_pc();
			ppc_address_t pc2 = gliss2_pc();
			if(pc1 != pc2) {
				display_states();
				fprintf(stderr, "ERROR: pc diverge (pc1 = %08x, pc2 = %08x)\n", pc1, pc2);
				return 1;
			}
		}

		// test end
		if(gliss2_ended()) {
			fprintf(stderr, "SUCCESS: co-simulation ended.\n");
			break;
		}

		/* traces */
		{
			char buffer[256];
			ppc_inst_t *inst = ppc_next_inst(sim);
			ppc_disasm(buffer, inst);
			fprintf(stderr, "%08x: %s\n", ppc_next_addr(sim),  buffer);
			ppc_free_inst(inst);
		}

		// simulation step
		fesetenv(&fenv1);
		gliss1_step();
		fegetenv(&fenv1);
		fesetenv(&fenv2);
		gliss2_step();
		fegetenv(&fenv2);

		// state comparison
		CHECK_BANK(gpr, GPR, 32);
		CHECK_REG(fpscr, FPSCR);
		CHECK_REG(lr, LR);
		if(compare_fpr() < 0)
			return 1;
	}

	/* cleanup */
	gliss1_cleanup();
	gliss2_cleanup();
	return 0;
}
