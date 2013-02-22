#include <assert.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <ppc/mem.h>
#include "../src/loader.h"

int main(void) {
	int i;
	
	for(i = 0; i < 10; i++) {
		ppc_memory_t *memory1, *memory2;
		ppc_loader_t *loader;
		
		printf("PASS %d\n", i);
		
		/* allocate memory */
		memory1 = ppc_mem_new();
		assert(memory1 != NULL);

		/* allocate memory */
		memory2 = ppc_mem_new();
		assert(memory2 != NULL);
		
		/* open an ELF file */
		loader = ppc_loader_open("primes");
		if(loader == NULL) {
			printf("error %d:%s\n", errno, strerror(errno));
			return 1;
		}
		
		/* load it */
		ppc_loader_load(loader, memory1);
		ppc_loader_load(loader, memory2);	
		
		/* free it */
		ppc_loader_close(loader);
		
		/* free memory */
		ppc_mem_delete(memory1);
		ppc_mem_delete(memory2);
	}
	
	printf("SUCCESS !\n");
	return 0;
}
