/*
 * Simulator base file.
 * Copyright (c) 2010, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of GLISS V2.
 *
 * OGliss is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * OGliss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGliss; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

#include <stdarg.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <signal.h>
#include <sys/time.h>
#if !defined(__WIN32) && !defined(__WIN64)
#	include <sys/resource.h>
#endif
#ifdef __MINGW32__
void *alloca(size_t);
#endif
#include <unistd.h>
#include <gliss/api.h>
#include <gliss/macros.h>
#include <gliss/loader.h>
#include <gliss/id.h>

/* interrupt handler */
static gliss_sim_t *sim;

static void handle_int(int signum) {
	puts("Interrupt caught ! Terminating...");
	gliss_set_sim_ended(sim);
}

static void handle_alarm(int signum) {
	puts("Alarm caught ! Terminating...");
	gliss_set_sim_ended(sim);
}


/**
 * Display usage of the command.
 * @param prog_name	Program name.
 */
void usage(const char *prog_name) {
	fprintf(stderr, "SYNTAX: %s OPTIONS <exec_name> <exec arguments>\n\n"
			"OPTIONS may be a combination of \n"
			"  -exit=<hexa_address>] : simulation exit address (default symbol _exit)\n"
			"  -f, -fast             : Step by step simulation is disable and straightforward execution is prefered (through run_sim())\n"
			"  -h, -help             : display usage message\n"
            "  -s                    : display user statistics\n"
            "  -more-stats           : display more statistics \n"
            "  -p, -profile=<path>   : generate the file <exec_name>.profile wich contains a statistical array of called instructions.\n"
            "                          Results are added to the file <exec_name>.profile. If the file does not exists it will be created.\n"
            "                          By default <exec_name>.profile is loaded and saved from the caller's current directory\n"
			"  -start=<hexa_address> : simulation start address (default symbol _start)\n"
			"  -t time               : stop the simulation after time seconds\n"
			"  -v, -verbose          : display simulated instructions\n"
			"\n"
			"if args or env strings must be passed to the simulated program,\n"
			"put them in <exec_name>.argv or <exec_name>.envp,\n"
			"one arg or env string on each line, whitespaces will not be ignored,\n"
			"a single '\\n' must be added on the last line of these files\n\n", prog_name);
}


/**
 * Display error.
 * @param fmt		Format string.
 * @param args		Format arguments.
 */
void error_args(const char *fmt, va_list args) {
	fprintf(stderr, "ERROR: ");
	vfprintf(stderr, fmt, args);
}


/**
 * Display error.
 * @param fmt		Format string.
 * @param ...		Format arguments.
 */
void error(const char *fmt, ...) {
	va_list args;
	va_start(args, fmt);
	error_args(fmt, args);
	va_end(args);
}


/**
 * Display a syntax error.
 * @param prog_name		Program name.
 * @param fmt			Format string.
 * @param ...			Format arguments.
 */
void syntax_error(char *prog_name, const char *fmt, ...) {
	va_list args;
	usage(prog_name);
	va_start(args, fmt);
	error_args(fmt, args);
	va_end(args);
}

extern char **environ;

typedef struct init_options_t {
	uint32_t flags;
#		define FLAG_ALLOCATED_ARGV	0x00000001
	int argc;
	char **argv;
	char **envp;
} init_options;


/**
 * Copy options from simulator to GLISS enviroment.
 * @param env	GLISS options to set.
 * @param opts	Simulator options to get.
 */
void copy_options_to_gliss_env(gliss_env_t *env, init_options *opt)
{
	env->argc = opt->argc;

	env->argv = opt->argv;
	env->argv_addr = 0;

	env->envp = opt->envp;
	env->envp_addr = 0;

	env->auxv = 0;
	env->auxv_addr = 0;

	env->stack_pointer = 0;
}


/**
 * Free the allocated options.
 * @param opt	Options to free.
 */
void free_options(init_options *opt)
{
	int i = 0;

	/* cleanup argv */
	if(opt->argv && (opt->flags & FLAG_ALLOCATED_ARGV)) {
		for (i = 0; opt->argv[i]; i++)
			free(opt->argv[i]);
		free(opt->argv);
	}

	/* cleanup envp */
	if (opt->envp) {
		for (i = 0; opt->envp[i]; i++)
			free(opt->envp[i]);
		free(opt->envp);
	}
}


