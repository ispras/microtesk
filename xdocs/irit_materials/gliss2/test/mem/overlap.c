#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <stdint.h>

#define PAGE 4000

int main(void) {
	int i, x = 0;
	int vs[PAGE];
	uint64_t start_time, end_time, delay;
	srandom(time());

	/* fill the array */
	for(i = 0; i < PAGE; i++)
		vs[i] = random() & (PAGE - 1);

	/* condition condition */
	{
		struct rusage buf;
		getrusage(RUSAGE_SELF, &buf);
		start_time = (uint64_t)buf.ru_utime.tv_sec*1000000.00 + buf.ru_utime.tv_usec;
	}
	for(i = 0; i < 10000000; i++) {
		int v = vs[i & (PAGE - 1)];
		if((v & 0x1) || ((v + 2) > PAGE))
			x++;
	}
	{
		struct rusage buf;
		getrusage(RUSAGE_SELF, &buf);
		end_time = (uint64_t)buf.ru_utime.tv_sec*1000000.00 + buf.ru_utime.tv_usec;
		delay = end_time - start_time;
        fprintf(stderr, "condition condition time = %f ms\n", (double)delay / 1000.00);
	}

	/* condition */
	{
		struct rusage buf;
		getrusage(RUSAGE_SELF, &buf);
		start_time = (uint64_t)buf.ru_utime.tv_sec*1000000.00 + buf.ru_utime.tv_usec;
	}
	for(i = 0; i < 10000000; i++) {
		int v = vs[i & (PAGE - 1)];
		if((v & 0x1) | ((v + 2) > PAGE))
			x++;
	}
	{
		struct rusage buf;
		getrusage(RUSAGE_SELF, &buf);
		end_time = (uint64_t)buf.ru_utime.tv_sec*1000000.00 + buf.ru_utime.tv_usec;
		delay = end_time - start_time;
        fprintf(stderr, "condition time = %f ms\n", (double)delay / 1000.00);
	}

	/* perform the computation */
	{
		struct rusage buf;
		getrusage(RUSAGE_SELF, &buf);
		start_time = (uint64_t)buf.ru_utime.tv_sec*1000000.00 + buf.ru_utime.tv_usec;
	}
	for(i = 0; i < 10000000; i++) {
		int v = vs[i & (PAGE - 1)];
		if((v & 0x1) | ((v + 2) & PAGE))
			x++;
	}
	{
		struct rusage buf;
		getrusage(RUSAGE_SELF, &buf);
		end_time = (uint64_t)buf.ru_utime.tv_sec*1000000.00 + buf.ru_utime.tv_usec;
		delay = end_time - start_time;
        fprintf(stderr, "mask time = %f ms\n", (double)delay / 1000.00);
	}

	printf("count = %d\n", x);
	return 0;
}
