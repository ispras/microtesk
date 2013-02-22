/*********************
Gliss CPU simulator validator
main.c : main functions
**********************/

#define MAIN_C
#define ISS_DISASM
#include "all_inc.h"

#include "interface_code.h"

pid_t drive_gdb_pid;

void drive_gdb(); //la fonction qui exec() gdb
int init_gdb(char *, char *); //initialisation de gdb
int init_gliss(char *);
void gdb_disasm_error_report();
char target_location[50];
int exit_on_errors = 1;
char gpname[200];
int init_registers = 1; 
int instr_count;

void catch_sigusr1(int sig)
	{
	fprintf(stderr, "Program at 0x%08x, instruction number %d\n", gdb_pc, instr_count);
	}
	
void usage(char * pname)
	{
	fprintf(stderr, "Usage: %s\t[-V|--version] [-h|--help] [--debug-log|-l] [--display_values|-v] [--display-replies|-r] [--no-exit-error|-x] [--test-program|-p] [--no-reg-init|-n] [target_host:port]\n", pname);
	fprintf(stderr, "\t--version\t\tdisplay version number\n\t--help  \t\tdisplay this help screen\n");
	fprintf(stderr, "\t--debug-log\t\tlog all communication with gdb in `pwd`/log\n");
	fprintf(stderr, "\t--display_values\tdisplay registers values on screen\n");
	fprintf(stderr, "\t--display_replies\tdisplay GDB replies on screen\n");
	fprintf(stderr, "\t--no-exit-error\tdo not exit on errors\n");
	fprintf(stderr, "\t--test-program\tname of the test program to run\n");
	fprintf(stderr, "\t--full-dumps\tdump the values of all registers on error\n");
	fprintf(stderr, "\t--no-reg-init\tdo not overwrite GDB regs with GLISS values at startup\n");
	}
	
void parse_commandline(int argc, char ** argv)
	{
		
	int longindex;
	char option;
	extern int setenv (const char *name, const char *value, int overwrite);
	struct option longopts[] = {

		/*name                  arg             flag                    val */
		{"version",     0,      NULL,   'V'},
		{"help",        0,      NULL,   'h'},
		{"debug-log", 0, NULL,  'l'},
		{"display-values", 0, NULL, 'v'},
		{"display-replies", 0, NULL, 'r'},
		{"no-exit-error", 0, NULL, 'x'},
		{"test-program", 1, NULL, 'p'},
		{"full-dumps", 0, NULL, 'd'},
		{"no-reg-init", 0, NULL, 'n'},
		{NULL, 0, NULL, 0},
		};

	char *optstring = "Vhrlvxp:dn";
	while ((option =
		getopt_long(argc, argv, optstring, longopts,
				&longindex)) != -1) {

		switch (option) {
			case 'V': printf("\n"); exit (1); break;
			case 'h': usage(argv[0]); exit(1); break;
			case 'l' : do_logging = 1; break;
			case 'v' : display_values = 1; break;
			case 'r' : display_replies = 1; break;
			case 'x': exit_on_errors = 0; break;
			case 'p': sprintf(gpname, "%s", optarg); break;
			case 'd' : display_full_dumps = 1; break;
			case 'n' : init_registers = 0; break;
			case '?': 
				fprintf(stderr, "Unknown option %c\n", optopt);
				exit(5);
			break;

			}
		}
		
	if ( optind < argc )
		{
		printf("Using target at %s\n", argv[optind]);
		sprintf(target_location, "%s", argv[optind]);
		}
	else 
		{
		printf("No commandline parameter given, assuming that target program is running locally using GDB simulator\n");
		sprintf(target_location, "local_gdb\n");
		}
	}
	
void disasm_error_report(char * drive_gdb_reply_buffer, PROC(_state_t) * real_state, PROC(_inst_t) * instr)
	{
	char * reptr;
	while ( (reptr = strstr(drive_gdb_reply_buffer, "},{")) )
		{
		*reptr = ' ';
		*(reptr+1)= '\n';
		*(reptr+2)= ' ';
		}	
	reptr = strstr(drive_gdb_reply_buffer, "[{");
	*reptr = '\n';
	*(reptr + 1) = ' ';
	reptr = drive_gdb_reply_buffer;
	while(*++reptr != '\n');
	reptr++;		
	if( strstr(reptr, "inst") )
		{
		reptr = strstr(reptr, "inst") + 5;
		printf("  GDB disasm: ");
		printf(reptr);	
		}
	char dis[200];
	if ( instr ) 
		{
		PROC(_disasm)(dis, instr);
		printf("GLISS disasm: \"%s\"\n", dis);
		}
	if ( exit_on_errors ) exit(1);		
	}
	
