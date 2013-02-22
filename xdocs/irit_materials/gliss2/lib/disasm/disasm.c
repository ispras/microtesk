/*
 * $Id: disasm.c,v 1.1 2009/02/25 17:30:25 casse Exp $
 * Copyright (c) 2010, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of GLISS V2.
 *
 * GLISS V2 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GLISS V2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GLISS V2; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

#include <stdlib.h>
#include <stdio.h>
#include <gliss/api.h>
#include <gliss/loader.h>

typedef struct list_entry_t
{
	const char *name;
	gliss_address_t addr;
	struct list_entry_t *next;
} list_entry_t;

void add_to_list(list_entry_t **m, const char *n, gliss_address_t a)
{
	list_entry_t *e = (list_entry_t *)malloc(sizeof(list_entry_t));
	if (e == 0)
	{
		fprintf(stderr, "ERROR: malloc failed\n");
	}
	e->name = n;
	e->addr = a;
	e->next = 0;

	if (*m == 0)
	{
		*m = e;
		return;
	}

	list_entry_t *t1 = *m;
	list_entry_t *t2 = 0;

	while (t1)
	{
		if (a >= t1->addr)
		{
			if (t1->next)
			{
				if (t1->next->addr > a)
				{
					/* we found the right place */
					t2 = t1->next;
					t1->next = e;
					e->next = t2;
					break;
				}
				else
					/* let's see the next one */
					t1 = t1->next;
			}
			else
			{
				/* insertion in end of list */
				t1->next = e;
				break;
			}
		}
		else
		{
			/* this case should occur only when testing the first entry of a list */
			*m = e;
			e->next = t1;
			break;
		}
	}
}

void print_list(list_entry_t *l)
{
	printf("printing list\n");

	list_entry_t *e = l;
	while (e)
	{
		printf("\t\"%s\"\t%08X\n", e->name, e->addr);
		if (e->next)
			e = e->next;
		else
			break;
	}
	printf("end list.\n");
}

/**
 * Get the label name associated with an address
 * @param	m	the sorted list to search within
 * @para	addr	the address whose label (if any) is wanted
 * @param	name	will point to the name if a label exists, NULL otherwise
 * @return	0 if no label exists for the given address, non zero otherwise
*/
int get_label_from_list(list_entry_t *m, gliss_address_t addr, const char **name)
{
	list_entry_t *e = m;
	while (e)
	{
		if (e->addr > addr)
		{
			*name = 0;
			return 0;
		}

		if (e->addr == addr)
		{
			*name = e->name;
			return 1;
		}

		if (e->next)
			e = e->next;
		else
			break;
	}
	return 0;
}

void destroy_list(list_entry_t *m)
{
	if (m == 0)
		return;
	list_entry_t *t1 = m;
	list_entry_t *t2 = 0;
	while (t1->next)
	{
		t2 = t1;
		t1 = t1->next;
		free(t2);
	}
	free(t1);
}

