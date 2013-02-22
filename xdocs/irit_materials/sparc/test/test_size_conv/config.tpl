#define TARGET_ENDIANNESS big
#define HOST_ENDIANNESS little

/* WARNING: unsure */
#define TOTO_SYSCALL_CODE(i, s) ((s)->R[0])
#define TOTO_SYSCALL_MEM(s) ((s)->M)


#define TOTO_SYSPARM_REG32_RCNT 4
#define TOTO_SYSPARM_REG32_REG(s, i) 	((s)->R[i])
#define TOTO_SYSPARM_REG32_SP(s) 	((s)->R[3])

#define TOTO_SYSPARM_REG32_RETURN(s, v)	{ (s)->R[0] = (v); }
#define TOTO_SYSPARM_REG32_SUCCEED(s)	{ (s)->R[1] = 0; }
#define TOTO_SYSPARM_REG32_FAILED(s)	{ (s)->R[1] = 0; }
