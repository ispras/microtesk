#include <sys/types.h>
#include <unistd.h>

#define SC(n) __attribute__ ((syscall(n)))
#define UNSUPPORTED __attribute__ ((unsupported))
#define ONLY_ARGS __attribute__ ((only_args))
#define ONLY_EMULATE __attribute__ ((only_emulate))
#define FD __attribute__ ((fd))

void _exit(int code) SC(1) ONLY_ARGS {
	struct full_state *s = (struct full_state *)state;
	s->running = FALSE;
	s->return_code = code;
}

pid_t fork(void) SC(2) UNSUPPORTED { }

ssize_t read(FD int fd, void *buf, size_t count) SC(3) ONLY_EMULATE {
	return (result != (size_t) -1) ? TRUE : FALSE;
}
