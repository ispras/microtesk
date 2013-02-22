/*
 *	syscall-linux module implementation
 *
 *	This file is part of GLISS V2
 *	Copyright (c) 2009, IRIT UPS.
 *
 *	GLISS is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	GLISS is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with OTAWA; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

#ifdef linux
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif
#endif

#include <assert.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/times.h>

#include <gliss/mem.h>
#include <gliss/sysparm.h>
#include <gliss/syscall.h>
#include <gliss/config.h>
#include "platform.h"

/* default page size */
#ifndef GLISS_PAGE_SIZE
#	define GLISS_PAGE_SIZE	4096
#endif

/* for access to gliss_env_t (system environment) */
#include <gliss/loader.h>

/* booleans */
typedef int BOOL;
#define FALSE 0
#define TRUE  1

/* global */
static FILE *verbose = NULL;

#define RETURN(x)	gliss_sysparm_return(state, x)
#define RESET_CR0SO	gliss_sysparm_succeed(state)
#define SET_CR0SO	gliss_sysparm_failed(state)
#define MEM_READ(buf, addr, size) gliss_mem_read(GLISS_SYSCALL_MEM(state), (addr), (buf), (size))
#define MEM_WRITE(addr, buf, size) gliss_mem_write(GLISS_SYSCALL_MEM(state), (addr), (buf), (size))
#define PARM_BEGIN	{ gliss_sysparm_t parm; gliss_sysparm_init(parm, state);
#define PARM(i)			gliss_sysparm_pop32(parm, state)
#define PARM_END	gliss_sysparm_destroy(parm, state); }
#define STRLEN(addr) my_strlen(state, addr)
#define MEM_WRITE_DWORD(a, v) gliss_mem_write64(GLISS_SYSCALL_MEM(state), (a), (v))

#define __SYSCALL_exit		  1
#define __SYSCALL_fork		  2
#define __SYSCALL_read		  3
#define __SYSCALL_write		  4
#define __SYSCALL_open		  5
#define __SYSCALL_close		  6
#define __SYSCALL_waitpid		  7
#define __SYSCALL_creat		  8
#define __SYSCALL_link		  9
#define __SYSCALL_unlink		 10
#define __SYSCALL_execve		 11
#define __SYSCALL_chdir		 12
#define __SYSCALL_time		 13
#define __SYSCALL_mknod		 14
#define __SYSCALL_chmod		 15
#define __SYSCALL_lchown		 16
#define __SYSCALL_break		 17
#define __SYSCALL_oldstat		 18
#define __SYSCALL_lseek		 19
#define __SYSCALL_getpid		 20
#define __SYSCALL_mount		 21
#define __SYSCALL_umount		 22
#define __SYSCALL_setuid		 23
#define __SYSCALL_getuid		 24
#define __SYSCALL_stime		 25
#define __SYSCALL_ptrace		 26
#define __SYSCALL_alarm		 27
#define __SYSCALL_oldfstat		 28
#define __SYSCALL_pause		 29
#define __SYSCALL_utime		 30
#define __SYSCALL_stty		 31
#define __SYSCALL_gtty		 32
#define __SYSCALL_access		 33
#define __SYSCALL_nice		 34
#define __SYSCALL_ftime		 35
#define __SYSCALL_sync		 36
#define __SYSCALL_kill		 37
#define __SYSCALL_rename		 38
#define __SYSCALL_mkdir		 39
#define __SYSCALL_rmdir		 40
#define __SYSCALL_dup		 41
#define __SYSCALL_pipe		 42
#define __SYSCALL_times		 43
#define __SYSCALL_prof		 44
#define __SYSCALL_brk		 45
#define __SYSCALL_setgid		 46
#define __SYSCALL_getgid		 47
#define __SYSCALL_signal		 48
#define __SYSCALL_geteuid		 49
#define __SYSCALL_getegid		 50
#define __SYSCALL_acct		 51
#define __SYSCALL_umount2		 52
#define __SYSCALL_lock		 53
#define __SYSCALL_ioctl		 54
#define __SYSCALL_fcntl		 55
#define __SYSCALL_mpx		 56
#define __SYSCALL_setpgid		 57
#define __SYSCALL_ulimit		 58
#define __SYSCALL_oldolduname	 59
#define __SYSCALL_umask		 60
#define __SYSCALL_chroot		 61
#define __SYSCALL_ustat		 62
#define __SYSCALL_dup2		 63
#define __SYSCALL_getppid		 64
#define __SYSCALL_getpgrp		 65
#define __SYSCALL_setsid		 66
#define __SYSCALL_sigaction		 67
#define __SYSCALL_sgetmask		 68
#define __SYSCALL_ssetmask		 69
#define __SYSCALL_setreuid		 70
#define __SYSCALL_setregid		 71
#define __SYSCALL_sigsuspend		 72
#define __SYSCALL_sigpending		 73
#define __SYSCALL_sethostname	 74
#define __SYSCALL_setrlimit		 75
#define __SYSCALL_getrlimit		 76
#define __SYSCALL_getrusage		 77
#define __SYSCALL_gettimeofday	 78
#define __SYSCALL_settimeofday	 79
#define __SYSCALL_getgroups		 80
#define __SYSCALL_setgroups		 81
#define __SYSCALL_select		 82
#define __SYSCALL_symlink		 83
#define __SYSCALL_oldlstat		 84
#define __SYSCALL_readlink		 85
#define __SYSCALL_uselib		 86
#define __SYSCALL_swapon		 87
#define __SYSCALL_reboot		 88
#define __SYSCALL_readdir		 89
#define __SYSCALL_mmap		 90
#define __SYSCALL_munmap		 91
#define __SYSCALL_truncate		 92
#define __SYSCALL_ftruncate		 93
#define __SYSCALL_fchmod		 94
#define __SYSCALL_fchown		 95
#define __SYSCALL_getpriority	 96
#define __SYSCALL_setpriority	 97
#define __SYSCALL_profil		 98
#define __SYSCALL_statfs		 99
#define __SYSCALL_fstatfs		100
#define __SYSCALL_ioperm		101
#define __SYSCALL_socketcall		102
#define __SYSCALL_syslog		103
#define __SYSCALL_setitimer		104
#define __SYSCALL_getitimer		105
#define __SYSCALL_stat		106
#define __SYSCALL_lstat		107
#define __SYSCALL_fstat		108
#define __SYSCALL_olduname		109
#define __SYSCALL_iopl		110
#define __SYSCALL_vhangup		111
#define __SYSCALL_idle		112
#define __SYSCALL_vm86old		113
#define __SYSCALL_wait4		114
#define __SYSCALL_swapoff		115
#define __SYSCALL_sysinfo		116
#define __SYSCALL_ipc		117
#define __SYSCALL_fsync		118
#define __SYSCALL_sigreturn		119
#define __SYSCALL_clone		120
#define __SYSCALL_setdomainname	121
#define __SYSCALL_uname		122
#define __SYSCALL_modify_ldt		123
#define __SYSCALL_adjtimex		124
#define __SYSCALL_mprotect		125
#define __SYSCALL_sigprocmask	126
#define __SYSCALL_create_module	127
#define __SYSCALL_init_module	128
#define __SYSCALL_delete_module	129
#define __SYSCALL_get_kernel_syms	130
#define __SYSCALL_quotactl		131
#define __SYSCALL_getpgid		132
#define __SYSCALL_fchdir		133
#define __SYSCALL_bdflush		134
#define __SYSCALL_sysfs		135
#define __SYSCALL_personality	136
#define __SYSCALL_afs_syscall	137
#define __SYSCALL_setfsuid		138
#define __SYSCALL_setfsgid		139
#define __SYSCALL__llseek		140
#define __SYSCALL_getdents		141
#define __SYSCALL__newselect		142
#define __SYSCALL_flock		143
#define __SYSCALL_msync		144
#define __SYSCALL_readv		145
#define __SYSCALL_writev		146
#define __SYSCALL_getsid		147
#define __SYSCALL_fdatasync		148
#define __SYSCALL__sysctl		149
#define __SYSCALL_mlock		150
#define __SYSCALL_munlock		151
#define __SYSCALL_mlockall		152
#define __SYSCALL_munlockall		153
#define __SYSCALL_sched_setparam		154
#define __SYSCALL_sched_getparam		155
#define __SYSCALL_sched_setscheduler		156
#define __SYSCALL_sched_getscheduler		157
#define __SYSCALL_sched_yield		158
#define __SYSCALL_sched_get_priority_max	159
#define __SYSCALL_sched_get_priority_min	160
#define __SYSCALL_sched_rr_get_interval	161
#define __SYSCALL_nanosleep		162
#define __SYSCALL_mremap		163
#define __SYSCALL_setresuid		164
#define __SYSCALL_getresuid		165
#define __SYSCALL_vm86		166
#define __SYSCALL_query_module	167
#define __SYSCALL_poll		168
#define __SYSCALL_nfsservctl		169
#define __SYSCALL_setresgid		170
#define __SYSCALL_getresgid		171
#define __SYSCALL_prctl              172
#define __SYSCALL_rt_sigreturn	173
#define __SYSCALL_rt_sigaction	174
#define __SYSCALL_rt_sigprocmask	175
#define __SYSCALL_rt_sigpending	176
#define __SYSCALL_rt_sigtimedwait	177
#define __SYSCALL_rt_sigqueueinfo	178
#define __SYSCALL_rt_sigsuspend	179
#define __SYSCALL_pread		180
#define __SYSCALL_pwrite		181
#define __SYSCALL_chown		182
#define __SYSCALL_getcwd		183
#define __SYSCALL_capget		184
#define __SYSCALL_capset		185
#define __SYSCALL_sigaltstack	186
#define __SYSCALL_sendfile		187
#define __SYSCALL_getpmsg		188
#define __SYSCALL_putpmsg		189
#define __SYSCALL_vfork		190
#define __SYSCALL_ugetrlimit		191
#define __SYSCALL_mmap2		192
#define __SYSCALL_truncate64		193
#define __SYSCALL_ftruncate64	194
#define __SYSCALL_stat64		195
#define __SYSCALL_lstat64		196
#define __SYSCALL_fstat64		197
#define __SYSCALL_lchown32		198
#define __SYSCALL_getuid32		199
#define __SYSCALL_getgid32		200
#define __SYSCALL_geteuid32		201
#define __SYSCALL_getegid32		202
#define __SYSCALL_setreuid32		203
#define __SYSCALL_setregid32		204
#define __SYSCALL_getgroups32	205
#define __SYSCALL_setgroups32	206
#define __SYSCALL_fchown32		207
#define __SYSCALL_setresuid32	208
#define __SYSCALL_getresuid32	209
#define __SYSCALL_setresgid32	210
#define __SYSCALL_getresgid32	211
#define __SYSCALL_chown32		212
#define __SYSCALL_setuid32		213
#define __SYSCALL_setgid32		214
#define __SYSCALL_setfsuid32		215
#define __SYSCALL_setfsgid32		216
#define __SYSCALL_pivot_root		217
#define __SYSCALL_mincore		218
#define __SYSCALL_madvise		219
#define __SYSCALL_getdents64		220
#define __SYSCALL_fcntl64		221

