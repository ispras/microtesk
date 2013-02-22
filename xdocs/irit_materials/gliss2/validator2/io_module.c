#include "io_module.h"
#include "internal.h"
#include <stdlib.h>
#include <string.h>


uint8_t gdb_mem_read8(uint32_t addr)
{
	char buf[50];
	char answer[200];

	sprintf(buf, "-data-read-memory 0X%08X x 2 1 1\n", addr);
	send_gdb_cmd(buf, answer, 0);
	wait_for_gdb_output(answer, 0);
	match_gdb_output(answer, "^done", IS_ERROR, "When trying to read memory, ");

	/* now let's find ''memory=[{addr="...",data=["<what we want>"]}]'' */
	/* we just have to go after '',data=["'' and read the data */
	char *ptr = strstr(answer, ",data=[\"");
	ptr += 7;
	uint8_t res = strtoul(ptr, 0, 0) & 0xFF;
	return res;
	/* we should better read 32 bits and cut the result to avoid garbage result */
}


uint16_t gdb_mem_read16(uint32_t addr)
{
	char buf[50];
	char answer[200];

	sprintf(buf, "-data-read-memory 0X%08X x 2 1 1\n", addr);
	send_gdb_cmd(buf, answer, 0);
	wait_for_gdb_output(answer, 0);
	match_gdb_output(answer, "^done", IS_ERROR, "When trying to read memory, ");

	/* now let's find ''memory=[{addr="...",data=["<what we want>"]}]'' */
	/* we just have to go after '',data=["'' and read the data */
	char *ptr = strstr(answer, ",data=[\"");
	ptr += 7;
	uint16_t res = strtoul(ptr, 0, 0) & 0xFFFF;
	return res;
	/* we should better read 32 bits and cut the result to avoid garbage result */
}


uint32_t gdb_mem_read32(uint32_t addr)
{
	char buf[50];
	char answer[200];
	sprintf(buf, "-data-read-memory 0X%08X x 4 1 1\n", addr);
	send_gdb_cmd(buf, answer, 0);
	match_gdb_output(answer, "^done", IS_ERROR, "When trying to read memory, ");
	/* now let's find ''memory=[{addr="...",data=["<what we want>"]}]'' */
	/* we just have to go after '',data=["'' and read the data */
	char *ptr = strstr(answer, "data=[\"");
	ptr += 7;
	uint32_t res = strtoul(ptr, 0, 0);
	return res;
}


uint64_t gdb_mem_read64(uint32_t addr)
{
	/*char buf[50];
	char answer[200];

	sprintf(buf, "-data-read-memory 0X%08X x 8 1 1\n", addr);
	send_gdb_cmd(buf, answer, 0);
	wait_for_gdb_output(answer);
	match_gdb_output(answer, "^done", IS_ERROR, "When trying to read memory, ");*/

	/* now let's find ''memory=[{addr="...",data=["<what we want>"]}]'' */
	/* we just have to go after '',data=["'' and read the data */
	/*char *ptr = strstr(answer, ",data=[\"");
	uint64_t res;
	read_gdb_output_register_value_u64(answer, &res);
	return res;*/
	
	/* we should better read 32 bits twice to avoid garbage result */
	uint64_t res = gdb_mem_read32(addr);
	return (res << 32) | gdb_mem_read32(addr + 4);
}


void gdb_mem_readX(uint32_t addr, void *dest)
{
/*	char buf[50];
	char answer[200]; */
	
	/* it seems like only 32 bit accesses with gdb and tsim  */
	/* returns a correct result, smaller or bigger accesses returns garbage. */
	/* it is probably because IO registers are, most of the time, 32 bit long */
	/* so let's slice the access in 32 bit blocks, except the eventual remainder */

}


