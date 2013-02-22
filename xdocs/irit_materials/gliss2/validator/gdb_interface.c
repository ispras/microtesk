/*********************
Gliss CPU simulator validator
gdb_interface.c : gdb-related functions
**********************/

#define GDB_INTERFACE_C
#include "all_inc.h"

void send_gdb_cmd(char * cmd, char * replybuf, int printreply)
	{
	if(cmd[strlen(cmd)-1] != '\n') fprintf(stderr, "Warning: no newline at end of command %s\n", cmd);
	write(to_gdb_pipe[1], cmd, strlen(cmd));
	log_msg("Sent %s\n", cmd);
	wait_for_gdb_output(replybuf, printreply);
	}
	
int wait_for_gdb_output(char * replybuf, int printreply)
	{
	fgets(replybuf, 1499, from_gdb);
	log_msg("Got %s\n", replybuf);
	if(*replybuf=='(' && *(replybuf+1) =='g')  //GDB a cette sale habitude de rajouter son invite partout. Nous allons nous en passer.
		{ //copy paste instead of recursion for performance reasons
		fgets(replybuf, 1499, from_gdb);
		log_msg("Got %s\n", replybuf);
		if(*replybuf=='(' && *(replybuf+1) =='g')  //GDB a cette sale habitude de rajouter son invite partout. Nous allons nous en passer.
			{
			wait_for_gdb_output(replybuf, 0);
			}
		if ( *replybuf != '*' && *replybuf != '^') //alors on a rien de particulier, on crache tout
			{
			printf("%s", replybuf);
			wait_for_gdb_output(replybuf, printreply);
			}
		}
	if ( *replybuf != '*' && *replybuf != '^') //alors on a rien de particulier, on crache tout
		{
		printf("%s", replybuf);
		wait_for_gdb_output(replybuf, printreply);
		}
	else
		if ( printreply )
			{
			printf("%s", replybuf);
			}
	return 0;
	}

int match_gdb_output(char * replybuf, char * pattern, int is_fatal, char * error_label)
	{
	if ( strstr(replybuf, "^error") || !strstr(replybuf, pattern))
		{
		switch(is_fatal)
			{
			case IS_FATAL: 
				fprintf(stderr, "FATAL: %s: ", error_label);
				fprintf(stderr, "%s\n", replybuf);
				exit(1);
				break;
			case IS_WARNING:
				fprintf(stderr, "WARNING: %s: ", error_label);
				fprintf(stderr, "%s\n I will attempt to continue.", replybuf);
				return 1;
				break;
			case IS_HANDLED_ELSEWHERE: 
				return 1;
				break;
			}
		}
	return 0;
	}

/*	
int read_gdb_output_register_value(char * replybuf, int64_t * regval)
	{
	char strval[30];
	if ( ! strstr(replybuf, "^done,value=\""))
		{
		fprintf(stderr, "FATAL: Unable to read register value: ");
		fprintf(stderr, "%s\n", replybuf);
		exit(1);
		}
	char * runptr = replybuf+13; //on saute sur le premier chiffre
	char *endptr;
	endptr = runptr;
	while ( isdigit(*endptr) ) endptr ++;
	strncpy(strval, runptr, endptr - runptr);
	strval[endptr-runptr] = '\0';
	*regval = atoll(strval);
	return 0;
	}
	*/
void read_gdb_output_register_value_lli(char * replybuf, long long int * regval)
	{
	char strval[30];
	if ( *replybuf != '^' || *(replybuf+1) != 'd')
		{
		fprintf(stderr, "FATAL: Unable to read register value: ");
		fprintf(stderr, "%s\n", replybuf);
		exit(1);
		}
	char * runptr = replybuf+13; //on saute sur le premier chiffre
	char *endptr;
	endptr = runptr;
	while ( isdigit(*endptr) ) endptr ++;
	strncpy(strval, runptr, endptr - runptr);
	strval[endptr-runptr] = '\0';
	*regval = atoll(strval);
	}
	
void read_gdb_output_register_value_li(char * replybuf, long int * regval)
	{
	long long int regval2;
	read_gdb_output_register_value_lli(replybuf, &regval2);
	*regval = (long int) regval2;
	}
	
void read_gdb_output_register_value_lf(char * replybuf, double * regval)
	{
	char strval[30];
	if ( *replybuf != '^' || *(replybuf+1) != 'd')
		{
		fprintf(stderr, "FATAL: Unable to read register value: ");
		fprintf(stderr, "%s\n", replybuf);
		exit(1);
		}
	char * runptr = replybuf+13; //on saute sur le premier chiffre
	char *endptr;
	endptr = runptr;
	while ( (*endptr) != '\"') endptr ++;
	strncpy(strval, runptr, endptr - runptr);
	strval[endptr-runptr] = '\0';
	*regval = atof(strval);
	}

void read_gdb_output_register_value_f(char * replybuf, float * regval)
	{
	double regval2;
	read_gdb_output_register_value_lf(replybuf, &regval2);
	*regval = (float) regval2;
	}
	
void read_gdb_output_register_value_llu(char * replybuf, unsigned long long int * regval)
	{
		char strval[30];
	if ( *replybuf != '^' || *(replybuf+1) != 'd')
		{
		fprintf(stderr, "FATAL: Unable to read register value: ");
		fprintf(stderr, "%s\n", replybuf);
		exit(1);
		}
	char * runptr = replybuf+13; //on saute sur le premier chiffre
	char *endptr;
	endptr = runptr;
	while ( (*endptr) != '\"') endptr ++;
	strncpy(strval, runptr, endptr - runptr);
	strval[endptr-runptr] = '\0';
	*regval = strtoull(strval, NULL, 10);
	}
	
void read_gdb_output_register_value_lu(char * replybuf,  unsigned long int * regval)
	{
		char strval[30];
	if ( *replybuf != '^' || *(replybuf+1) != 'd')
		{
		fprintf(stderr, "FATAL: Unable to read register value: ");
		fprintf(stderr, "%s\n", replybuf);
		exit(1);
		}
	char * runptr = replybuf+13; //on saute sur le premier chiffre
	char *endptr;
	endptr = runptr;
	while ( (*endptr) != '\"') endptr ++;
	strncpy(strval, runptr, endptr - runptr);
	strval[endptr-runptr] = '\0';
	*regval = strtoul(strval, NULL, 10);
	}
	
int read_gdb_output_pc(char * replybuf, int * pc)
	{
	char strval[30];
	if ( *replybuf != '^' || *(replybuf+1) != 'd')
		{
		fprintf(stderr, "FATAL: Unable to read register value: ");
		fprintf(stderr, "%s\n", replybuf);
		exit(1);
		}
	char * runptr = replybuf + 13;
	char *endptr;
	endptr = runptr;
	while ( isxdigit(*endptr) ) endptr ++;
	strncpy(strval, runptr, endptr - runptr);
	strval[endptr-runptr] = '\0';
	/* si la valeur est reportée en décimal */
	*pc = strtoll(strval, NULL, 10);
	/* sinon
	*pc = strtoll(strval, NULL, 16);
	*/
	return 0;
	}
#undef GDB_INTERFACE_C