int init_gliss(char * drive_gdb_reply_buffer)
	{
	
	/* make the platform */
	platform = PROC(_new_platform)();
	if(platform == NULL)  {
		fprintf(stderr, "ERROR: no more resources\n");
		exit(2);
	}
	
	/* load the image in the platform */
	if (PROC(_load_platform)(platform, gpname) == -1) {
		fprintf(stderr, "ERROR: cannot load the given executable : %s.\n", gpname);
		exit(2);
	}

	/* make the state depending on the platform */
	real_state = PROC(_new_state)(platform);
	if (real_state == NULL)  {
		fprintf(stderr, "ERROR: no more resources\n");
		exit(2);
	}
	
	/* make the simulator */
	sim = PROC(_new_sim)(real_state, 0, 0);
	if (sim == NULL) {
		fprintf(stderr, "ERROR: no more resources\n");
		exit(2);
	}

//	void *system_list[3];
//	void *loader_list[7];
//	void *mem_list[3];
//	int page_size_system;
//	FILE *verbose_system;
//	int nb_bits=0; /* 0 => par defaut */
//	int nb_mem=0;  /* 0 => par defaut */	
//		
//	page_size_system=4096;
//	verbose_system=NULL;
//		
//	system_list[0]=&page_size_system;
//	system_list[1]=verbose_system;
//	system_list[2]=NULL;
//		
//	char * libs_path[] =
//		{
//		NULL
//		};
//	extern char ** environ;
//		
//	char * gargv = gpname;
//
//	loader_list[0]=NULL; /*denotes the use of the extended parameter list */
//	loader_list[1]=&(gargv);
//	loader_list[2]=environ;            /* environnement */
//	loader_list[3]="../../gel/src"; /* gel plugin path */
//	loader_list[4]=libs_path;
//	loader_list[5]=NULL;
//	loader_list[6]=NULL;
//
//	mem_list[0]=&nb_mem;
//	mem_list[1]=&nb_bits;
//	mem_list[2]=NULL;
		
	send_gdb_cmd("-data-evaluate-expression $sp\n", drive_gdb_reply_buffer, display_replies);
	int *sp;
	read_gdb_output_pc(drive_gdb_reply_buffer, &sp);
//	read_gdb_output_pc(drive_gdb_reply_buffer, &loader_list[6]); //on demande ‡ gliss de charger la pile au mÍme endroit que GDB
	
//	real_state=iss_init(mem_list,loader_list,system_list,NULL,NULL);

	//gliss est stoppÈ au tout dÈbut (_start), gdb est quelques instructions plus loin, donc il faut avancer gliss
	send_gdb_cmd("-data-evaluate-expression $pc\n", drive_gdb_reply_buffer, display_replies);
	read_gdb_output_pc(drive_gdb_reply_buffer, &gdb_pc);
	/* here we assume the next instruction PC is always called NIA */
	gliss_pc = real_state->NIA;
	
	/* set gliss stack pointer with same value as gdb */
	real_state->GPR[1] = sp;
	
	printf("Now stepping GLISS till it is in sync with GDB...");
	int a = 0;
	while ( gliss_pc != gdb_pc )
		{
/*		iss_fetch(NIA(real_state),buff_instr);
                curinstr=iss_decode(real_state,NIA(real_state),buff_instr);
                iss_complete(curinstr,real_state);
                iss_free(curinstr);
		gliss_pc = NIA(real_state);*/
		PROC(_step)(sim);
		gliss_pc = real_state->NIA;
		a++;
		}
	printf("%d steps done\n", a);
		
	//maintenant il faut Ècraser les registres de GDB avec ceux de GLISS
	if ( init_registers ) init_gdb_regs(drive_gdb_reply_buffer);
		
	return 0;
	}