void gdb_callback(leon_address_t addr, int size, void *data, int type_access)
{
	uint8_t *ptr8;
	uint16_t *ptr16;
	uint32_t *ptr32;
	uint64_t *ptr64;

	if (type_access == LEON_MEM_READ)
	{
		/* read the real data from GDB's memory */
		switch (size)
		{
			case 1:
				ptr8 = (uint8_t *)data;
				*ptr8 = gdb_mem_read8(addr);
				break;
			case 2:
				ptr16 = (uint16_t *)data;
				*ptr16 = gdb_mem_read16(addr);
				break;
			case 4:
				ptr32 = (uint32_t *)data;
				*ptr32 = gdb_mem_read32(addr);
				break;
			case 8:
				ptr64 = (uint64_t *)data;
				*ptr64 = gdb_mem_read64(addr);
				break;
			default:
				/* read as 32 bit blocks if aligned, remainder read 8 bit by 8 bit */
				/* read as 8 bit blocks if not aligned */
				if ((uint32_t)data % 4)
				{
					ptr8 = (uint8_t *)data;
					int i;
					for (i = 0; i < size; i++)
						ptr8[i] = gdb_mem_read8(addr + i);
				}
				else
				{
					ptr32 = (uint32_t *)data;
					int nb_block = size / 32;
					int rem = size % 32;
					ptr8 = (uint8_t *)((uint32_t)data + (nb_block << 2));
					uint32_t addr_rem = addr + (nb_block << 2);
					int i;
					for (i = 0; i < nb_block; i++)
						ptr32[i] = gdb_mem_read32(addr + i);
					for (i = 0; i < rem; i++)
						ptr8[i] = gdb_mem_read8(addr_rem + i);
				}
		}
	}
	else
	{
		/* if write access: nothing to do here */
	}
}

extern uint32_t gliss_pc;

void debug_callback(leon_address_t addr, int size, void *data, int type_access)
{
	uint8_t *ptr8;
	uint16_t *ptr16;
	uint32_t *ptr32;
	uint64_t *ptr64;
	leon_memory_t *mem = leon_get_memory(platform, LEON_MAIN_MEMORY);

	if (type_access == LEON_MEM_READ) {
		switch (size) {
		case 1:
			ptr8 = (uint8_t *)data;
			printf("debug-mem(%08X)R, [%08X] = %02X\n", gliss_pc, addr, PROC(_mem_read8)(mem, addr));
			*ptr8 = PROC(_mem_read8)(mem, addr);
			break;
		case 2:
			ptr16 = (uint16_t *)data;
			printf("debug-mem(%08X)R, [%08X] = %04X\n", gliss_pc, addr, PROC(_mem_read16)(mem, addr));
			*ptr16 = PROC(_mem_read16)(mem, addr);
			break;
		case 4:
			ptr32 = (uint32_t *)data;
			printf("debug-mem(%08X)R, [%08X] = %08X\n", gliss_pc, addr, PROC(_mem_read32)(mem, addr));
			*ptr32 = PROC(_mem_read32)(mem, addr);
			break;
		case 8:
			ptr64 = (uint64_t *)data;
			printf("debug-mem(%08X)R, [%08X] = %016llX\n", gliss_pc, addr, PROC(_mem_read64)(mem, addr));
			*ptr64 = PROC(_mem_read64)(mem, addr);
			break;
		default:
			ptr8 = (uint8_t *)data;
			int i;
			printf("debug-mem(%08X)R, [%08X]:%X =\n", gliss_pc, addr, size);
			for (i = 0; i < size; i++) {
				ptr8[i] = PROC(_mem_read8)(mem, addr + i);
				printf("%02X", ptr8[i]);
				if ((i % 4) == 0)
					putchar('\n');
			}
		}
	}
	else {
		switch (size) {
		case 1:
			ptr8 = (uint8_t *)data;
			printf("debug-mem(%08X)W, [%08X] = %02X\n", gliss_pc, addr, *ptr8);
			break;
		case 2:
			ptr16 = (uint16_t *)data;
			printf("debug-mem(%08X)W, [%08X] = %04X\n", gliss_pc, addr, *ptr16);
			break;
		case 4:
			ptr32 = (uint32_t *)data;
			printf("debug-mem(%08X)W, [%08X] = %08X\n", gliss_pc, addr, *ptr32);
			break;
		case 8:
			ptr64 = (uint64_t *)data;
			printf("debug-mem(%08X)W, [%08X] = %016llX\n", gliss_pc, addr, *ptr64);
			break;
		default:
			ptr8 = (uint8_t *)data;
			int i;
			printf("debug-mem(%08X)W, [%08X]:%X =\n", gliss_pc, addr, size);
			for (i = 0; i < size; i++) {
				printf("%02X", ptr8[i]);
				if ((i % 4) == 0)
					putchar('\n');
			}
		}
	}
}