/* FD match table */

/**
 * Find a new simulation FD with the given system FD.
 * @param pf	Platform.
 * @param sfd	System FD.
 * @return		Simulation FD.
 */
static int fd_new(gliss_platform_t *pf, int sfd) {
	int i;
	if(sfd < 0)
		return -1;
	for(i = 0; i < GLISS_FD_COUNT; i++)
		if(pf->fds[i] == -1) {
			pf->fds[i] = sfd;
			return i;
		}
	fprintf(stderr, "ERROR: system.c: no more free fd.\n");
	exit(1);
}

/**
 * Free a used FD.
 * @param pf	Platform.
 * @param fd	FD to delete.
 */
static void fd_delete(gliss_platform_t *pf, int fd) {
	assert(fd >= 0 && fd < GLISS_FD_COUNT);
	pf->fds[fd] = -1;
}


/**
 * Convert a simulated FD to a system FD.
 * @param pf	Platform.
 */
static int _fd(gliss_platform_t *pf, int fd) {
	assert(fd >= 0 && fd < GLISS_FD_COUNT);
	return pf->fds[fd];
}

/**
 * Initialize the FD translation system.
 */
void gliss_syscall_init(gliss_platform_t *pf) {
	int i;

	/* FD init */
	for(i = 0; i < GLISS_FD_COUNT; i++)
		pf->fds[i] = -1;
	fd_new(pf, dup(0));
	fd_new(pf, dup(1));
	fd_new(pf, dup(2));

	/* BRK base init */
	pf->brk_base = 0;

	/* running init */
	pf->running = FALSE;
}

/**
 * Stop the FD translation system.
 */
void gliss_syscall_destroy(gliss_platform_t *pf) {
	int i;

	/* destroy FDS */
	for(i = 0; i < GLISS_FD_COUNT; i++)
		if(pf->fds[i] != -1)
			close(pf->fds[i]);
}


// some global variables for syscall
static BOOL swap = FALSE;

static int my_strlen(gliss_state_t *state, gliss_address_t addr)
{
	int len = 0;
	char buffer[32];

	while(1) {
		int size = sizeof(buffer);
		char *p = buffer;
		MEM_READ(buffer, addr, size);
		while(size > 0) {
			if(*p == 0) return len;
			len++;
			size--;
			p++;
		}
		addr += sizeof(buffer);
	}
}


