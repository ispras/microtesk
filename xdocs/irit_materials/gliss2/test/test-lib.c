#include <stdio.h>
#include "../lib/gliss.h"

struct {
	unsigned long v, r, n, t;
} left_tests[] = {
	{ 0x00ff00ff, 8, 32, 0xff00ff00 }, 
	{ 0xff0000ff, 8, 32, 0x0000ffff },
	{ 0x00ff00ff, 8, 24, 0x0000ffff }, 
	{ 0x0000ffff, 8, 24, 0x00ffff00 },
	{ 0, 0, 0 }
};


struct {
	unsigned long v, r, n, t;
} right_tests[] = {
	{ 0x00ff00ff, 8, 32, 0xff00ff00 }, 
	{ 0xff0000ff, 8, 32, 0xffff0000 },
	{ 0x00ff00ff, 8, 24, 0x00ffff00 }, 
	{ 0x0000ffff, 8, 24, 0x00ff00ff },
	{ 0, 0, 0 }
};

int main(void) {
	int i;
	int errors = 0;
	
	printf("mask(%d) = %08x\n", 32, gliss_mask32(32));
	
	// left rotations
	for(i = 0; left_tests[i].n; i++) {
		uint32_t v =
			gliss_rotate_left32(
				left_tests[i].v,
				left_tests[i].r,
				left_tests[i].n
			);
		printf("%08x <<< %d [%d] = %08x (%08x)",
			left_tests[i].v,
			left_tests[i].r,
			left_tests[i].n,
			v,
			left_tests[i].t);
		if(v != left_tests[i].t) {
			errors++;
			printf(" failed !");
		}
		putchar('\n');
	}

	// right rotations
	for(i = 0; right_tests[i].n; i++) {
		uint32_t v = gliss_rotate_right32(
			right_tests[i].v,
			right_tests[i].r,
			right_tests[i].n
		);
		printf("%08x >>> %d [%d] = %08x (%08x)",
			right_tests[i].v,
			right_tests[i].r,
			right_tests[i].n,
			v,
			right_tests[i].t);
		if(v != right_tests[i].t) {
			errors++;
			printf(" failed !");
		}
		putchar('\n');
	}
	
	// display errors
	printf("%d errors\n", errors);
}
