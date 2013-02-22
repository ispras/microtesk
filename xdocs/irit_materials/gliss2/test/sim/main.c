#include <assert.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include "memory.h"
#include "loader.h"

int main(void) {
	gliss_memory_t *memory1, *memory2;
	gliss_loader_t *loader;
	
	/* allocate memory */
	memory1 = gliss_mem_new();
	assert(memory1 != NULL);

	/* allocate memory */
	memory2 = gliss_mem_new();
	assert(memory2 != NULL);
	
	/* open an ELF file */
	loader = gliss_loader_open("primes");
	if(loader == NULL) {
		printf("error %d:%s\n", errno, strerror(errno));
		return 1;
	}
	
	/* load it */
	gliss_loader_load(loader, memory1);
	gliss_loader_load(loader, memory2);	
	
	/* free it */
	gliss_loader_close(loader);
	
	/* free memory */
	gliss_mem_delete(memory1);
	gliss_mem_delete(memory2);
	
	return 0;
}