static char *gliss_get_syscall_name(int num)
{
	switch(num)
	{
		case __SYSCALL_exit: return "exit";
		case __SYSCALL_fork: return "fork";
		case __SYSCALL_read: return "read";
		case __SYSCALL_write: return "write";
		case __SYSCALL_open: return "open";
		case __SYSCALL_close: return "close";
		case __SYSCALL_waitpid: return "waitpid";
		case __SYSCALL_creat: return "creat";
		case __SYSCALL_link: return "link";
		case __SYSCALL_unlink: return "unlink";
		case __SYSCALL_execve: return "execve";
		case __SYSCALL_chdir: return "chdir";
		case __SYSCALL_time: return "time";
		case __SYSCALL_mknod: return "mknod";
		case __SYSCALL_chmod: return "chmod";
		case __SYSCALL_lchown: return "lchown";
		case __SYSCALL_break: return "break";
		case __SYSCALL_oldstat: return "oldstat";
		case __SYSCALL_lseek: return "lseek";
		case __SYSCALL_getpid: return "getpid";
		case __SYSCALL_mount: return "mount";
		case __SYSCALL_umount: return "umount";
		case __SYSCALL_setuid: return "setuid";
		case __SYSCALL_getuid: return "getuid";
		case __SYSCALL_stime: return "stime";
		case __SYSCALL_ptrace: return "ptrace";
		case __SYSCALL_alarm: return "alarm";
		case __SYSCALL_oldfstat: return "oldfstat";
		case __SYSCALL_pause: return "pause";
		case __SYSCALL_utime: return "utime";
		case __SYSCALL_stty: return "stty";
		case __SYSCALL_gtty: return "gtty";
		case __SYSCALL_access: return "access";
		case __SYSCALL_nice: return "nice";
		case __SYSCALL_ftime: return "ftime";
		case __SYSCALL_sync: return "sync";
		case __SYSCALL_kill: return "kill";
		case __SYSCALL_rename: return "rename";
		case __SYSCALL_mkdir: return "mkdir";
		case __SYSCALL_rmdir: return "rmdir";
		case __SYSCALL_dup: return "dup";
		case __SYSCALL_pipe: return "pipe";
		case __SYSCALL_times: return "times";
		case __SYSCALL_prof: return "prof";
		case __SYSCALL_brk: return "brk";
		case __SYSCALL_setgid: return "setgid";
		case __SYSCALL_getgid: return "getgid";
		case __SYSCALL_signal: return "signal";
		case __SYSCALL_geteuid: return "geteuid";
		case __SYSCALL_getegid: return "getegid";
		case __SYSCALL_acct: return "acct";
		case __SYSCALL_umount2: return "umount2";
		case __SYSCALL_lock: return "lock";
		case __SYSCALL_ioctl: return "ioctl";
		case __SYSCALL_fcntl: return "fcntl";
		case __SYSCALL_mpx: return "mpx";
		case __SYSCALL_setpgid: return "setpgid";
		case __SYSCALL_ulimit: return "ulimit";
		case __SYSCALL_oldolduname: return "oldolduname";
		case __SYSCALL_umask: return "umask";
		case __SYSCALL_chroot: return "chroot";
		case __SYSCALL_ustat	: return "ustat";
		case __SYSCALL_dup2: return "dup2";
		case __SYSCALL_getppid: return "getppid";
		case __SYSCALL_getpgrp: return "getpgrp";
		case __SYSCALL_setsid: return "setsid";
		case __SYSCALL_sigaction: return "sigaction";
		case __SYSCALL_sgetmask: return "sgetmask";
		case __SYSCALL_ssetmask: return "ssetmask";
		case __SYSCALL_setreuid: return "setreuid";
		case __SYSCALL_setregid: return "setregid";
		case __SYSCALL_sigsuspend: return "sigsuspend";
		case __SYSCALL_sigpending: return "sigpending";
		case __SYSCALL_sethostname: return "sethostname";
		case __SYSCALL_setrlimit: return "setrlimit";
		case __SYSCALL_getrlimit: return "getrlimit";
		case __SYSCALL_getrusage: return "getrusage";
		case __SYSCALL_gettimeofday: return "gettimeofday";
		case __SYSCALL_settimeofday: return "settimeofday";
		case __SYSCALL_getgroups: return "getgroups";
		case __SYSCALL_setgroups: return "setgroups";
		case __SYSCALL_select: return "select";
		case __SYSCALL_symlink: return "symlink";
		case __SYSCALL_oldlstat: return "oldlstat";
		case __SYSCALL_readlink: return "readlink";
		case __SYSCALL_uselib: return "uselib";
		case __SYSCALL_swapon: return "swapon";
		case __SYSCALL_reboot: return "reboot";
		case __SYSCALL_readdir: return "readdir";
		case __SYSCALL_mmap: return "mmap";
		case __SYSCALL_munmap: return "munmap";
		case __SYSCALL_truncate: return "truncate";
		case __SYSCALL_ftruncate: return "ftruncate";
		case __SYSCALL_fchmod: return "fchmod";
		case __SYSCALL_fchown: return "fchown";
		case __SYSCALL_getpriority: return "getpriority";
		case __SYSCALL_setpriority: return "setpriority";
		case __SYSCALL_profil: return "profil";
		case __SYSCALL_statfs: return "statfs";
		case __SYSCALL_fstatfs: return "fstatfs";
		case __SYSCALL_ioperm: return "ioperm";
		case __SYSCALL_socketcall: return "socketcall";
		case __SYSCALL_syslog: return "syslog";
		case __SYSCALL_setitimer: return "setitimer";
		case __SYSCALL_getitimer: return "getitimer";
		case __SYSCALL_stat: return "stat";
		case __SYSCALL_lstat: return "lstat";
		case __SYSCALL_fstat: return "fstat";
		case __SYSCALL_olduname: return "olduname";
		case __SYSCALL_iopl: return "iopl";
		case __SYSCALL_vhangup: return "vhangup";
		case __SYSCALL_idle: return "idle";
		case __SYSCALL_vm86old: return "vm86old";
		case __SYSCALL_wait4: return "wait4";
		case __SYSCALL_swapoff: return "swapoff";
		case __SYSCALL_sysinfo: return "sysinfo";
		case __SYSCALL_ipc: return "ipc";
		case __SYSCALL_fsync: return "fsync";
		case __SYSCALL_sigreturn: return "sigreturn";
		case __SYSCALL_clone: return "clone";
		case __SYSCALL_setdomainname: return "setdomainname";
		case __SYSCALL_uname: return "uname";
		case __SYSCALL_modify_ldt: return "modify_ldt";
		case __SYSCALL_adjtimex: return "adjtimex";
		case __SYSCALL_mprotect: return "mprotect";
		case __SYSCALL_sigprocmask: return "sigprocmask";
		case __SYSCALL_create_module: return "create_module";
		case __SYSCALL_init_module: return "init_module";
		case __SYSCALL_delete_module: return "delete_module";
		case __SYSCALL_get_kernel_syms: return "get_kernel_syms";
		case __SYSCALL_quotactl: return "quotactl";
		case __SYSCALL_getpgid: return "getpgid";
		case __SYSCALL_fchdir: return "fchdir";
		case __SYSCALL_bdflush: return "bdflush";
		case __SYSCALL_sysfs: return "sysfs";
		case __SYSCALL_personality: return "personality";
		case __SYSCALL_afs_syscall: return "afs_syscall";
		case __SYSCALL_setfsuid: return "setfsuid";
		case __SYSCALL_setfsgid: return "setfsgid";
		case __SYSCALL__llseek: return "_llseek";
		case __SYSCALL_getdents: return "getdents";
		case __SYSCALL__newselect: return "newselect";
		case __SYSCALL_flock: return "flock";
		case __SYSCALL_msync: return "msync";
		case __SYSCALL_readv: return "readv";
		case __SYSCALL_writev: return "writev";
		case __SYSCALL_getsid: return "getsid";
		case __SYSCALL_fdatasync: return "fdatasync";
		case __SYSCALL__sysctl: return "sysctl";
		case __SYSCALL_mlock: return "mlock";
		case __SYSCALL_munlock: return "munlock";
		case __SYSCALL_mlockall: return "mlockall";
		case __SYSCALL_munlockall: return "munlockall";
		case __SYSCALL_sched_setparam: return "sched_setparam";
		case __SYSCALL_sched_getparam: return "sched_getparam";
		case __SYSCALL_sched_setscheduler: return "sched_setscheduler";
		case __SYSCALL_sched_getscheduler: return "getsheduler";
		case __SYSCALL_sched_yield: return "sched_yield";
		case __SYSCALL_sched_get_priority_max: return "sched_get_priority_max";
		case __SYSCALL_sched_get_priority_min: return "sched_get_priority_min";
		case __SYSCALL_sched_rr_get_interval: return "sched_rr_get_interval";
		case __SYSCALL_nanosleep: return "nanosleep";
		case __SYSCALL_mremap: return "mremap";
		case __SYSCALL_setresuid	: return "setresuid";
		case __SYSCALL_getresuid: return "getresuid";
		case __SYSCALL_vm86: return "vm86";
		case __SYSCALL_query_module: return "query_module";
		case __SYSCALL_poll: return "poll";
		case __SYSCALL_nfsservctl: return "nfsservctl";
		case __SYSCALL_setresgid	: return "setresgid";
		case __SYSCALL_getresgid: return "getresgid";
		case __SYSCALL_prctl: return "prctl";
		case __SYSCALL_rt_sigreturn: return "rt_sigreturn";
		case __SYSCALL_rt_sigaction: return "rt_sigaction";
		case __SYSCALL_rt_sigprocmask: return "rt_sigprocmask";
		case __SYSCALL_rt_sigpending: return "rt_sigpending";
		case __SYSCALL_rt_sigtimedwait: return "rt_sigtimedwait";
		case __SYSCALL_rt_sigqueueinfo: return "rt_sigqueueinfo";
		case __SYSCALL_rt_sigsuspend: return "rt_sigsuspend";
		case __SYSCALL_pread: return "pread";
		case __SYSCALL_pwrite: return "pwrite";
		case __SYSCALL_chown: return "chown";
		case __SYSCALL_getcwd: return "getcwd";
		case __SYSCALL_capget: return "capget";
		case __SYSCALL_capset: return "capset";
		case __SYSCALL_sigaltstack: return "sigaltstack";
		case __SYSCALL_sendfile: return "sendfile";
		case __SYSCALL_getpmsg: return "getpmsg";
		case __SYSCALL_putpmsg: return "putpmsg";
		case __SYSCALL_vfork: return "vfork";
		case __SYSCALL_ugetrlimit: return "ugetrlimit";
		case __SYSCALL_mmap2: return "mmap2";
		case __SYSCALL_truncate64: return "truncate64";
		case __SYSCALL_ftruncate64: return "ftruncate64";
		case __SYSCALL_stat64: return "stat64";
		case __SYSCALL_lstat64: return "lstat64";
		case __SYSCALL_fstat64: return "fstat64";
		case __SYSCALL_lchown32: return "lchown32";
		case __SYSCALL_getuid32: return "getuid32";
		case __SYSCALL_getgid32: return "getgid32";
		case __SYSCALL_geteuid32: return "geteuid32";
		case __SYSCALL_getegid32: return "getegid32";
		case __SYSCALL_setreuid32: return "setreuid32";
		case __SYSCALL_setregid32: return "setregid32";
		case __SYSCALL_getgroups32: return "getgroups32";
		case __SYSCALL_setgroups32: return "setgroups32";
		case __SYSCALL_fchown32: return "fchown32";
		case __SYSCALL_setresuid32: return "setresuid32";
		case __SYSCALL_getresuid32: return "getresuid32";
		case __SYSCALL_setresgid32: return "setresgid32";
		case __SYSCALL_getresgid32: return "getresgid32";
		case __SYSCALL_chown32: return "chown32";
		case __SYSCALL_setuid32: return "setuid32";
		case __SYSCALL_setgid32: return "setgid32";
		case __SYSCALL_setfsuid32: return "setfsuid32";
		case __SYSCALL_setfsgid32: return "setfsgid32";
		case __SYSCALL_pivot_root: return "pivot_root";
		case __SYSCALL_mincore: return "mincore";
		case __SYSCALL_madvise: return "madvise";
		case __SYSCALL_getdents64: return "getdents64";
		case __SYSCALL_fcntl64: return "fnctl64";
	}
	return "?";
}