/* replace every '\n' by '\0'
   after we read argv or envp file
   => there will be 2 '\0' after the last string
   return the number of '\n' replaced, ie the number of "substrings" */
int cut_multi_string(char *s)
{
	int i = 0;
	while (*s)
	{
		if (*s == '\n')
		{
			*s = '\0';
			i++;
		}
		s++;
	}
	return i;
}

/* find the beginning of the next "substring", 0 if none (ie the next '\0' is followed by another '\0')
   WARNING: be sure not to call this if no '\0' is after s */
char *next_multi_string(char *s)
{
	while (*s)
		s++;

	/*printf("next multi, s=%c[%02X], s+1=%c[%02X]\n", *s, *s, *(s+1), *(s+1));fflush(stdout);*/
	return s + 1;
}

/** Dump an entire line of text from "file_id" by reading it with getc() */
void dump_line(FILE* file_id)
{
    char cc;

    cc = fgetc(file_id);
    // Reading a char upto end of line
    while((cc != '\n') && (cc != EOF))
        cc = fgetc(file_id);
}

/** Dump every characters from "file_id" wether "\t" "\n" ' ' are reached */
void dump_word(FILE* file_id)
{
    char cc;
    cc = fgetc(file_id);
    // Dump first blank
    while((cc == '\n') || (cc == '\t') || (cc == ' '))
        cc = fgetc(file_id);
    // Dump the word
    while((cc != '\n') && (cc != '\t') && (cc != ' ') && (cc != EOF))
        cc = fgetc(file_id);
}

/**
 * Load or create a profiling file, any data will be loaded into the array inst_stat
 * otherwise each element of inst_stat will be initialize to zero
 * @param profiling_file_name       string of the profiling file to create or open
 * @param inst_stat                 array data initialized to zero or with the existing
 *                                  profiling file
 * @return the file identifier of the profiling file
 */
FILE* load_profiling_file(char* profiling_file_name, int inst_stat[])
{
    int   i, inst_id, stat;
    FILE* profile_id;
    profile_id = fopen(profiling_file_name,"r+");

    if(profile_id == NULL)
    {
        // Profile file does not exist
        if((profile_id = fopen(profiling_file_name,"w+")) == NULL)
            fprintf(stderr,"Creation of %s failed !", profiling_file_name), exit(EXIT_FAILURE);

        for(i = 0; i<GLISS_INSTRUCTIONS_NB; i++)
            inst_stat[i] = 0; // Init stats to zero
    }
    else
    {
        // Profile file already exists
        dump_line(profile_id); // dump title
        dump_line(profile_id); // dump proc name
        dump_line(profile_id); // dump column's titles

        // Init stats with the profiling file
        for(i = 0; i<GLISS_INSTRUCTIONS_NB; i++) {
			int g;
            dump_word(profile_id);
            g = fscanf(profile_id, "%d", &inst_id);
            g = fscanf(profile_id, "%d", &stat);
            inst_stat[inst_id] = stat;
        }
        // Erasing old profile :
        if( freopen (profiling_file_name, "w", profile_id) == NULL )
            fprintf(stderr,"Creation of %s failed !", profiling_file_name), exit(EXIT_FAILURE);
    }
    rewind(profile_id);
    fprintf(profile_id, "profiling statistics generated by GLISS2 for :\n");
    fprintf(profile_id, "%s\n",GLISS_PROC_NAME);
    fprintf(profile_id, "| instruction name | instruction identifier (cf. module id.h) | call count |\n");

    return profile_id;
}


/** write into the given profiling file "profile_id" data from "inst_stat" */
void write_profiling_file(FILE* profile_id, int inst_stat[])
{
	// Sort stats
    int i, j;
    struct {
		int id;    // instruction identifier
		int count; // total number instruction call
	}tmp, entries[GLISS_INSTRUCTIONS_NB];

	// Copy inst_stat[] into entries[]
	for(i = 0; i<GLISS_INSTRUCTIONS_NB; i++)
	{
		entries[i].id    = i;
		entries[i].count = inst_stat[i];
	}

	// sort entries[]
	for(i = 0; i<GLISS_INSTRUCTIONS_NB; i++)
	{
		for(j = i+1; j<GLISS_INSTRUCTIONS_NB; j++)
		{
			if( entries[j].count > entries[i].count)
			{
				tmp        = entries[i];
				entries[i] = entries[j];
				entries[j] = tmp;
			}
		}
	}

    // Write stats
    for(i = 0; i<GLISS_INSTRUCTIONS_NB; i++)
    {
        fprintf(profile_id, "%s %d %d\n",
        	gliss_get_string_ident(entries[i].id),	// instruction name
        	entries[i].id,							// instruction id
        	entries[i].count);						// instruction stats
    }
}


