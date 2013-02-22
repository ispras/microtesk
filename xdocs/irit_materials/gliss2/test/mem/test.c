#include <stdio.h>
#include <assert.h>
#include <math.h>
#include <stdlib.h>
#include <gliss/mem.h>

#define PAGE	4096

int check_block(gliss_memory_t *mem, uint8_t buf[], gliss_address_t addr, int size) {
	int i;
	gliss_mem_write(mem, addr, buf, size);

	/* byte test */
	for(i = 0; i < size; i++)
		if(gliss_mem_read8(mem, addr + i) != buf[i]) {
			fprintf(stderr, "byte read error at %p: %08x := %08x\n",
				(void *)(addr + i),
				gliss_mem_read8(mem, addr + i),
				buf[i]);
			return 0;
		}

	/* block test */
	{
		uint8_t b2[size];
		gliss_mem_read(mem, addr, b2, size);
		for(i = 0; i < size; i++)
			if(b2[i] != buf[i]) {
				fprintf(stderr, "buffer read error at %p\n", (void *)(addr + i));
				return 0;
			}
	}

	/* all is fine */
	return 1;
}

int main(void) {
	uint8_t u8;
	uint16_t u16;
	uint32_t u32;
	uint64_t u64;
	float f;
	double d;
	gliss_memory_t *mem;
	mem = gliss_mem_new();
	assert(mem);

	/* simple read / write */
	gliss_mem_write8(mem, 0x200, 0x12);
	u8 = gliss_mem_read8(mem, 0x200);
	assert(u8 == 0x12);

	gliss_mem_write16(mem, 0x200, 0x1234);
	u16 = gliss_mem_read16(mem, 0x200);
	assert(u16 == 0x1234);

	gliss_mem_write32(mem, 0x200, 0x12345678);
	u32 = gliss_mem_read32(mem, 0x200);
	assert(u32 == 0x12345678);

	gliss_mem_write64(mem, 0x200, 0x123456789abcdef0LL);
	u64 = gliss_mem_read64(mem, 0x200);
	assert(u64 == 0x123456789abcdef0LL);

	gliss_mem_writef(mem, 0x200, M_PI);
	f = gliss_mem_readf(mem, 0x200);
	assert(f == (float)(M_PI));

	gliss_mem_writed(mem, 0x200, M_PI);
	d = gliss_mem_readd(mem, 0x200);
	assert(d == (double)(M_PI));

	/* odd read-write */
	gliss_mem_write8(mem, 0x201, 0x12);
	u8 = gliss_mem_read8(mem, 0x201);
	assert(u8 == 0x12);

	gliss_mem_write16(mem, 0x201, 0x1234);
	u16 = gliss_mem_read16(mem, 0x201);
	assert(u16 == 0x1234);

	gliss_mem_write32(mem, 0x201, 0x12345678);
	u32 = gliss_mem_read32(mem, 0x201);
	assert(u32 == 0x12345678);

	gliss_mem_write64(mem, 0x201, 0x123456789abcdef0LL);
	u64 = gliss_mem_read64(mem, 0x201);
	assert(u64 == 0x123456789abcdef0LL);

	gliss_mem_writef(mem, 0x201, M_PI);
	f = gliss_mem_readf(mem, 0x201);
	assert(f == (float)(M_PI));

	gliss_mem_writed(mem, 0x201, M_PI);
	d = gliss_mem_readd(mem, 0x201);
	assert(d == (double)(M_PI));

	/* inter-page read / write */
	gliss_mem_write8(mem, PAGE, 0x12);
	u8 = gliss_mem_read8(mem, PAGE);
	assert(u8 == 0x12);

	gliss_mem_write16(mem, PAGE - 1, 0x1234);
	u16 = gliss_mem_read16(mem, PAGE - 1);
	assert(u16 == 0x1234);

	gliss_mem_write32(mem, PAGE - 2, 0x12345678);
	u32 = gliss_mem_read32(mem, PAGE - 2);
	assert(u32 == 0x12345678);

	gliss_mem_write32(mem, PAGE - 1, 0x12345678);
	u32 = gliss_mem_read32(mem, PAGE - 1);
	assert(u32 == 0x12345678);

	gliss_mem_write32(mem, PAGE - 3, 0x12345678);
	u32 = gliss_mem_read32(mem, PAGE - 3);
	assert(u32 == 0x12345678);

	gliss_mem_write64(mem, PAGE - 4, 0x123456789abcdef0LL);
	u64 = gliss_mem_read64(mem, PAGE - 4);
	assert(u64 == 0x123456789abcdef0LL);

	gliss_mem_writef(mem, PAGE - 2, M_PI);
	f = gliss_mem_readf(mem, PAGE - 2);
	assert(f == (float)(M_PI));

	gliss_mem_writed(mem, PAGE - 4, M_PI);
	d = gliss_mem_readd(mem, PAGE - 4);
	assert(d == (double)(M_PI));

	/* block read / write */
	{
		char buf[PAGE * 4];
		int i;
		for(i = 0; i < PAGE * 4; i++)
			buf[i] = random();

		assert(check_block(mem, buf, 4, PAGE / 2));
		assert(check_block(mem, buf, 0, PAGE));
		assert(check_block(mem, buf, PAGE / 2, PAGE));
		assert(check_block(mem, buf, PAGE / 2, 3 * PAGE));
	}

	gliss_mem_delete(mem);
	puts("SUCCESS: all is fine !");
	return 0;
}