static BOOL gliss_syscall_exit(gliss_state_t *state) {
	gliss_platform_t *pf = gliss_platform(state);
	if(verbose)
		fprintf(verbose, "exit()\n");
	if(pf->running)
		pf->running = FALSE;
	return TRUE;
}

static BOOL gliss_syscall_fork(gliss_state_t *state) { RETURN(-1); return FALSE; }

static BOOL gliss_syscall_read(gliss_state_t *state) {
	int fd;
	size_t count;
	gliss_address_t buf_addr;
	void *buf;
	int64_t ret;

	PARM_BEGIN
		fd = _fd(gliss_platform(state), PARM(0));
		buf_addr = (uint32_t)PARM(1);
		count = (size_t)PARM(2);
	PARM_END
	buf = (void *)malloc(count);
	if(verbose)
		fprintf(verbose, "read(fd=%d, buf=0x%08x, count=%d)\n", fd, (uint32_t) buf_addr, count);

	if(buf) {
		ret = read(fd, buf, count);
		if(ret > 0) MEM_WRITE(buf_addr, buf, ret);
		free(buf);
	} else {
		ret = -1;
	}
	RETURN(ret);
	return (ret != (size_t) -1) ? TRUE : FALSE;
}


static BOOL gliss_syscall_write(gliss_state_t *state)
{
	int fd;
	size_t count;
	void *buf;
	gliss_address_t buf_addr;
	size_t ret;

	PARM_BEGIN
		fd = _fd(gliss_platform(state), PARM(0));
		buf_addr = (uint32_t)PARM(1);
		count = (size_t) PARM(2);
	PARM_END
	if(verbose)
		fprintf(verbose, "write(fd=%d, buf=0x%08x, count=%d)\n", fd, (uint32_t) buf_addr, count);
	buf = malloc(count);
	if(buf)
	{
		MEM_READ(buf, buf_addr, count);
		ret = write(fd, buf, count);
		free(buf);
	}
	else
	{
		ret = -1;
	}
	RETURN(ret);
	return (ret != (size_t) -1)?TRUE:FALSE;
}

static BOOL gliss_syscall_open(gliss_state_t *state) {
	gliss_address_t addr;
	int pathnamelen;
	char *pathname;
	int flags;
	mode_t mode;
	int ret;

	PARM_BEGIN
		addr = PARM(0);
		pathnamelen = STRLEN(addr);
		pathname = (char *) malloc(pathnamelen + 1);
		MEM_READ(pathname, addr, pathnamelen + 1);
		flags = PARM(1);
		mode = PARM(2);
	PARM_END
	if(verbose)
		fprintf(verbose, "open(pathname=\"%s\", flags=%d, mode=%d)\n", pathname, flags, mode);
	ret = open(pathname, flags, mode);
	free(pathname);
	RETURN(fd_new(gliss_platform(state), ret));
	return ret != -1;
}

static BOOL gliss_syscall_close(gliss_state_t *state)
{
	int fd;
	int ret;

	PARM_BEGIN
		fd = _fd(gliss_platform(state), PARM(0));
	PARM_END
	if(verbose)
		fprintf(verbose, "close(fd=%d)\n", fd);
	ret = close(fd);
	RETURN(ret);
	return ret != -1;
}