int main(int argc, char **argv) {
	gliss_platform_t *pf;
	int s_it;
	/*Elf32_Shdr *s;*/
	gliss_loader_sect_t *s_tab;
	int sym_it;
	/*Elf32_Sym *sym;*/
	int nb_sect_disasm = 0;
	gliss_loader_t *loader;
	int max_size = 0, i;


	/* test arguments */
	if(argc != 2) {
		fprintf(stderr, "ERROR: one argument required: the simulated program !\n");
		return 1;
	}

	/* we need a loader alone for sections */
	loader = gliss_loader_open(argv[1]);
	if (loader == NULL)
	{
		fprintf(stderr, "ERROR: cannot load the given executable : %s.\n", argv[1]);
		return 2;
	}

	printf("found %d sections in the executable %s\n", gliss_loader_count_sects(loader)-1, argv[1]);
	/*s_tab = (Elf32_Shdr*)malloc(gliss_loader_count_sects(loader) * sizeof(Elf32_Shdr));
	s = gliss_loader_first_sect(loader, &s_it);
	while (s_it >= 0)*/
	s_tab = (gliss_loader_sect_t *)malloc(gliss_loader_count_sects(loader) * sizeof(gliss_loader_sect_t));
	for(s_it = 0; s_it < gliss_loader_count_sects(loader); s_it++)
	{
		gliss_loader_sect_t data;
		gliss_loader_sect(loader, s_it, &data);
		if(data.type == GLISS_LOADER_SECT_TEXT)
		{
			s_tab[nb_sect_disasm++] = data;
			printf("[X]");
		}
		printf("\t%20s\ttype:%08X\taddr:%08X\tsize:%08X\n", data.name, data.type, data.addr, data.size);
	}
	printf("found %d sections to disasemble\n", nb_sect_disasm);

	printf("\nfound %d symbols in the executable %s\n", gliss_loader_count_syms(loader)-1, argv[1]);
	list_entry_t *list_labels = 0;
	/*sym = gliss_loader_first_sym(loader, &sym_it);*/
	for(sym_it = 0; sym_it < gliss_loader_count_syms(loader); sym_it++)
	{
		gliss_loader_sym_t data;
		gliss_loader_sym(loader, sym_it, &data);
		if(data.type == GLISS_LOADER_SYM_CODE || data.type == GLISS_LOADER_SYM_DATA)
		{
			printf("[L]");
			add_to_list(&list_labels, data.name, data.value);
		}
		printf("\t%20s\tvalue:%08X\tsize:%08X\tinfo:%08X\tshndx:%08X\n", data.name, data.value, data.size, data.type, data.sect);
	}

	/* create the platform */
	pf = gliss_new_platform();
	if(pf == NULL) {
		fprintf(stderr, "ERROR: cannot create the platform.");
		destroy_list(list_labels);
		return 1;
	}

	/* load it */
	gliss_loader_load(loader, pf);
	/* CAUTION: C99 valid declarations, BUT C89 invalid */
	int i_sect;

	gliss_decoder_t *d = gliss_new_decoder(pf);
	/* multi iss part, TODO: improve */
	gliss_state_t *state = gliss_new_state(pf);
	/* not really useful as select condition for instr set will never change as we don't execute here,
	 * changing instr set should be done manually by manipulating state */
	gliss_set_cond_state(d, state);

	/* compute instruction max size */
	for(i = 1; i < GLISS_TOP; i++) {
		int size = gliss_get_inst_size_from_id(i) / 8;
		if(size > max_size)
			max_size = size;
	}

	/* disassemble the sections */
	for (i_sect = 0; i_sect<nb_sect_disasm; i_sect++)
	{
		gliss_address_t adr_start = s_tab[i_sect].addr;
		gliss_address_t adr_end = s_tab[i_sect].addr + s_tab[i_sect].size;

		printf("\ndisasm new section, addr=%08X, size=%08X\n", s_tab[i_sect].addr, s_tab[i_sect].size);

		while (adr_start < adr_end) {
			int size;
			char buff[100];
			gliss_inst_t *inst = gliss_decode(d, adr_start);
			gliss_disasm(buff, inst);
			/*uint32_t code = gliss_mem_read32(gliss_get_memory(pf, 0), adr_start);*/
			const char *n;
			if (get_label_from_list(list_labels, adr_start, &n))
				printf("\n%08X <%s>\n", adr_start, n);
			printf("%08X:\t", adr_start);
			size = gliss_get_inst_size(inst) / 8;
			for(i = 0; i < max_size; i++) {
				if(i < size)
					printf("%02X", gliss_mem_read8(gliss_get_memory(pf, 0), adr_start + i));
				else
					fputs("  ", stdout);
			}
			printf("\t%s\n", buff);
			/* inst size is given in bit, we want it in byte */
			adr_start += size;
		}
	}

	gliss_delete_decoder(d);
	gliss_unlock_platform(pf);
	destroy_list(list_labels);

	return 0;
}