int init_gdb(char * drive_gdb_reply_buffer, char * target)
	{
	/* Initialisation de GDB */
	close(to_gdb_pipe[0]);
	close(from_gdb_pipe[1]);
	from_gdb = fdopen(from_gdb_pipe[0], "r");
	//close(from_gdb_pipe[0]);
	/*Il faut balancer tout le blabla de d√©part et attendre l'invite*/
	while ( ! strstr(drive_gdb_reply_buffer, "(gdb)") )
		{
		memset(drive_gdb_reply_buffer, 0, 1499);
		fgets(drive_gdb_reply_buffer, 1499, from_gdb);
		printf(drive_gdb_reply_buffer);
		}

	if ( strcmp(target, "local_gdb\n") ) //si on ne travaille pas en local
		{
		char finalcmd[200];
		snprintf(finalcmd, 199, "-target-select remote tcp:%s\n", target);
		send_gdb_cmd(finalcmd, drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^connected", IS_FATAL, "When connecting to target, ");
		/*Proc√©dure d√©crite dans la doc : on met un breakpoint sur main, on v√©rifie que gdb est ok*/
		send_gdb_cmd("-break-insert _start\n", drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^done,bkpt={number=", IS_FATAL, "When inserting breakpoint on main, ");
		send_gdb_cmd("-exec-continue\n", drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^running", IS_FATAL, "When trying to run the program, ");
		wait_for_gdb_output(drive_gdb_reply_buffer, display_replies); //on doit attendre le message d'arr√™t au bkpt
		match_gdb_output(drive_gdb_reply_buffer, "*stopped,reason=\"breakpoint-hit\"", IS_FATAL, "When waiting for breakpoint, ");
		}
	else 
		{
		send_gdb_cmd("-target-select sim\n", drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^connected", IS_FATAL, "When connecting to simulator, ");
		send_gdb_cmd("-target-download\n", drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^done", IS_FATAL, "When connecting to simulator, ");
		send_gdb_cmd("-exec-arguments\n", drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^done", IS_FATAL, "When setting arguments list, ");
		/*Proc√©dure d√©crite dans la doc : on met un breakpoint sur main, on v√©rifie que gdb est ok*/
		send_gdb_cmd("-break-insert _start\n", drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^done,bkpt={number=", IS_FATAL, "When inserting breakpoint on main, ");
		/*On lance le programme*/
		send_gdb_cmd("-exec-run > .tmp/test_program_output < .tmp/test_program_input\n", drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^running", IS_FATAL, "When trying to run the program, ");
		wait_for_gdb_output(drive_gdb_reply_buffer, display_replies); //on doit attendre le message d'arr√™t au bkpt
		match_gdb_output(drive_gdb_reply_buffer, "*stopped,reason=\"breakpoint-hit\"", IS_FATAL, "When waiting for breakpoint, ");
		}
	
	create_gdb_vars(drive_gdb_reply_buffer); //gÈnÈrÈ par le script python		
		
	return 0;	
	}
	
	
int main(int argc, char ** argv)
	{
	char drive_gdb_cmd_buffer[150];
	memset(drive_gdb_cmd_buffer, 0, 150);
	char drive_gdb_reply_buffer[1500];
	memset(drive_gdb_reply_buffer, 0, 1500);
	setvbuf(stdout, NULL, _IONBF, 0);
	open_log_file("log");
	parse_commandline(argc, argv);
	if ( ! strlen(gpname) ) sprintf(gpname, ".tmp/test_program");
		
	/* On cr√©e les deux tubes pour communiquer avec gdb */
	if ( pipe(to_gdb_pipe) )
		{
		fprintf(stderr, "FATAL: Couldn't create pipe to communicate with gdb driving process\n");
		exit(-1);
		}
	if ( pipe(from_gdb_pipe) )
		{
		fprintf(stderr, "FATAL: Couldn't create pipe to communicate with gdb driving process\n");
		exit(-1);
		}

	struct stat check_file;
	if ( stat(gpname, &check_file))
		{
		fprintf(stderr, "FATAL: Couldn't find test program %s, please specify one on the commandline (-p)\n", gpname);
		exit(1);
		}
	if ( lstat(".tmp", &check_file) )
		{
		mkdir(".tmp", 0755);
		}
	if ( stat(".tmp/test_program_input", &check_file) || stat(".tmp/test_program_output", &check_file))
		{
		int fdes = open(".tmp/test_program_input", O_CREAT | O_WRONLY);
		close(fdes);
		fdes = open(".tmp/test_program_output", O_CREAT | O_WRONLY);
		close(fdes);
		}

	
	signal(SIGUSR1, catch_sigusr1);
		
		
	
	/* Le fork qui va lancer GDB */
	if ( ! ( drive_gdb_pid = fork () ) )
		{
		drive_gdb();
		}
		
	printf("Initializing GDB\n");
	init_gdb(drive_gdb_reply_buffer, target_location);
		
	printf("Initializing Gliss\n");
	init_gliss(drive_gdb_reply_buffer);
	instr_count = 0;		
		
	/*iss_fetch(NIA(real_state),buff_instr);
	curinstr=iss_decode(real_state,NIA(real_state),buff_instr);*/
	curinstr = PROC(_decode)(sim->decoder, real_state->NIA);
		
	read_vars_this_instruction(drive_gdb_reply_buffer);
	compare_regs_this_instruction(drive_gdb_reply_buffer, real_state, curinstr, instr_count);
	free(curinstr);
		
	printf("\n\033[1mREADY\033[0m - starting step by step execution\n");
	while ( 1 ) 
		{
		instr_count++;
			
		/* GDB step */
		send_gdb_cmd("-data-evaluate-expression $pc\n", drive_gdb_reply_buffer, display_replies);
		read_gdb_output_pc(drive_gdb_reply_buffer, &gdb_pc);
		send_gdb_cmd("-exec-step-instruction\n", drive_gdb_reply_buffer, display_replies);
		match_gdb_output(drive_gdb_reply_buffer, "^running", IS_FATAL, "When trying to advance of one step, ");
		wait_for_gdb_output(drive_gdb_reply_buffer, 0);
		if ( ! match_gdb_output(drive_gdb_reply_buffer, "*stopped,reason=\"exited", IS_HANDLED_ELSEWHERE, NULL))
			{
			printf("Program %s\n", drive_gdb_reply_buffer + 1 );
			break;
			}
		
		match_gdb_output(drive_gdb_reply_buffer, "*stopped,reason=\"end-stepping-range\"", IS_FATAL, "When trying to advance of one step, ");
		
		send_gdb_cmd("-var-update *\n", drive_gdb_reply_buffer, display_replies);
		//pour l'instant on se moque un peu de la valeur de retour
		match_gdb_output(drive_gdb_reply_buffer, "^done,", IS_FATAL, "When updating register variables, ");
				
		/* GLISS step */
		gliss_pc = real_state->NIA;
		if (display_values) printf("PC gdb %d gliss %d\n", gdb_pc, gliss_pc);
		if (gliss_pc != gdb_pc)
			{
			fprintf(stderr, "Gliss and GDB PCs differ : gdb 0x%08x gliss 0x%08x\n", gdb_pc, gliss_pc);
			fprintf(stdout, "Assembly follows\n");
			send_gdb_cmd("-data-disassemble -s \"$pc - 20\" -e \"$pc+12\" -- 0\n", drive_gdb_reply_buffer, 0);
			disasm_error_report(drive_gdb_reply_buffer, NULL, NULL);
			exit(1);
			}
		/*iss_fetch(NIA(real_state),buff_instr);
                curinstr=iss_decode(real_state,NIA(real_state),buff_instr);
                iss_complete(curinstr,real_state);*/
		curinstr = PROC(_decode)(sim->decoder, real_state->NIA);
		PROC(_step)(sim);
		
		read_vars_this_instruction(drive_gdb_reply_buffer);
		compare_regs_this_instruction(drive_gdb_reply_buffer, real_state, curinstr, instr_count);
			
                free(curinstr);	
			
		
		}
		
		
		
	/*struct rusage statistiques;
	getrusage(RUSAGE_SELF, & statistiques);
	printf("User time : %ld s %ld us\n", statistiques.ru_utime.tv_sec, statistiques.ru_utime.tv_usec);
	printf("Sys time : %ld s %ld us\n", statistiques.ru_stime.tv_sec, statistiques.ru_stime.tv_usec);*/
	send_gdb_cmd("-gdb-exit\n", drive_gdb_reply_buffer, display_replies);
	fd_set tmp_set;
	FD_ZERO(&tmp_set);
	FD_SET(from_gdb_pipe[0], &tmp_set);
	struct timeval tv;
	tv.tv_sec = 0;
	tv.tv_usec = 0;
	while ( select(from_gdb_pipe[0] + 3, &tmp_set, NULL, NULL, &tv) )
		{
		FD_SET(from_gdb_pipe[0], &tmp_set);
		memset(drive_gdb_reply_buffer, 0, 1499);
		if( fgets(drive_gdb_reply_buffer, 10, from_gdb) == NULL ) break;
		printf("%s", drive_gdb_reply_buffer);
		}

	close_log_file();
	fclose(from_gdb);
	close(to_gdb_pipe[1]);
	printf("%d instructions\n", instr_count);
	return 0;
	}

void drive_gdb()
	{
	char *gdb_argv[] = {"gdb", "--interpreter", "mi",  "--quiet", gpname, NULL};
	setvbuf(stdout, NULL, _IONBF, 0);
	setvbuf(stdin, NULL, _IONBF, 0);
	close(to_gdb_pipe[1]);
	
	/*GDB n'a pas besoin du logfile */
	close_log_file();
	
	/* On redirige l'entr√©e de GDB sur le pipe */
	if ( dup2(to_gdb_pipe[0], STDIN_FILENO) == -1 )
		{
		fprintf(stderr, "FATAL: Couldn't dup2() GDB pipe to stdin\n");
		exit(-1);
		}
	close(to_gdb_pipe[0]);

	
	close(from_gdb_pipe[0]);
	/* On redirige la sortie de GDB sur le pipe aussi */
	if ( dup2(from_gdb_pipe[1], STDOUT_FILENO) == -1 )
		{
		fprintf(stderr, "FATAL: Couldn't dup2() GDB pipe to stdout\n");
		exit(-1);
		}
	close(from_gdb_pipe[1]);


	/*Maintenant on peut lancer GDB en mode MI */
	printf("execvp(%s)", GDB_NAME);
	if ( execvp(GDB_NAME, gdb_argv) ) //si execvp retourne alors ouch
		{
		perror("GDB execvp()");
		exit(-1);
		}
	}


#undef MAIN_C
	