static BOOL gliss_syscall_waitpid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_creat(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_link(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_unlink(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_execve(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_chdir(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_time(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mknod(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_chmod(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_lchown(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_break(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_oldstat(gliss_state_t *state) { RETURN(-1); return FALSE; }

static BOOL gliss_syscall_lseek(gliss_state_t *state)
{
	int fildes;
	off_t offset;
	int whence;
	off_t ret;

	PARM_BEGIN
		fildes = _fd(gliss_platform(state), PARM(0));
		offset = PARM(1);
		whence = PARM(2);
	PARM_END
	if(verbose)
		fprintf(verbose, "lseek(fd=%d, offset=%lu, whence=%d)\n", fildes, offset, whence);
	ret = lseek(fildes, offset, whence);
	RETURN(ret);
	return ret != -1;
}

static BOOL gliss_syscall_getpid(gliss_state_t *state) {
	pid_t pid;

	if(verbose)
		fprintf(verbose, "getpid()\n");
	pid = getpid();
	RETURN(pid);
	return TRUE;
}

static BOOL gliss_syscall_mount(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_umount(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setuid(gliss_state_t *state) { RETURN(-1); return FALSE; }

static BOOL gliss_syscall_getuid(gliss_state_t *state) {
	uid_t uid;

	if(verbose)
		fprintf(verbose, "getuid()\n");
	uid = getuid();
	RETURN(uid);
	return TRUE;
}

static BOOL gliss_syscall_stime(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ptrace(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_alarm(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_oldfstat(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_pause(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_utime(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_stty(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_gtty(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_access(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_nice(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ftime(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sync(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_kill(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rename(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mkdir(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rmdir(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_dup(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_pipe(gliss_state_t *state) { RETURN(-1); return FALSE; }

static void gliss_swap(void *buf, int count)
{
	if(count > 0)
	{
		char temp[8];
		char *src = (char *) buf + count - 1;
		char *dst = temp;

		do
		{
			*dst = *src;
		} while(src--, dst++, --count);
	}
}

static void gliss_swap_tms(struct tms *buf)
{
	gliss_swap(&buf->tms_utime, sizeof(&buf->tms_utime));
	gliss_swap(&buf->tms_stime, sizeof(&buf->tms_stime));
	gliss_swap(&buf->tms_cutime, sizeof(&buf->tms_cutime));
	gliss_swap(&buf->tms_cstime, sizeof(&buf->tms_cstime));
}

static BOOL gliss_syscall_times(gliss_state_t *state) {
	gliss_address_t buf_addr;
	struct tms buf;
	clock_t ret;

	//printf("times is being executed\n");
	ret = times(&buf);
	if(swap) gliss_swap_tms(&buf);
	PARM_BEGIN
		buf_addr = PARM(0);
	PARM_END
	MEM_WRITE(buf_addr, &buf, sizeof(struct tms));
	RETURN(ret);
	//printf("times return: %d\n",ret);
	return ret != (clock_t) -1;
}


static BOOL gliss_syscall_prof(gliss_state_t *state) { RETURN(-1); return FALSE; }

static BOOL gliss_syscall_brk(gliss_state_t *state) {
	uint32_t new_brk_addr;
	BOOL success;
	gliss_platform_t *pf = gliss_platform(state);

	PARM_BEGIN
		new_brk_addr = PARM(0);
	PARM_END
	success = FALSE;

	if(verbose) {
		fprintf(verbose, "new_brk(end=0x%08x)\n", new_brk_addr);
		fprintf(verbose, "brk(end=0x%08x)\n", pf->brk_base);
	}

	if(new_brk_addr > pf->brk_base) {
		pf->brk_base = new_brk_addr;
		success = TRUE;
	}

	RETURN(pf->brk_base);
	return success;
}

static BOOL gliss_syscall_setgid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getgid(gliss_state_t *state) {
	gid_t gid;
	if(verbose)
		fprintf(verbose, "getgid()\n");
	gid = getgid();
	RETURN(gid);
	return TRUE;
}

static BOOL gliss_syscall_signal(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_geteuid(gliss_state_t *state) {
	uid_t uid;

	if(verbose)
		fprintf(verbose, "geteuid()\n");
	uid = geteuid();
	RETURN(uid);
	return TRUE;
}

static BOOL gliss_syscall_getegid(gliss_state_t *state) {
	gid_t gid;

	if(verbose)
		fprintf(verbose, "getegid()\n");
	gid = getegid();
	RETURN(gid);
	return TRUE;
}

static BOOL gliss_syscall_acct(gliss_state_t *state)
{
  if(verbose)
    fprintf(verbose, "acct() not implemented.\n");
  RETURN(-1); return FALSE;
}

static BOOL gliss_syscall_umount2(gliss_state_t *state)
{
  if(verbose)
    fprintf(verbose, "unmount2() not implemented.\n");
  RETURN(-1); return FALSE;
}

static BOOL gliss_syscall_lock(gliss_state_t *state)
{
  if(verbose)
    fprintf(verbose, "lock() not implemented.\n");
  RETURN(-1); return FALSE;
}

static BOOL gliss_syscall_ioctl(gliss_state_t *state)
{
  if(verbose)
    fprintf(verbose, "ioctl() not implemented.\n");
  RETURN(-1); return FALSE;
}

static BOOL gliss_syscall_fcntl(gliss_state_t *state)  {  RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mpx(gliss_state_t *state) { RETURN(-1); return FALSE;  }
static BOOL gliss_syscall_setpgid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ulimit(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_oldolduname(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_umask(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_chroot(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ustat(gliss_state_t *state)  { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_dup2(gliss_state_t *state) { RETURN(-1); return FALSE;  }
static BOOL gliss_syscall_getppid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getpgrp(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setsid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sigaction(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sgetmask(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ssetmask(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setreuid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setregid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sigsuspend(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sigpending(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sethostname(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setrlimit(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getrlimit(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getrusage(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_gettimeofday(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_settimeofday(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getgroups(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setgroups(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_select(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_symlink(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_oldlstat(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_readlink(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_uselib(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_swapon(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_reboot(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_readdir(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mmap(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_munmap(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_truncate(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ftruncate(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_fchmod(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_fchown(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getpriority(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setpriority(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_profil(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_statfs(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_fstatfs(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ioperm(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_socketcall(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_syslog(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setitimer(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getitimer(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_stat(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_lstat(gliss_state_t *state) { RETURN(-1); return FALSE; }

static void gliss_swap_stat(struct stat *buf) {
	gliss_swap(&buf->st_dev, sizeof(&buf->st_dev));
	gliss_swap(&buf->st_ino, sizeof(&buf->st_ino));
	gliss_swap(&buf->st_mode, sizeof(&buf->st_mode));
	gliss_swap(&buf->st_nlink, sizeof(&buf->st_nlink));
	gliss_swap(&buf->st_uid, sizeof(&buf->st_uid));
	gliss_swap(&buf->st_gid, sizeof(&buf->st_gid));
	gliss_swap(&buf->st_rdev, sizeof(&buf->st_rdev));
	gliss_swap(&buf->st_size, sizeof(&buf->st_size));
	gliss_swap(&buf->st_blksize, sizeof(&buf->st_blksize));
	gliss_swap(&buf->st_blocks, sizeof(&buf->st_blocks));
	gliss_swap(&buf->st_atime, sizeof(&buf->st_atime));
	gliss_swap(&buf->st_mtime, sizeof(&buf->st_mtime));
	gliss_swap(&buf->st_ctime, sizeof(&buf->st_ctime));
	gliss_swap(&buf->st_ino, sizeof(&buf->st_ino));
}

static BOOL gliss_syscall_fstat(gliss_state_t *state) {
	int fd;
	struct stat *buf;
	gliss_address_t buf_addr;
	int ret;

	PARM_BEGIN
		fd = _fd(gliss_platform(state), PARM(0));
		buf_addr = PARM(1);
	PARM_END
	if(verbose)
		fprintf(verbose, "fstat(fd=%d, buf=0x%08x)\n", fd, (uint32_t)buf_addr);
	buf = (struct stat *) malloc(sizeof(struct stat));
	if(buf)
	{
		ret = fstat(fd, buf);
		if(ret >= 0)
		{
			if(swap) gliss_swap_stat(buf);
			MEM_WRITE(buf_addr, buf, sizeof(struct stat));
		}
		free(buf);
	}
	else
	{
		ret = -1;
	}
	RETURN(ret);
	return ret != -1;
}

static BOOL gliss_syscall_olduname(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_iopl(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_vhangup(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_idle(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_vm86old(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_wait4(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_swapoff(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sysinfo(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ipc(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_fsync(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sigreturn(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_clone(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setdomainname(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_uname(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_modify_ldt(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_adjtimex(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mprotect(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sigprocmask(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_create_module(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_init_module(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_delete_module(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_get_kernel_syms(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_quotactl(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getpgid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_fchdir(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_bdflush(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sysfs(gliss_state_t *state) { RETURN(-1); return FALSE; }

static BOOL gliss_syscall_personality(gliss_state_t *state) { return TRUE; }

static BOOL gliss_syscall_afs_syscall(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setfsuid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setfsgid(gliss_state_t *state) { RETURN(-1); return FALSE; }

static BOOL gliss_syscall__llseek(gliss_state_t *state)
{
	int fd;
	uint32_t offset_high;
	uint32_t offset_low;
	gliss_address_t result_addr;
	int whence;
	int ret;

	PARM_BEGIN
		fd = _fd(gliss_platform(state), PARM(0));
		offset_high = PARM(1);
		offset_low = PARM(2);
		result_addr = PARM(3);
		whence = PARM(4);
	PARM_END
	if(verbose)
		fprintf(verbose, "_lseek(fd=%d, offset_high=%u, offset_low=%u, result=0x%08x, whence=%d)\n", fd, offset_high, offset_low, (uint32_t) result_addr, whence);
	if(offset_high == 0)
	{
		off_t lseek_ret = lseek(fd, offset_low, whence);
		if(lseek_ret >= 0)
		{
			MEM_WRITE_DWORD(result_addr, lseek_ret);
			ret = 0;
		}
		else
		{
			ret = -1;
		}
	}
	else
	{
		ret = -1;
	}
	RETURN(ret);
	return ret != -1;
}

static BOOL gliss_syscall_getdents(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_newselect(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_flock(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_msync(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_readv(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_writev(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getsid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_fdatasync(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sysctl(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mlock(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_munlock(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mlockall(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_munlockall(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sched_setparam(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sched_getparam(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sched_setscheduler(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getsheduler(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sched_yield(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sched_get_priority_max(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sched_get_priority_min(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sched_rr_get_interval(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_nanosleep(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mremap(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setresuid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getresuid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_vm86(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_query_module(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_poll(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_nfsservctl(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setresgid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getresgid(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_prctl(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rt_sigreturn(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rt_sigaction(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rt_sigprocmask(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rt_sigpending(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rt_sigtimedwait(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rt_sigqueueinfo(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_rt_sigsuspend(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_pread(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_pwrite(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_chown(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getcwd(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_capget(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_capset(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sigaltstack(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_sendfile(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getpmsg(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_putpmsg(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_vfork(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ugetrlimit(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mmap2(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_truncate64(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_ftruncate64(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_stat64(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_lstat64(gliss_state_t *state) { RETURN(-1); return FALSE; }

#ifdef linux
static void gliss_swap_stat64(struct stat64 *buf)
{
	gliss_swap(&buf->st_dev, sizeof(&buf->st_dev));
	#if __WORDSIZE == 64
		gliss_swap(&buf->st_ino, sizeof(&buf->st_ino));
	#else
		gliss_swap(&buf->__pad1, sizeof(&buf->__pad1));
		gliss_swap(&buf->__st_ino, sizeof(&buf->__st_ino));
	#endif
	gliss_swap(&buf->st_mode, sizeof(&buf->st_mode));
	gliss_swap(&buf->st_nlink, sizeof(&buf->st_nlink));
	gliss_swap(&buf->st_uid, sizeof(&buf->st_uid));
	gliss_swap(&buf->st_gid, sizeof(&buf->st_gid));
	gliss_swap(&buf->st_rdev, sizeof(&buf->st_rdev));
	#if __WORDSIZE == 64
	#else
		gliss_swap(&buf->__pad2, sizeof(&buf->__pad2));
	#endif
	gliss_swap(&buf->st_size, sizeof(&buf->st_size));
	gliss_swap(&buf->st_blksize, sizeof(&buf->st_blksize));
	gliss_swap(&buf->st_blocks, sizeof(&buf->st_blocks));
	gliss_swap(&buf->st_ino, sizeof(&buf->st_ino));
#if __GLIBC_PREREQ(2,3)
#    if __USE_MISC
	/* st_atime, st_mtime and st_ctime are macros */
	gliss_swap(&buf->st_atim, sizeof(&buf->st_atim));
	gliss_swap(&buf->st_mtim, sizeof(&buf->st_mtim));
	gliss_swap(&buf->st_ctim, sizeof(&buf->st_ctim));
#    else
	gliss_swap(&buf->st_atime, sizeof(&buf->st_atime));
	gliss_swap(&buf->__st_atimensec, sizeof(&buf->__st_atimensec));
	gliss_swap(&buf->st_mtime, sizeof(&buf->st_mtime));
	gliss_swap(&buf->__st_mtimensec, sizeof(&buf->__st_mtimensec));
	gliss_swap(&buf->st_ctime, sizeof(&buf->st_ctime));
	gliss_swap(&buf->__st_ctimensec, sizeof(&buf->__st_ctimensec));
#    endif
#elif   __GLIBC_PREREQ(2,2)
	gliss_swap(&buf->st_atime, sizeof(&buf->st_atime));
	gliss_swap(&buf->__unused1, sizeof(&buf->__unused1));
	gliss_swap(&buf->st_mtime, sizeof(&buf->st_mtime));
	gliss_swap(&buf->__unused2, sizeof(&buf->__unused2));
	gliss_swap(&buf->st_ctime, sizeof(&buf->st_ctime));
	gliss_swap(&buf->__unused3, sizeof(&buf->__unused3));
#else
#	error "Glibc 2.2 or greater needed"
#endif
}
#endif

static BOOL gliss_syscall_fstat64(gliss_state_t *state) {
#ifdef linux
	int fd;
	gliss_address_t buf_addr;
	struct stat64 *buf;
	int ret;

	PARM_BEGIN
		fd = _fd(gliss_platform(state), PARM(0));
		buf_addr = PARM(1);
	PARM_END
	if(verbose)
		fprintf(verbose, "fstat64(fd=%d, buf=0x%08x)\n", fd, (uint32_t) buf_addr);
	buf = (struct stat64 *) malloc(sizeof(struct stat64));
	if(buf)
	{
		ret = fstat64(fd, buf);
		if(ret >= 0)
		{
			if(swap) gliss_swap_stat64(buf);
			MEM_WRITE(buf_addr, buf, sizeof(struct stat64));
		}
		free(buf);
	}
	else
	{
		ret = -1;
	}
	RETURN(ret);
	return ret != -1;
#else
	int ret = -1;
	RETURN(ret);
	return FALSE;
#endif
}

static BOOL gliss_syscall_lchown32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getuid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getgid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_geteuid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getegid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setreuid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setregid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getgroups32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setgroups32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_fchown32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setresuid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getresuid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setresgid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getresgid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_chown32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setuid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setgid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setfsuid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_setfsgid32(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_pivot_root(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_mincore(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_madvise(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_getdents64(gliss_state_t *state) { RETURN(-1); return FALSE; }
static BOOL gliss_syscall_fnctl64(gliss_state_t *state) { RETURN(-1); return FALSE; }


void gliss_syscall(gliss_inst_t *inst, gliss_state_t *state) {
	int syscall_num;
	BOOL ret = FALSE;

	syscall_num = GLISS_SYSCALL_CODE(inst, state);
	if(verbose)
		fprintf(verbose, "got a system call (number : %u; name : %s)\n", syscall_num, gliss_get_syscall_name(syscall_num));

	switch(syscall_num) {
	case __SYSCALL_exit: ret = gliss_syscall_exit(state); break;
	case __SYSCALL_fork: ret = gliss_syscall_fork(state); break;
	case __SYSCALL_read: ret = gliss_syscall_read(state); break;
	case __SYSCALL_write: ret = gliss_syscall_write(state); break;
	case __SYSCALL_open: ret = gliss_syscall_open(state); break;
	case __SYSCALL_close: ret = gliss_syscall_close(state); break;
	case __SYSCALL_waitpid: ret = gliss_syscall_waitpid(state); break;
	case __SYSCALL_creat: ret = gliss_syscall_creat(state); break;
	case __SYSCALL_link: ret = gliss_syscall_link(state); break;
	case __SYSCALL_unlink: ret = gliss_syscall_unlink(state); break;
	case __SYSCALL_execve: ret = gliss_syscall_execve(state); break;
	case __SYSCALL_chdir: ret = gliss_syscall_chdir(state); break;
	case __SYSCALL_time: ret = gliss_syscall_time(state); break;
	case __SYSCALL_mknod: ret = gliss_syscall_mknod(state); break;
	case __SYSCALL_chmod: ret = gliss_syscall_chmod(state); break;
	case __SYSCALL_lchown: ret = gliss_syscall_lchown(state); break;
	case __SYSCALL_break: ret = gliss_syscall_break(state); break;
	case __SYSCALL_oldstat: ret = gliss_syscall_oldstat(state); break;
	case __SYSCALL_lseek: ret = gliss_syscall_lseek(state); break;
	case __SYSCALL_getpid: ret = gliss_syscall_getpid(state); break;
	case __SYSCALL_mount: ret = gliss_syscall_mount(state); break;
	case __SYSCALL_umount: ret = gliss_syscall_umount(state); break;
	case __SYSCALL_setuid: ret = gliss_syscall_setuid(state); break;
	case __SYSCALL_getuid: ret = gliss_syscall_getuid(state); break;
	case __SYSCALL_stime: ret = gliss_syscall_stime(state); break;
	case __SYSCALL_ptrace: ret = gliss_syscall_ptrace(state); break;
	case __SYSCALL_alarm: ret = gliss_syscall_alarm(state); break;
	case __SYSCALL_oldfstat: ret = gliss_syscall_oldfstat(state); break;
	case __SYSCALL_pause: ret = gliss_syscall_pause(state); break;
	case __SYSCALL_utime: ret = gliss_syscall_utime(state); break;
	case __SYSCALL_stty: ret = gliss_syscall_stty(state); break;
	case __SYSCALL_gtty: ret = gliss_syscall_gtty(state); break;
	case __SYSCALL_access: ret = gliss_syscall_access(state); break;
	case __SYSCALL_nice: ret = gliss_syscall_nice(state); break;
	case __SYSCALL_ftime: ret = gliss_syscall_ftime(state); break;
	case __SYSCALL_sync: ret = gliss_syscall_sync(state); break;
	case __SYSCALL_kill: ret = gliss_syscall_kill(state); break;
	case __SYSCALL_rename: ret = gliss_syscall_rename(state); break;
	case __SYSCALL_mkdir: ret = gliss_syscall_mkdir(state); break;
	case __SYSCALL_rmdir: ret = gliss_syscall_rmdir(state); break;
	case __SYSCALL_dup: ret = gliss_syscall_dup(state); break;
	case __SYSCALL_pipe: ret = gliss_syscall_pipe(state); break;
	case __SYSCALL_times: ret = gliss_syscall_times(state); break;
	case __SYSCALL_prof: ret = gliss_syscall_prof(state); break;
	case __SYSCALL_brk: ret = gliss_syscall_brk(state); break;
	case __SYSCALL_setgid: ret = gliss_syscall_setgid(state); break;
	case __SYSCALL_getgid: ret = gliss_syscall_getgid(state); break;
	case __SYSCALL_signal: ret = gliss_syscall_signal(state); break;
	case __SYSCALL_geteuid: ret = gliss_syscall_geteuid(state); break;
	case __SYSCALL_getegid: ret = gliss_syscall_getegid(state); break;
	case __SYSCALL_acct: ret = gliss_syscall_acct(state); break;
	case __SYSCALL_umount2: ret = gliss_syscall_umount2(state); break;
	case __SYSCALL_lock: ret = gliss_syscall_lock(state); break;
	case __SYSCALL_ioctl: ret = gliss_syscall_ioctl(state); break;
	case __SYSCALL_fcntl: ret = gliss_syscall_fcntl(state); break;
	case __SYSCALL_mpx: ret = gliss_syscall_mpx(state); break;
	case __SYSCALL_setpgid: ret = gliss_syscall_setpgid(state); break;
	case __SYSCALL_ulimit: ret = gliss_syscall_ulimit(state); break;
	case __SYSCALL_oldolduname: ret = gliss_syscall_oldolduname(state); break;
	case __SYSCALL_umask: ret = gliss_syscall_umask(state); break;
	case __SYSCALL_chroot: ret = gliss_syscall_chroot(state); break;
	case __SYSCALL_ustat	: ret = gliss_syscall_ustat(state); break;
	case __SYSCALL_dup2: ret = gliss_syscall_dup2(state); break;
	case __SYSCALL_getppid: ret = gliss_syscall_getppid(state); break;
	case __SYSCALL_getpgrp: ret = gliss_syscall_getpgrp(state); break;
	case __SYSCALL_setsid: ret = gliss_syscall_setsid(state); break;
	case __SYSCALL_sigaction: ret = gliss_syscall_sigaction(state); break;
	case __SYSCALL_sgetmask: ret = gliss_syscall_sgetmask(state); break;
	case __SYSCALL_ssetmask: ret = gliss_syscall_ssetmask(state); break;
	case __SYSCALL_setreuid: ret = gliss_syscall_setreuid(state); break;
	case __SYSCALL_setregid: ret = gliss_syscall_setregid(state); break;
	case __SYSCALL_sigsuspend: ret = gliss_syscall_sigsuspend(state); break;
	case __SYSCALL_sigpending: ret = gliss_syscall_sigpending(state); break;
	case __SYSCALL_sethostname: ret = gliss_syscall_sethostname(state); break;
	case __SYSCALL_setrlimit: ret = gliss_syscall_setrlimit(state); break;
	case __SYSCALL_getrlimit: ret = gliss_syscall_getrlimit(state); break;
	case __SYSCALL_getrusage: ret = gliss_syscall_getrusage(state); break;
	case __SYSCALL_gettimeofday: ret = gliss_syscall_gettimeofday(state); break;
	case __SYSCALL_settimeofday: ret = gliss_syscall_settimeofday(state); break;
	case __SYSCALL_getgroups: ret = gliss_syscall_getgroups(state); break;
	case __SYSCALL_setgroups: ret = gliss_syscall_setgroups(state); break;
	case __SYSCALL_select: ret = gliss_syscall_select(state); break;
	case __SYSCALL_symlink: ret = gliss_syscall_symlink(state); break;
	case __SYSCALL_oldlstat: ret = gliss_syscall_oldlstat(state); break;
	case __SYSCALL_readlink: ret = gliss_syscall_readlink(state); break;
	case __SYSCALL_uselib: ret = gliss_syscall_uselib(state); break;
	case __SYSCALL_swapon: ret = gliss_syscall_swapon(state); break;
	case __SYSCALL_reboot: ret = gliss_syscall_reboot(state); break;
	case __SYSCALL_readdir: ret = gliss_syscall_readdir(state); break;
	case __SYSCALL_mmap: ret = gliss_syscall_mmap(state); break;
	case __SYSCALL_munmap: ret = gliss_syscall_munmap(state); break;
	case __SYSCALL_truncate: ret = gliss_syscall_truncate(state); break;
	case __SYSCALL_ftruncate: ret = gliss_syscall_ftruncate(state); break;
	case __SYSCALL_fchmod: ret = gliss_syscall_fchmod(state); break;
	case __SYSCALL_fchown: ret = gliss_syscall_fchown(state); break;
	case __SYSCALL_getpriority: ret = gliss_syscall_getpriority(state); break;
	case __SYSCALL_setpriority: ret = gliss_syscall_setpriority(state); break;
	case __SYSCALL_profil: ret = gliss_syscall_profil(state); break;
	case __SYSCALL_statfs: ret = gliss_syscall_statfs(state); break;
	case __SYSCALL_fstatfs: ret = gliss_syscall_fstatfs(state); break;
	case __SYSCALL_ioperm: ret = gliss_syscall_ioperm(state); break;
	case __SYSCALL_socketcall: ret = gliss_syscall_socketcall(state); break;
	case __SYSCALL_syslog: ret = gliss_syscall_syslog(state); break;
	case __SYSCALL_setitimer: ret = gliss_syscall_setitimer(state); break;
	case __SYSCALL_getitimer: ret = gliss_syscall_getitimer(state); break;
	case __SYSCALL_stat: ret = gliss_syscall_stat(state); break;
	case __SYSCALL_lstat: ret = gliss_syscall_lstat(state); break;
	case __SYSCALL_fstat: ret = gliss_syscall_fstat(state); break;
	case __SYSCALL_olduname: ret = gliss_syscall_olduname(state); break;
	case __SYSCALL_iopl: ret = gliss_syscall_iopl(state); break;
	case __SYSCALL_vhangup: ret = gliss_syscall_vhangup(state); break;
	case __SYSCALL_idle: ret = gliss_syscall_idle(state); break;
	case __SYSCALL_vm86old: ret = gliss_syscall_vm86old(state); break;
	case __SYSCALL_wait4: ret = gliss_syscall_wait4(state); break;
	case __SYSCALL_swapoff: ret = gliss_syscall_swapoff(state); break;
	case __SYSCALL_sysinfo: ret = gliss_syscall_sysinfo(state); break;
	case __SYSCALL_ipc: ret = gliss_syscall_ipc(state); break;
	case __SYSCALL_fsync: ret = gliss_syscall_fsync(state); break;
	case __SYSCALL_sigreturn: ret = gliss_syscall_sigreturn(state); break;
	case __SYSCALL_clone: ret = gliss_syscall_clone(state); break;
	case __SYSCALL_setdomainname: ret = gliss_syscall_setdomainname(state); break;
	case __SYSCALL_uname: ret = gliss_syscall_uname(state); break;
	case __SYSCALL_modify_ldt: ret = gliss_syscall_modify_ldt(state); break;
	case __SYSCALL_adjtimex: ret = gliss_syscall_adjtimex(state); break;
	case __SYSCALL_mprotect: ret = gliss_syscall_mprotect(state); break;
	case __SYSCALL_sigprocmask: ret = gliss_syscall_sigprocmask(state); break;
	case __SYSCALL_create_module: ret = gliss_syscall_create_module(state); break;
	case __SYSCALL_init_module: ret = gliss_syscall_init_module(state); break;
	case __SYSCALL_delete_module: ret = gliss_syscall_delete_module(state); break;
	case __SYSCALL_get_kernel_syms: ret = gliss_syscall_get_kernel_syms(state); break;
	case __SYSCALL_quotactl: ret = gliss_syscall_quotactl(state); break;
	case __SYSCALL_getpgid: ret = gliss_syscall_getpgid(state); break;
	case __SYSCALL_fchdir: ret = gliss_syscall_fchdir(state); break;
	case __SYSCALL_bdflush: ret = gliss_syscall_bdflush(state); break;
	case __SYSCALL_sysfs: ret = gliss_syscall_sysfs(state); break;
	case __SYSCALL_personality: ret = gliss_syscall_personality(state); break;
	case __SYSCALL_afs_syscall: ret = gliss_syscall_afs_syscall(state); break;
	case __SYSCALL_setfsuid: ret = gliss_syscall_setfsuid(state); break;
	case __SYSCALL_setfsgid: ret = gliss_syscall_setfsgid(state); break;
	case __SYSCALL__llseek: ret = gliss_syscall__llseek(state); break;
	case __SYSCALL_getdents: ret = gliss_syscall_getdents(state); break;
	case __SYSCALL__newselect: ret = gliss_syscall_newselect(state); break;
	case __SYSCALL_flock: ret = gliss_syscall_flock(state); break;
	case __SYSCALL_msync: ret = gliss_syscall_msync(state); break;
	case __SYSCALL_readv: ret = gliss_syscall_readv(state); break;
	case __SYSCALL_writev: ret = gliss_syscall_writev(state); break;
	case __SYSCALL_getsid: ret = gliss_syscall_getsid(state); break;
	case __SYSCALL_fdatasync: ret = gliss_syscall_fdatasync(state); break;
	case __SYSCALL__sysctl: ret = gliss_syscall_sysctl(state); break;
	case __SYSCALL_mlock: ret = gliss_syscall_mlock(state); break;
	case __SYSCALL_munlock: ret = gliss_syscall_munlock(state); break;
	case __SYSCALL_mlockall: ret = gliss_syscall_mlockall(state); break;
	case __SYSCALL_munlockall: ret = gliss_syscall_munlockall(state); break;
	case __SYSCALL_sched_setparam: ret = gliss_syscall_sched_setparam(state); break;
	case __SYSCALL_sched_getparam: ret = gliss_syscall_sched_getparam(state); break;
	case __SYSCALL_sched_setscheduler: ret = gliss_syscall_sched_setscheduler(state); break;
	case __SYSCALL_sched_getscheduler: ret = gliss_syscall_getsheduler(state); break;
	case __SYSCALL_sched_yield: ret = gliss_syscall_sched_yield(state); break;
	case __SYSCALL_sched_get_priority_max: ret = gliss_syscall_sched_get_priority_max(state); break;
	case __SYSCALL_sched_get_priority_min: ret = gliss_syscall_sched_get_priority_min(state); break;
	case __SYSCALL_sched_rr_get_interval: ret = gliss_syscall_sched_rr_get_interval(state); break;
	case __SYSCALL_nanosleep: ret = gliss_syscall_nanosleep(state); break;
	case __SYSCALL_mremap: ret = gliss_syscall_mremap(state); break;
	case __SYSCALL_setresuid	: ret = gliss_syscall_setresuid(state); break;
	case __SYSCALL_getresuid: ret = gliss_syscall_getresuid(state); break;
	case __SYSCALL_vm86: ret = gliss_syscall_vm86(state); break;
	case __SYSCALL_query_module: ret = gliss_syscall_query_module(state); break;
	case __SYSCALL_poll: ret = gliss_syscall_poll(state); break;
	case __SYSCALL_nfsservctl: ret = gliss_syscall_nfsservctl(state); break;
	case __SYSCALL_setresgid	: ret = gliss_syscall_setresgid(state); break;
	case __SYSCALL_getresgid: ret = gliss_syscall_getresgid(state); break;
	case __SYSCALL_prctl: ret = gliss_syscall_prctl(state); break;
	case __SYSCALL_rt_sigreturn: ret = gliss_syscall_rt_sigreturn(state); break;
	case __SYSCALL_rt_sigaction: ret = gliss_syscall_rt_sigaction(state); break;
	case __SYSCALL_rt_sigprocmask: ret = gliss_syscall_rt_sigprocmask(state); break;
	case __SYSCALL_rt_sigpending: ret = gliss_syscall_rt_sigpending(state); break;
	case __SYSCALL_rt_sigtimedwait: ret = gliss_syscall_rt_sigtimedwait(state); break;
	case __SYSCALL_rt_sigqueueinfo: ret = gliss_syscall_rt_sigqueueinfo(state); break;
	case __SYSCALL_rt_sigsuspend: ret = gliss_syscall_rt_sigsuspend(state); break;
	case __SYSCALL_pread: ret = gliss_syscall_pread(state); break;
	case __SYSCALL_pwrite: ret = gliss_syscall_pwrite(state); break;
	case __SYSCALL_chown: ret = gliss_syscall_chown(state); break;
	case __SYSCALL_getcwd: ret = gliss_syscall_getcwd(state); break;
	case __SYSCALL_capget: ret = gliss_syscall_capget(state); break;
	case __SYSCALL_capset: ret = gliss_syscall_capset(state); break;
	case __SYSCALL_sigaltstack: ret = gliss_syscall_sigaltstack(state); break;
	case __SYSCALL_sendfile: ret = gliss_syscall_sendfile(state); break;
	case __SYSCALL_getpmsg: ret = gliss_syscall_getpmsg(state); break;
	case __SYSCALL_putpmsg: ret = gliss_syscall_putpmsg(state); break;
	case __SYSCALL_vfork: ret = gliss_syscall_vfork(state); break;
	case __SYSCALL_ugetrlimit: ret = gliss_syscall_ugetrlimit(state); break;
	case __SYSCALL_mmap2: ret = gliss_syscall_mmap2(state); break;
	case __SYSCALL_truncate64: ret = gliss_syscall_truncate64(state); break;
	case __SYSCALL_ftruncate64: ret = gliss_syscall_ftruncate64(state); break;
	case __SYSCALL_stat64: ret = gliss_syscall_stat64(state); break;
	case __SYSCALL_lstat64: ret = gliss_syscall_lstat64(state); break;
	case __SYSCALL_fstat64: ret = gliss_syscall_fstat64(state); break;
	case __SYSCALL_lchown32: ret = gliss_syscall_lchown32(state); break;
	case __SYSCALL_getuid32: ret = gliss_syscall_getuid32(state); break;
	case __SYSCALL_getgid32: ret = gliss_syscall_getgid32(state); break;
	case __SYSCALL_geteuid32: ret = gliss_syscall_geteuid32(state); break;
	case __SYSCALL_getegid32: ret = gliss_syscall_getegid32(state); break;
	case __SYSCALL_setreuid32: ret = gliss_syscall_setreuid32(state); break;
	case __SYSCALL_setregid32: ret = gliss_syscall_setregid32(state); break;
	case __SYSCALL_getgroups32: ret = gliss_syscall_getgroups32(state); break;
	case __SYSCALL_setgroups32: ret = gliss_syscall_setgroups32(state); break;
	case __SYSCALL_fchown32: ret = gliss_syscall_fchown32(state); break;
	case __SYSCALL_setresuid32: ret = gliss_syscall_setresuid32(state); break;
	case __SYSCALL_getresuid32: ret = gliss_syscall_getresuid32(state); break;
	case __SYSCALL_setresgid32: ret = gliss_syscall_setresgid32(state); break;
	case __SYSCALL_getresgid32: ret = gliss_syscall_getresgid32(state); break;
	case __SYSCALL_chown32: ret = gliss_syscall_chown32(state); break;
	case __SYSCALL_setuid32: ret = gliss_syscall_setuid32(state); break;
	case __SYSCALL_setgid32: ret = gliss_syscall_setgid32(state); break;
	case __SYSCALL_setfsuid32: ret = gliss_syscall_setfsuid32(state); break;
	case __SYSCALL_setfsgid32: ret = gliss_syscall_setfsgid32(state); break;
	case __SYSCALL_pivot_root: ret = gliss_syscall_pivot_root(state); break;
	case __SYSCALL_mincore: ret = gliss_syscall_mincore(state); break;
	case __SYSCALL_madvise: ret = gliss_syscall_madvise(state); break;
	case __SYSCALL_getdents64: ret = gliss_syscall_getdents64(state); break;
	case __SYSCALL_fcntl64: ret = gliss_syscall_fnctl64(state); break;
	}

	if(!ret) {
		if(verbose)
			fprintf(verbose, "Warning : system call returns an error (number : %u, name : %s)\n", syscall_num, gliss_get_syscall_name(syscall_num));
		SET_CR0SO;
	}
	else
		RESET_CR0SO;
}


