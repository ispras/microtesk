/*********************
Gliss CPU simulator validator
internal.h : internal definitions
**********************/

#ifndef INTERNAL_H
#define INTERNAL_H

#include <stdio.h>
#include <stdint.h>
#include GLISS_API_H

//main.c

extern int to_gdb_pipe[2]; //les descripteurs du tube d'interface avec le process qui s'occupe de gdb
extern int from_gdb_pipe[2];
extern FILE * from_gdb;
extern uint32_t gdb_pc;
extern uint32_t gdb_npc;
extern uint32_t gliss_pc;
extern uint32_t gliss_npc;

extern int display_values;
extern int display_replies;
extern int display_full_dumps;
extern PROC(_state_t) * real_state;
extern PROC(_inst_t) *curinstr;
extern PROC(_platform_t) *platform;
extern PROC(_sim_t) *sim;

extern void disasm_error_report(char * drive_gdb_reply_buffer, PROC(_state_t) * state, PROC(_inst_t) * instr, int cpt, int do_exit);


//gdb_interface.c

void send_gdb_cmd(char * cmd, char * replybuf, int printreply); //envoi synchrone d'une commande à gdb (attente d'une réponse)
int wait_for_gdb_output(char * replybuf, int printreply); //attente d'un message de gdb

#define IS_ERROR 1
#define IS_WARNING 2
#define IS_HANDLED_ELSEWHERE 3 // éventuelle erreur gérée directement par l'appelante
int match_gdb_output(char * replybuf, char * pattern, int is_fatal, char * error_label); //regexp pour vérifier la sortie de gdb
void read_gdb_output_pc(char * replybuf, uint32_t * pc);

void read_gdb_output_register_value_8(char * replybuf, uint8_t * regval);
void read_gdb_output_register_value_16(char * replybuf, uint16_t * regval);
void read_gdb_output_register_value_u32(char * replybuf, uint32_t * regval);
void read_gdb_output_register_value_u64(char * replybuf, uint64_t * regval);
void read_gdb_output_register_value_i32(char * replybuf, int32_t * regval);
void read_gdb_output_register_value_i64(char * replybuf, int64_t * regval);
void read_gdb_output_register_value_f32(char * replybuf, float * regval);
void read_gdb_output_register_value_f64(char * replybuf, double * regval);


//log.c

extern FILE * logfile;
int open_log_file(char * fpath);
void close_log_file();
void log_msg(char * fmt, ...);

extern int do_logging;


#endif /* INTERNAL_H */
