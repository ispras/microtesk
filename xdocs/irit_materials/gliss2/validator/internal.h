/*********************
Gliss CPU simulator validator
internal.h : internal definitions
**********************/


//main.c
#undef EXTERN
#ifdef MAIN_C
#define EXTERN 
#else
#define EXTERN extern
#endif



EXTERN int to_gdb_pipe[2]; //les descripteurs du tube d'interface avec le process qui s'occupe de gdb
EXTERN int from_gdb_pipe[2];
EXTERN FILE * from_gdb;
EXTERN int64_t *gdb_regvals;
EXTERN int gdb_pc;
EXTERN int gliss_pc;

EXTERN int display_values;
EXTERN int display_replies;
EXTERN int display_full_dumps;
EXTERN PROC(_state_t) * real_state;
/*EXTERN code_t buff_instr[20];*/
EXTERN PROC(_inst_t) *curinstr;
EXTERN PROC(_platform_t) *platform;
EXTERN PROC(_sim_t) *sim;

#undef EXTERN

//gdb_interface.c
#ifdef GDB_INTERFACE_C
#define EXTERN 
#else
#define EXTERN extern
#endif

EXTERN void send_gdb_cmd(char * cmd, char * replybuf, int printreply); //envoi synchrone d'une commande à gdb (attente d'une réponse)
EXTERN int wait_for_gdb_output(char * replybuf, int printreply); //attente d'un message de gdb

#define IS_FATAL 1
#define IS_WARNING 2
#define IS_HANDLED_ELSEWHERE 3 //éventuelle erreur gérée directement par l'appelante
EXTERN int match_gdb_output(char * replybuf, char * pattern, int is_fatal, char * error_label); //regexp pour vérifier la sortie de gdb
EXTERN int read_gdb_output_pc(char * replybuf, int * pc);

EXTERN void read_gdb_output_register_value_lli(char * replybuf, long long int * regval);
EXTERN void read_gdb_output_register_value_li(char * replybuf, long int * regval);
EXTERN void read_gdb_output_register_value_lf(char * replybuf, double * regval);
EXTERN void read_gdb_output_register_value_f(char * replybuf, float * regval);
EXTERN void read_gdb_output_register_value_llu(char * replybuf, unsigned long long int * regval);
EXTERN void read_gdb_output_register_value_lu(char * replybuf,  unsigned long int * regval);
#undef EXTERN

//log.c
#ifdef LOG_C
#define EXTERN 
#else
#define EXTERN extern
#endif

EXTERN FILE * logfile;
EXTERN int open_log_file(char * fpath);
EXTERN void close_log_file();
EXTERN void log_msg(char * fmt, ...);

EXTERN int do_logging;
#undef EXTERN