/**
 * Prepare options from a given table.
 * @param argc		Argument count.
 * @param argv		Argument list.
 * @param options	Options to initialize.
 */
void make_argv_from_table(int argc, char **argv, init_options *options) {
	options->argv = argv;
	options->argc = argc;
}


/**
 * Load argv from a configuration file.
 * @param app		Application name.
 * @param path		Path to the configuration file.
 * @param options	Options to initialize.
 */
int make_argv_from_file(const char *app, const char *path, init_options *options) {
	FILE *f;
	char *argv_str, *c_ptr;
	int file_size;
	int nb, toto, i;

	/* open the file */
	f = fopen(path, "r");
	if(f == NULL)
		return -1;

	/* get file size */
	fseek(f, 0, SEEK_END);
	file_size = ftell(f);
	rewind(f);

	/* allocate buffer */
	argv_str = malloc((file_size + 1) * sizeof(char));
	if (argv_str == 0) {
		error("ERROR: cannot allocate memory\n");
		return -1;
	}

	/* copy the file */
	if (fread(argv_str, sizeof(char), file_size, f) != file_size) {
		error("ERROR: cannot read the whole option file\n");
		return -1;
	}
	argv_str[file_size] = '\0';

	/* close the file */
	if (fclose(f)) {
		error("ERROR: cannot close the option file\n");
		return 1;
	}

	/* allocate argv array */
	nb = 0;
	toto = strlen(argv_str);
	nb = cut_multi_string(argv_str);
	options->argv = malloc((nb + 2) * sizeof(char *));
	if(options->argv == NULL) {
		error("ERROR: cannot allocate memory\n");
		return -1;
	}
	options->flags |= FLAG_ALLOCATED_ARGV;

	/* build first argument */
	c_ptr = argv_str;
	options->argv[0] = malloc(strlen(app) + 1);
	strcpy(options->argv[0], app);

	/* build other arguments */
	for(i = 1; i <= nb; i++) {
		options->argv[i] = malloc(sizeof(char) * (strlen(c_ptr) + 1));
		if(options->argv[i] == 0) {
			error("ERROR: cannot allocate memory\n");
			return -1;
		}
		strcpy(options->argv[i], c_ptr);
		c_ptr = next_multi_string(c_ptr);
	}

	/* last empty argument */
	options->argv[nb + 1] = 0;
	options->argc = nb + 1;

	/* cleanup */
	free(argv_str);
}


/**
 * Load envp from a configuration file.
 * @param path		Path to the configuration file.
 * @return			String of envp.
 */
char *make_envp_from_file(const char *path) {
	FILE *f;
	char *envp_str;
	int file_size;

	/* get the envp file and copy it into a buffer */
	f = fopen(path, "r");
	if(f == NULL)
		return NULL;

	/* get file size */
	fseek(f, 0, SEEK_END);
	file_size = ftell(f);
	rewind(f);

	/* allocate buffer */
	envp_str = malloc((file_size + 1) * sizeof(char));
	if(envp_str == 0) {
		error("ERROR: cannot allocate memory\n");
		return NULL;
	}

	/* copy the file */
	if(fread(envp_str, sizeof(char), file_size, f) != file_size) {
		error("ERROR: cannot read the whole option file\n");
		return NULL;
	}
	envp_str[file_size] = '\0';

	/* close the file */
	if (fclose(f)) {
		error("ERROR: cannot close the option file\n");
		return envp_str;
	}
	return envp_str;
}


/**
 * Build the envp in the given options.
 * @param path		Path to the envp file.
 * @param options	Options to initialize.
 * @return			0 for success, <0 else.
 */
int make_envp(char *path, init_options *options) {
	char *envp_str, *c_ptr;
	int nb, nb_bis, i;

	/* from file */
	envp_str = make_envp_from_file(path);

	/* find default env size and added env size*/
	nb = 0;
	while(environ[nb])
		nb++;
	nb_bis = 0;
	if(envp_str)
		nb_bis = cut_multi_string(envp_str);

	/* copy envs */
	options->envp = malloc((nb + nb_bis + 1) * sizeof(char *));
	if(options->envp == 0) {
		error("ERROR: cannot allocate memory\n");
		return -1;
	}

	/* 1st default env */
	for(i = 0; i < nb; i++) {
		options->envp[i] = malloc(sizeof(char) * (strlen(environ[i]) + 1));
		if(options->envp[i] == 0) {
			error("ERROR: cannot allocate memory\n");
			return -1;
		}
		strcpy(options->envp[i], environ[i]);
	}

	/* then added env */
	c_ptr = envp_str;
	for(i = nb; i < nb + nb_bis; i++) {
		options->envp[i] = malloc(sizeof(char) * (strlen(c_ptr) + 1));
		if (options->envp[i] == 0) {
			error("ERROR: cannot allocate memory\n");
			return -1;
		}
		strcpy(options->envp[i], c_ptr);
		c_ptr = next_multi_string(c_ptr);
	}

	/* final */
	options->envp[nb + nb_bis] = 0;
	if(envp_str)
		free(envp_str);
	return 0;
}


int main(int argc, char **argv) {
    gliss_state_t *state = 0;
    gliss_platform_t *platform = 0;
    gliss_loader_t *loader = 0;
    gliss_address_t addr_start = 0;
    gliss_address_t addr_exit = 0;
	char *c_ptr = 0;
    char *profiling_file_name;
    char *profiling_file_path = NULL;
    FILE *profile_id=0;
    int inst_stat[GLISS_INSTRUCTIONS_NB];
	int is_start_given = 0;
	int is_exit_given = 0;
	int prog_index = 0;
	/*Elf32_Sym *Elf_sym = 0;*/
	int sym_exit = 0, sym_start = 0;
	FILE *f = 0;
	/* this buffer should be big enough to hold an executable's name + 5 chars */
	char buffer[256];
	char *argv_str = 0;
	char *envp_str = 0;
	init_options options = {0, 0, 0};
	int i = 0;
	int nb, nb_bis = 0;
	long file_size = 0;
	int verbose = 0;
	int stats = 0;
    int profile = 0;
    int fast_sim = 0;
    int more_stat = 0;
	uint64_t inst_cnt = 0;
	uint64_t start_time=0, end_time, delay = 0;
	uint64_t start_sys_time=0, end_sys_time, sys_delay = 0;
	struct timeval start_all_time;
	int time = 0;

	/* scan arguments */
	for(i = 1; i < argc; i++) {

		/* -h or -help options */
		if(strcmp(argv[i], "-help") == 0 || strcmp(argv[i], "-h") == 0)  {
			usage(argv[0]);
			return 0;
		}

		/* -v or -verbose option */
		else if(strcmp(argv[i], "-verbose") == 0 || strcmp(argv[i], "-v") == 0)
			verbose = 1;

		else if(strcmp(argv[i], "-fast") == 0 || strcmp(argv[i], "-f") == 0)
			fast_sim = 1;
		else if(strcmp(argv[i], "-more-stats") == 0 )
			more_stat = 1;

        /* -p or -profile=<path> option */
        else if( strcmp(argv[i], "-p") == 0 || strcmp(argv[i], "-profile") == 0){
			profile = 1;
		}
        else if( strncmp(argv[i], "-profile=", 9) == 0 ){
            profile = 1;
            if(strlen(argv[i]) - 9){
                profiling_file_path = (char*)alloca( sizeof(char) * (strlen(argv[i]) - 9) );
                strcpy(profiling_file_path, argv[i]+9);
            }else{
                syntax_error(argv[0], "missing path for option : %s???\n", argv[i]);
                return 2;
            }

		}

		/* -start= option */
		else if(strncmp(argv[i], "-start=", 7) == 0) {
			is_start_given = i;
			addr_start = strtoul(argv[i] + 7, &c_ptr, 16);
			if(*c_ptr != '\0') {
				syntax_error(argv[0],  "bad start address specified : %s, only hexadecimal address accepted\n", argv[i]);
				return 2;
			}
		}

		/* -exit= option */
		else if(strncmp(argv[i], "-exit=", 6) == 0) {
			is_exit_given = i;
			addr_exit = strtoul(argv[is_exit_given] + 6, &c_ptr, 16);
			if(*c_ptr == '\0') {
				syntax_error(argv[0], "bad exit address specified : %s, only hexadecimal address accepted\n", argv[i]);
				return 2;
			}
		}

		/* -s option */
		else if(strcmp(argv[i], "-s") == 0)
			stats = 1;

		/* -t option */
		else if(strcmp(argv[i], "-t") == 0) {
			i++;
			if(i >= argc) {
				syntax_error(argv[0], "-t option requires a time argument");
				return 2;
			}
			time = strtoul(argv[i], NULL, 10);
		}

		/* option ? */
		else if(argv[i][0] == '-') {
			syntax_error(argv[0], "unknown option: %s\n", argv[i]);
			return 2;
		}

		/* free argument */
		else {
			prog_index = i;
			break;
		}
	}

	/* exec available ? */
	if(prog_index == 0) {
		syntax_error(argv[0], "no executable given !\n");
		return 2;
	}

	/* open the exec file */
    loader = gliss_loader_open(argv[prog_index]);
	if(loader == NULL) {
		fprintf(stderr, "ERROR: cannot open program %s\n", argv[prog_index]);
		return 2;
	}

	/* find the _start symbol if no start address is given */
	if(!is_start_given)
        addr_start = gliss_loader_start(loader);
	if(verbose)
		printf("START=%08x\n", addr_start);

	/* find the _exit symbol if no exit address is given */
	if (!is_exit_given) {
		int found = 0;

		/* search symbol _exit */
        for(sym_exit = 0; sym_exit < gliss_loader_count_syms(loader); sym_exit++) {
            gliss_loader_sym_t data;
            gliss_loader_sym(loader, sym_exit, &data);
			if(strcmp(data.name, "_exit") == 0) {
				/* we found _exit */
				addr_exit = data.value;
				found = 1;
				break;
			}
		}

		/* check for error */
		if(!found) {
			syntax_error(argv[0], "ERROR: cannot find the \"_exit\" symbol and no exit address is given.\n");
			return 2;
		}
	}
	if(verbose)
		printf("EXIT=%08x\n", addr_exit);

	options.argv = options.envp = 0;

	/* prepare the simulated argv */
	snprintf(buffer, sizeof(buffer), "%s.argv", argv[prog_index]);
	if(make_argv_from_file(argv[prog_index], buffer, &options) < 0)
		make_argv_from_table(argc - prog_index, argv + prog_index, &options);

	/* prepare the simulated envp */
	snprintf(buffer, sizeof(buffer), "%s.envp", argv[prog_index]);
	if(make_envp(buffer, &options) < 0)
		return 1;

	/* close loader file */
    gliss_loader_close(loader);

	/* make the platform */
    platform = gliss_new_platform();
	if (platform == NULL)  {
		fprintf(stderr, "ERROR: cannot create platform\n");
		return 2;
	}

	/* initialize system options */
    copy_options_to_gliss_env(gliss_get_sys_env(platform), &options);

	/* load the image in the platform */
    if (gliss_load_platform(platform, argv[prog_index]) == -1) {
		error("ERROR: cannot load the given executable : %s.\n", argv[i]);
		return 2;
	}

	/* free argv and envp once copied to simulator's memory */
	if(!argv_str)
		options.argv = NULL;
	free_options(&options);

	/* make the state depending on the platform */
    state = gliss_new_state(platform);
	if (state == NULL)  {
		fprintf(stderr, "ERROR: cannot create state\n");
		return 2;
	}

	/* make the simulator */
    sim = gliss_new_sim(state, addr_start, addr_exit);
	if (sim == NULL) {
		fprintf(stderr, "ERROR: cannot create simulator\n");
		return 2;
	}


	fflush(stdout);
	int cpt = 0;

	/* profile generation */
    if(profile)
    {
        // Append correctly path and name
        if( profiling_file_path != NULL){
			profiling_file_name = (char*)alloca(sizeof(char)* (strlen(profiling_file_path) + strlen(GLISS_PROC_NAME) + 10));
			strcpy(profiling_file_name, profiling_file_path);
			// Append the last slash if necessary
			if( strlen(profiling_file_path) )
				if(profiling_file_name[strlen(profiling_file_path)-1] != '/')
					strcat(profiling_file_name, "/");

			strcat(profiling_file_name, GLISS_PROC_NAME);
		}else{
			profiling_file_name = (char*)alloca(sizeof(char)* strlen(GLISS_PROC_NAME) + 10);
			strcpy(profiling_file_name, GLISS_PROC_NAME);
		}
        strcat(profiling_file_name,".profile");

        profile_id = load_profiling_file(profiling_file_name, inst_stat);
    }

	/* measure time */
#	if !defined(__WIN32) && !defined(__WIN64)
		if(stats) {
			struct rusage buf;
			getrusage(RUSAGE_SELF, &buf);
			start_time = (uint64_t)buf.ru_utime.tv_sec*1000000.00 + buf.ru_utime.tv_usec;
		}
		if(more_stat) {
			struct rusage buf;
			gettimeofday(&start_all_time, NULL);
			start_sys_time = (uint64_t)buf.ru_stime.tv_sec*1000000.00 + buf.ru_stime.tv_usec;
		}
#	endif

	/* initialize signals */
#	if !defined(__WIN32) && !defined(__WIN64)
		signal(SIGINT, handle_int);
		if(time) {
			signal(SIGALRM, handle_alarm);
			alarm(time);
		}
#	endif

	/* full speed simulation */
    if(!verbose && !profile)
    {

			if(fast_sim)
				inst_cnt += gliss_run_and_count_inst(sim);
			else
			{
				while(addr_exit != state->GLISS_PC_NAME)
				{
					gliss_step(sim);
					inst_cnt++;
				}
			}


	}

	/* verbose simulation */
	else
	{
        gliss_inst_t *inst;
        while(!gliss_is_sim_ended(sim))
		{
            inst = gliss_next_inst(sim);
            if( profile )
            {
                inst_stat[inst->ident]++;
            }

            if(verbose)
            {
                gliss_disasm(buffer, inst);
                fprintf(stderr, "%08x: %s\n", gliss_next_addr(sim),  buffer);
            }
            gliss_free_inst(inst);
            gliss_step(sim);
			inst_cnt++;
		}
	}

	/* produce statistics */
#	if !defined(__WIN32) && !defined(__WIN64)
		if(stats) {
			struct rusage buf;
			getrusage(RUSAGE_SELF, &buf);
			end_time = (uint64_t)buf.ru_utime.tv_sec*1000000.00 + buf.ru_utime.tv_usec;
			delay = end_time - start_time;
			fprintf(stderr, "Simulated instructions = %llu\n", inst_cnt);
			fprintf(stderr, "Time = %f ms\n", (double)delay / 1000.00);
			fprintf(stderr, "Rate = %f Mips\n", ((double)inst_cnt / (double)delay) );
		}
		if(more_stat)
		{
			struct rusage buf;
			struct timeval end_all_time, all_delay;
			double time;
			if(gettimeofday(&end_all_time, NULL) < 0) {
				fprintf(stderr, "ERROR: can not get time ?\n");
				return 1;
			}
			timersub(&end_all_time, &start_all_time, &all_delay);
			time = all_delay.tv_sec + (double)all_delay.tv_usec * 10E-6;

			end_sys_time = (uint64_t)buf.ru_stime.tv_sec*1000000.00 + buf.ru_stime.tv_usec;
			sys_delay = end_sys_time - start_sys_time;
			fprintf(stderr, "\nSystem (computed with rusage()): \n");
			fprintf(stderr, "Sys time = %f sec\n", (double)sys_delay / 1000000.00);
			fprintf(stderr, "\nUser+System (computed with gettimeofday()): \n");
			fprintf(stderr, "Time : %f sec\n", time);
			fprintf(stderr, "Rate = %f Mips\n", ((double)inst_cnt / time) / 1000000.00 );
		}
	#endif

    if(profile)
    {
        write_profiling_file(profile_id, inst_stat);
        fclose(profile_id);
    }

	/* cleanup */
    gliss_delete_sim(sim);
	return 0;
}
