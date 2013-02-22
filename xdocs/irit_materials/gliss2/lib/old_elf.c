/*
 *	$Id: old_elf.c,v 1.12 2009/11/26 09:01:17 casse Exp $
 *	old_elf module interface
 *
 *	This file is part of OTAWA
 *	Copyright (c) 2010, IRIT UPS.
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

#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <gliss/loader.h>


#ifndef NDEBUG
#	define assertp(c, m)	\
		if(!(c)) { \
			fprintf(stderr, "assertion failure %s:%d: %s", __FILE__, __LINE__, m); \
			abort(); }
#	define TRACE 	/*fprintf(stderr, "%s:%d\n", __FILE__, __LINE__)*/
#else
#	define assertp(c, m)
#	define TRACE
#endif

#include <gliss/mem.h>
#if defined(GLISS_VFAST_MEM)

#   include <gliss/config.h>
#   define little	0
#   define big		1

#   ifndef TARGET_ENDIANNESS
#   	error "TARGET_ENDIANNESS must be defined !"
#   endif

#   ifndef HOST_ENDIANNESS
#   	error "HOST_ENDIANNESS must be defined !"
#   endif

#endif

/* ELF definitions */
typedef uint32_t Elf32_Addr;
typedef uint16_t Elf32_Half;
typedef uint32_t Elf32_Off;
typedef uint16_t Elf32_Section;
typedef uint32_t Elf32_Word;

#define EI_MAG0		0		/* File identification byte 0 index */
#define ELFMAG0		0x7f		/* Magic number byte 0 */

#define EI_MAG1		1		/* File identification byte 1 index */
#define ELFMAG1		'E'		/* Magic number byte 1 */

#define EI_MAG2		2		/* File identification byte 2 index */
#define ELFMAG2		'L'		/* Magic number byte 2 */

#define EI_MAG3		3		/* File identification byte 3 index */
#define ELFMAG3		'F'		/* Magic number byte 3 */

/* Conglomeration of the identification bytes, for easy testing as a word.  */
#define	ELFMAG		"\177ELF"
#define	SELFMAG		4

#define EI_CLASS	4		/* File class byte index */
#define ELFCLASSNONE	0		/* Invalid class */
#define ELFCLASS32	1		/* 32-bit objects */
#define ELFCLASS64	2		/* 64-bit objects */

#define EI_DATA		5		/* Data encoding byte index */
#define ELFDATANONE	0		/* Invalid data encoding */
#define ELFDATA2LSB	1		/* 2's complement, little endian */
#define ELFDATA2MSB	2		/* 2's complement, big endian */

#define EI_VERSION	6		/* File version byte index */
					/* Value must be EV_CURRENT */

#define EI_PAD		7		/* Byte index of padding bytes */

/* Legal values for e_type (object file type).  */

#define ET_NONE		0		/* No file type */
#define ET_REL		1		/* Relocatable file */
#define ET_EXEC		2		/* Executable file */
#define ET_DYN		3		/* Shared object file */
#define ET_CORE		4		/* Core file */
#define	ET_NUM		5		/* Number of defined types.  */
#define ET_LOPROC	0xff00		/* Processor-specific */
#define ET_HIPROC	0xffff		/* Processor-specific */

#define SHT_NULL	0		/* Section header table entry unused */
#define SHT_PROGBITS	1		/* Program data */
#define SHT_SYMTAB	2		/* Symbol table */
#define SHT_STRTAB	3		/* String table */
#define SHT_RELA	4		/* Relocation entries with addends */
#define SHT_HASH	5		/* Symbol hash table */
#define SHT_DYNAMIC	6		/* Dynamic linking information */
#define SHT_NOTE	7		/* Notes */
#define SHT_NOBITS	8		/* Program space with no data (bss) */
#define SHT_REL		9		/* Relocation entries, no addends */
#define SHT_SHLIB	10		/* Reserved */
#define SHT_DYNSYM	11		/* Dynamic linker symbol table */
#define	SHT_NUM		12		/* Number of defined types.  */

#define SHF_WRITE	(1 << 0)	/* Writable */
#define SHF_ALLOC	(1 << 1)	/* Occupies memory during execution */
#define SHF_EXECINSTR	(1 << 2)	/* Executable */
#define SHF_MASKPROC	0xf0000000	/* Processor-specific */

typedef struct
{
  Elf32_Word	sh_name;		/* Section name (string tbl index) */
  Elf32_Word	sh_type;		/* Section type */
  Elf32_Word	sh_flags;		/* Section flags */
  Elf32_Addr	sh_addr;		/* Section virtual addr at execution */
  Elf32_Off	sh_offset;		/* Section file offset */
  Elf32_Word	sh_size;		/* Section size in bytes */
  Elf32_Word	sh_link;		/* Link to another section */
  Elf32_Word	sh_info;		/* Additional section information */
  Elf32_Word	sh_addralign;		/* Section alignment */
  Elf32_Word	sh_entsize;		/* Entry size if section holds table */
} Elf32_Shdr;

typedef struct
{
  Elf32_Word	st_name;		/* Symbol name (string tbl index) */
  Elf32_Addr	st_value;		/* Symbol value */
  Elf32_Word	st_size;		/* Symbol size */
  unsigned char	st_info;		/* Symbol type and binding */
  unsigned char	st_other;		/* No defined meaning, 0 */
  Elf32_Section	st_shndx;		/* Section index */
} Elf32_Sym;

#define PT_LOAD	1
typedef struct
{
  Elf32_Word	p_type;			/* Segment type */
  Elf32_Off	p_offset;		/* Segment file offset */
  Elf32_Addr	p_vaddr;		/* Segment virtual address */
  Elf32_Addr	p_paddr;		/* Segment physical address */
  Elf32_Word	p_filesz;		/* Segment size in file */
  Elf32_Word	p_memsz;		/* Segment size in memory */
  Elf32_Word	p_flags;		/* Segment flags */
  Elf32_Word	p_align;		/* Segment alignment */
} Elf32_Phdr;

#define EI_NIDENT (16)
typedef struct
{
  unsigned char	e_ident[EI_NIDENT];	/* Magic number and other info */
  Elf32_Half	e_type;			/* Object file type */
  Elf32_Half	e_machine;		/* Architecture */
  Elf32_Word	e_version;		/* Object file version */
  Elf32_Addr	e_entry;		/* Entry point virtual address */
  Elf32_Off	e_phoff;		/* Program header table file offset */
  Elf32_Off	e_shoff;		/* Section header table file offset */
  Elf32_Word	e_flags;		/* Processor-specific flags */
  Elf32_Half	e_ehsize;		/* ELF header size in bytes */
  Elf32_Half	e_phentsize;		/* Program header table entry size */
  Elf32_Half	e_phnum;		/* Program header table entry count */
  Elf32_Half	e_shentsize;		/* Section header table entry size */
  Elf32_Half	e_shnum;		/* Section header table entry count */
  Elf32_Half	e_shstrndx;		/* Section header string table index */
} Elf32_Ehdr;

/* auxiliary vector types */
#define AT_NULL		0
#define AT_IGNORE	1
#define AT_EXECFD	2
#define AT_PHDR		3
#define AT_PHENT	4
#define AT_PHNUM	5
#define AT_PAGESZ	6
#define AT_BASE		7
#define AT_FLAGS	8
#define AT_ENTRY	9
#define AT_DCACHEBSIZE	10
#define AT_ICACHEBSIZE	11
#define AT_UCACHEBSIZE	12


/*** symbol constants ***/
#define ELF32_ST_BIND(i)	((i)>>4)
#define ELF32_ST_TYPE(i)	((i)&0xf)
#define ELF32_ST_INFO(b,t)	(((b)<<4)+((t)&0xf))

#define STB_LOCAL   0
#define STB_GLOBAL  1
#define STB_WEAK    2
#define STB_LOPROC 13
#define STB_HIPROC 15

#define STT_NOTYPE   0
#define STT_OBJECT   1
#define STT_FUNC     2
#define STT_SECTION  3
#define STT_FILE     4
#define STT_LOPROC  13
#define STT_HIPROC  15


/*********************** ELF loader ********************************/

typedef struct tables {
	int32_t sechdr_tbl_size;
	Elf32_Shdr *sec_header_tbl;
	int32_t secnmtbl_ndx;
	char *sec_name_tbl;

	int32_t symtbl_ndx;
	Elf32_Sym *sym_tbl;
	char *symstr_tbl;

	int32_t dysymtbl_ndx;
	Elf32_Sym *dysym_tbl;
	char *dystr_tbl;

	int32_t hashtbl_ndx;
	Elf32_Word *hash_tbl;
	Elf32_Sym *hashsym_tbl;

        int32_t     pgm_hdr_tbl_size;
        Elf32_Phdr *pgm_header_tbl;
} Elf_Tables;

struct text_secs{
	char name[20];
	uint32_t offset;
	uint32_t address;
	uint32_t size;
        uint8_t *bytes;
	struct text_secs *next;
};

struct text_info{
	uint16_t txt_index;
	uint32_t address;
	uint32_t size;
	uint32_t txt_addr;
	uint32_t txt_size;
	uint8_t *bytes;
	struct text_secs *secs;
};

struct data_secs{
	char name[20];
	uint32_t offset;
	uint32_t address;
	uint32_t size;
	uint32_t type;
	uint32_t flags;
	uint8_t *bytes;
	struct data_secs *next;
};

struct data_info{
	uint32_t address;
	uint32_t size;
	struct data_secs *secs;
};


/* Global data */
static Elf_Tables Initial_Tables = {
	0,
	NULL,
	-1,
	NULL,
	-1,
	NULL,
	NULL,
	-1,
	NULL,
	NULL,
	-1,
	NULL,
	NULL,
	0,
	NULL
};
static Elf_Tables Tables;
static struct text_info Text;
static struct data_info Data;
static Elf32_Ehdr Ehdr;
static int Is_Elf_Little = 0;

static int is_host_little(void) {
    uint32_t x;
    x=0xDEADBEEF;
    return ( ((unsigned char) x)==0xEF );
}

static int16_t ConvertByte2( int16_t Word ) {
	union{
		unsigned char c[2];
		int16_t i;
	} w1,w2;
	w1.i = Word;
	w2.c[0] = w1.c[1];
	w2.c[1] = w1.c[0];
	return (w2.i);
}

static int32_t ConvertByte4( int32_t Dword ){
	union{
		unsigned char c[4];
		int32_t i;
	} dw1,dw2;
	dw1.i = Dword;
	dw2.c[0] = dw1.c[3];
	dw2.c[1] = dw1.c[2];
	dw2.c[2] = dw1.c[1];
	dw2.c[3] = dw1.c[0];
	return (dw2.i);
}

static void ConvertElfHeader(Elf32_Ehdr *Ehdr) {
	Ehdr->e_type = ConvertByte2(Ehdr->e_type);
	Ehdr->e_machine = ConvertByte2(Ehdr->e_machine);
	Ehdr->e_version = ConvertByte4(Ehdr->e_version);
	Ehdr->e_entry = ConvertByte4(Ehdr->e_entry);
	Ehdr->e_phoff = ConvertByte4(Ehdr->e_phoff);
	Ehdr->e_shoff = ConvertByte4(Ehdr->e_shoff);
	Ehdr->e_flags = ConvertByte4(Ehdr->e_flags);
	Ehdr->e_ehsize = ConvertByte2(Ehdr->e_ehsize);
	Ehdr->e_phentsize = ConvertByte2(Ehdr->e_phentsize);
	Ehdr->e_phnum = ConvertByte2(Ehdr->e_phnum);
	Ehdr->e_shentsize = ConvertByte2(Ehdr->e_shentsize);
	Ehdr->e_shnum = ConvertByte2(Ehdr->e_shnum);
	Ehdr->e_shstrndx = ConvertByte2(Ehdr->e_shstrndx);
}

static int ElfReadHeader(int fd, Elf32_Ehdr *Ehdr){
	int foffset;
	TRACE;
	foffset = lseek(fd, 0, SEEK_CUR);
	lseek(fd, 0, SEEK_SET);
	if(read(fd, Ehdr, sizeof(Elf32_Ehdr)) != sizeof(Elf32_Ehdr))
		return -1;
	if(bcmp(Ehdr->e_ident, ELFMAG, 4)) {
		errno = EBADF;
		return -1;
	}
    if(Ehdr->e_ident[EI_DATA] == 1)
		Is_Elf_Little = 1;
    else if(Ehdr->e_ident[EI_DATA] == 2)
		Is_Elf_Little = 0;
    else {
		errno = EBADF;
		return -1;
	}
    if(Ehdr->e_ident[EI_CLASS] == ELFCLASS64) {
		errno = EFBIG;
		return -1;
	}
	lseek(fd, foffset, SEEK_SET);
	if (is_host_little() != Is_Elf_Little)
		ConvertElfHeader(Ehdr);
	return 0;
}

static int ElfCheckExec(const Elf32_Ehdr *Ehdr) {
	TRACE;
	if(Ehdr->e_type == ET_EXEC)
		return 0;
	else {
		errno = EBADF;
		return -1;
	}
}

static void ConvertPgmHeader(Elf32_Phdr *Ephdr) {
	Ephdr->p_type = ConvertByte4(Ephdr->p_type);
	Ephdr->p_offset = ConvertByte4(Ephdr->p_offset);
	Ephdr->p_vaddr = ConvertByte4(Ephdr->p_vaddr);
	Ephdr->p_paddr = ConvertByte4(Ephdr->p_paddr);
	Ephdr->p_filesz = ConvertByte4(Ephdr->p_filesz);
	Ephdr->p_memsz = ConvertByte4(Ephdr->p_memsz);
	Ephdr->p_flags = ConvertByte4(Ephdr->p_flags);
	Ephdr->p_align = ConvertByte4(Ephdr->p_align);
}

static int ElfReadPgmHdrTbl(int fd,const Elf32_Ehdr *Ehdr) {
	int32_t i;
	TRACE;
	if(Ehdr->e_phoff == 0) {
		errno = EBADF;
		return -1;
	}
    lseek(fd, Ehdr->e_phoff,SEEK_SET);
    Tables.pgm_hdr_tbl_size = Ehdr->e_phnum;
    Tables.pgm_header_tbl = (Elf32_Phdr *)malloc(Ehdr->e_phnum * sizeof(Elf32_Phdr));
    if(Tables.pgm_header_tbl == NULL) {
		errno = ENOMEM;
		return -1;
	}
	if(read(fd,Tables.pgm_header_tbl,(Ehdr->e_phnum*sizeof(Elf32_Phdr)))
	!= (Ehdr->e_phnum*sizeof(Elf32_Phdr))) {
		errno = EBADF;
		return -1;
	}
	if (is_host_little() != Is_Elf_Little)
    	for(i=0; i < Ehdr->e_phnum; ++i)
			ConvertPgmHeader(&Tables.pgm_header_tbl[i]);
   return 0;
}

static void ConvertSecHeader(Elf32_Shdr *Eshdr) {
	Eshdr->sh_name = ConvertByte4(Eshdr->sh_name);
	Eshdr->sh_type = ConvertByte4(Eshdr->sh_type);
	Eshdr->sh_flags = ConvertByte4(Eshdr->sh_flags);
	Eshdr->sh_addr = ConvertByte4(Eshdr->sh_addr);
	Eshdr->sh_offset = ConvertByte4(Eshdr->sh_offset);
	Eshdr->sh_size = ConvertByte4(Eshdr->sh_size);
	Eshdr->sh_link = ConvertByte4(Eshdr->sh_link);
	Eshdr->sh_info = ConvertByte4(Eshdr->sh_info);
	Eshdr->sh_addralign = ConvertByte4(Eshdr->sh_addralign);
	Eshdr->sh_entsize = ConvertByte4(Eshdr->sh_entsize);
}

static int ElfReadSecHdrTbl(int fd, const Elf32_Ehdr *Ehdr) {
	int32_t i, foffset;
	TRACE;
	if(Ehdr->e_shoff == 0) {
		errno = EBADF;
		return -1;
	}
	foffset = lseek(fd, 0, SEEK_CUR);
	lseek(fd, Ehdr->e_shoff,SEEK_SET);
	Tables.sechdr_tbl_size = Ehdr->e_shnum;
	Tables.sec_header_tbl = (Elf32_Shdr *)malloc(Ehdr->e_shnum * sizeof(Elf32_Shdr));
    if(Tables.sec_header_tbl == NULL) {
		errno = ENOMEM;
		return -1;
	}
	if(read(fd,Tables.sec_header_tbl,(Ehdr->e_shnum*sizeof(Elf32_Shdr)))
	!= (Ehdr->e_shnum*sizeof(Elf32_Shdr))) {
		errno = EBADF;
		return -1;
	}
	if (is_host_little() != Is_Elf_Little)
		for(i=0;i<Ehdr->e_shnum;++i)
			ConvertSecHeader(&Tables.sec_header_tbl[i]);
	lseek(fd,foffset,SEEK_SET);
	return 0;
}

static int ElfReadSecNameTbl(int fd, const Elf32_Ehdr *Ehdr) {
	int foffset;
	Elf32_Shdr Eshdr;
	TRACE;
	if(Ehdr->e_shoff == 0 || Tables.secnmtbl_ndx == 0) {
		errno = EBADF;
		return -1;
	}
	if(Tables.secnmtbl_ndx > 0)
		return 0;
	Tables.secnmtbl_ndx = Ehdr->e_shstrndx;
	foffset = lseek(fd, 0, SEEK_CUR);
	lseek(fd,Ehdr->e_shoff + Ehdr->e_shstrndx * Ehdr->e_shentsize, SEEK_SET);
    if(read(fd,&Eshdr, sizeof(Eshdr)) != sizeof(Eshdr)) {
		errno = EBADF;
		return -1;
	}
	if (is_host_little() != Is_Elf_Little)
		ConvertSecHeader(&Eshdr);
	Tables.sec_name_tbl = (char *)malloc(Eshdr.sh_size);
    if(Tables.sec_name_tbl == NULL) {
		errno = ENOMEM;
		return -1;
	}
	lseek(fd,Eshdr.sh_offset,SEEK_SET);
	if(read(fd,Tables.sec_name_tbl,Eshdr.sh_size) != Eshdr.sh_size) {
		errno = EBADF;
		return -1;
	}
	lseek(fd, foffset, SEEK_SET);
	return 0;
}

static void ConvertSymTblEnt(Elf32_Sym *Esym) {
	Esym->st_name = ConvertByte4(Esym->st_name);
	Esym->st_value = ConvertByte4(Esym->st_value);
	Esym->st_size = ConvertByte4(Esym->st_size);
	Esym->st_shndx = ConvertByte2(Esym->st_shndx);
}

static int ElfReadSymTbl(int fd, const Elf32_Ehdr *Ehdr) {
	int32_t i, j, foffset;
	TRACE;
	if(Ehdr->e_shoff == 0) {
		errno = EBADF;
		return -1;
	}
	if(Tables.symtbl_ndx == 0) {
		errno = EBADF;
		return -1;
	}
	if(Tables.symtbl_ndx > 0)
		return 0; /* already done */
	foffset = lseek(fd,0,SEEK_CUR);
	lseek(fd,Ehdr->e_shoff,SEEK_SET);
	for(i=0; i < Ehdr->e_shnum; ++i)
		if(Tables.sec_header_tbl[i].sh_type == SHT_SYMTAB)
			break;
    if(Ehdr->e_shnum == i) {
		errno = EBADF;
		return -1;
	}
	Tables.symtbl_ndx = i;
	lseek(fd,Tables.sec_header_tbl[i].sh_offset,SEEK_SET);
	Tables.sym_tbl = (Elf32_Sym *)malloc(Tables.sec_header_tbl[i].sh_size);
	if(Tables.sym_tbl == NULL) {
		errno = ENOMEM;
		return -1;
	}
    if(read(fd,Tables.sym_tbl,Tables.sec_header_tbl[i].sh_size)
	!= Tables.sec_header_tbl[i].sh_size) {
		errno = EBADF;
		return -1;
	}
	if(is_host_little() != Is_Elf_Little)
		for(j=0;j<(Tables.sec_header_tbl[i].sh_size/Tables.sec_header_tbl[i].sh_entsize);++j)
			ConvertSymTblEnt(&Tables.sym_tbl[j]);
	/* Got Symbol table now reading string table for it */
	i = Tables.sec_header_tbl[i].sh_link;
	Tables.symstr_tbl = (char *)malloc(Tables.sec_header_tbl[i].sh_size);
	if(Tables.symstr_tbl == NULL) {
		errno = ENOMEM;
		return -1;
	}
	lseek(fd,Tables.sec_header_tbl[i].sh_offset,SEEK_SET);
	if(read(fd,(char *)Tables.symstr_tbl,Tables.sec_header_tbl[i].sh_size)
	!= Tables.sec_header_tbl[i].sh_size) {
		errno = EBADF;
		return -1;
	}
	lseek(fd,foffset,SEEK_SET);
	return 0;
}

static int ElfReadTextSecs(int fd, const Elf32_Ehdr *Ehdr) {
	int32_t i,foffset;
	struct text_secs *txt_sec, **ptr, *ptr1;
	TRACE;
    if(Ehdr->e_shoff == 0) {
		errno = EBADF;
		return -1;
	}
	foffset = lseek(fd,0,SEEK_CUR);
	lseek(fd,Ehdr->e_shoff,SEEK_SET);

	TRACE;
	for(i=0; i<Ehdr->e_shnum; ++i) {
		if((Tables.sec_header_tbl[i].sh_type == SHT_PROGBITS)
		&& (Tables.sec_header_tbl[i].sh_flags & SHF_ALLOC)
		&& (Tables.sec_header_tbl[i].sh_flags & SHF_EXECINSTR)) {
			txt_sec = (struct text_secs *)malloc(sizeof(struct text_secs));
			if(txt_sec == NULL) {
				errno = ENOMEM;
				return -1;
			}
			strcpy(txt_sec->name,&Tables.sec_name_tbl[Tables.sec_header_tbl[i].sh_name]);
			if(!strcmp(txt_sec->name,".text"))
				Text.txt_index = i;
			txt_sec->offset = Tables.sec_header_tbl[i].sh_offset;
			txt_sec->address = Tables.sec_header_tbl[i].sh_addr;
			txt_sec->size = Tables.sec_header_tbl[i].sh_size;
			txt_sec->next = NULL;
			txt_sec->bytes= (uint8_t *)malloc(txt_sec->size *sizeof(uint8_t));
            if(txt_sec->bytes==NULL) {
				free(txt_sec);
				errno = ENOMEM;
				return -1;
			}
			lseek(fd,txt_sec->offset,SEEK_SET);
			if(read(fd,txt_sec->bytes,txt_sec->size) != txt_sec->size) {
				free(txt_sec->bytes);
				free(txt_sec);
				errno = EBADF;
				return -1;
			}
			/* set next ptr */
			ptr = &Text.secs;
			while(*ptr != NULL){
				if((*ptr)->address > txt_sec->address){
					txt_sec->next = *ptr;
					*ptr = txt_sec;
					break;
				}
				ptr = &((*ptr)->next);
			}
			if(*ptr == NULL)
				*ptr = txt_sec;
		}
	}
    if(Text.secs == NULL) {
		errno = EBADF;
		return -1;
	}

    /* ??? */
    TRACE;
	Text.address = Text.secs->address;
	ptr1 = Text.secs;
	while(ptr1->next != NULL)
        ptr1 = ptr1->next;
	Text.size = ptr1->address + ptr1->size - Text.address;
	Text.bytes = (uint8_t *)malloc(Text.size);
	if(Text.bytes == NULL) {
		errno = ENOMEM;
		return -1;
	}
	memset(Text.bytes,0,Text.size);

	TRACE;
	ptr1 = Text.secs;
	while(ptr1 != NULL){
		if(!strcmp(ptr1->name,".text")){
			Text.txt_addr = ptr1->address;
			Text.txt_size = ptr1->size;
		}
		lseek(fd,ptr1->offset,SEEK_SET);
		if(read(fd,&Text.bytes[ptr1->address-Text.address],ptr1->size)
		!= ptr1->size) {
			errno = EBADF;
			return -1;
		}
		ptr1 = ptr1->next;
	}
	lseek(fd,foffset,SEEK_SET);
	Text.txt_addr = Ehdr->e_entry; // modification par Tahiry l'entrée du programme est entry et non le debut du segment de prog

	TRACE;
    return 0;
}

static int ElfInsertDataSec(const Elf32_Shdr *hdr,int fd) {
	struct data_secs *data_sec,**ptr;
	data_sec = (struct data_secs *)malloc(sizeof(struct data_secs));
	if(data_sec == NULL) {
		errno = ENOMEM;
		return -1;
	}
	strcpy(data_sec->name,&Tables.sec_name_tbl[hdr->sh_name]);
	data_sec->offset = hdr->sh_offset;
	data_sec->address = hdr->sh_addr;
	data_sec->size = hdr->sh_size;
	data_sec->type = hdr->sh_type;
	data_sec->flags = hdr->sh_flags;
	data_sec->next = NULL;
	data_sec->bytes=(uint8_t *)malloc(data_sec->size);
    if(data_sec->bytes==NULL) {
		free(data_sec);
		errno = ENOMEM;
		return -1;
	}
    if(strcmp(data_sec->name,".bss") != 0
	&& strcmp(data_sec->name,".sbss")) {
		lseek(fd,data_sec->offset,SEEK_SET);
		if(read(fd,data_sec->bytes,data_sec->size)!=data_sec->size) {
			free(data_sec->bytes);
			free(data_sec);
			errno = EBADF;
			return -1;
		}
    }
	else
		memset(data_sec->bytes,0,data_sec->size);
	ptr = &Data.secs;
	while(*ptr != NULL){
		if((*ptr)->address > data_sec->address){
			data_sec->next = *ptr;
			*ptr = data_sec;
			break;
		}
		ptr = &((*ptr)->next);
	}
	if(*ptr == NULL)
		*ptr = data_sec;
	return 0;
}

static int ElfReadDataSecs(int fd, const Elf32_Ehdr *Ehdr) {
	int32_t i,foffset;
	foffset = lseek(fd,0,SEEK_CUR);
	for(i=0;i<Ehdr->e_shnum;++i){
		int res = 0;
		if(Tables.sec_header_tbl[i].sh_type == SHT_PROGBITS){
			if(Tables.sec_header_tbl[i].sh_flags == (SHF_ALLOC | SHF_WRITE))
				res = ElfInsertDataSec(&Tables.sec_header_tbl[i],fd);
			else if(Tables.sec_header_tbl[i].sh_flags == (SHF_ALLOC))
				res = ElfInsertDataSec(&Tables.sec_header_tbl[i],fd);
        }
		else if(Tables.sec_header_tbl[i].sh_type == SHT_NOBITS
		&& Tables.sec_header_tbl[i].sh_flags == (SHF_ALLOC | SHF_WRITE))
			res = ElfInsertDataSec(&Tables.sec_header_tbl[i],fd);
		if(res != 0)
			return -1;
    }
    if(Data.secs != NULL)
		Data.address = Data.secs->address;
	else
		Data.address = 0;
	lseek(fd,foffset,SEEK_SET);
    return 0;
}

static int ElfRead(int elf){
	if(ElfReadHeader(elf,&Ehdr) == 0
	&& ElfCheckExec(&Ehdr) == 0
	&& ElfReadPgmHdrTbl(elf,&Ehdr) == 0
	&& ElfReadSecHdrTbl(elf,&Ehdr) == 0
	&& ElfReadSecNameTbl(elf,&Ehdr) == 0
	&& ElfReadSymTbl(elf,&Ehdr) == 0
	&& ElfReadTextSecs(elf,&Ehdr) == 0
	&& ElfReadDataSecs(elf,&Ehdr) == 0)
		return 0;
	else
		return -1;
}

static void ElfCleanup(void) {
	struct text_secs *curt, *nextt;
	struct data_secs *curd, *nextd;

	/* free static tables */
	if(Tables.pgm_header_tbl != NULL) {
		free(Tables.pgm_header_tbl);
		Tables.pgm_header_tbl = NULL;
	}
	if(Tables.sec_header_tbl != NULL) {
		free(Tables.sec_header_tbl);
		Tables.sec_header_tbl = NULL;
	}
	if(Tables.sec_name_tbl != NULL) {
		free(Tables.sec_name_tbl);
		Tables.sec_name_tbl = NULL;
	}
	if(Tables.sym_tbl != NULL) {
		free(Tables.sym_tbl);
		Tables.sym_tbl = NULL;
	}
	if(Tables.symstr_tbl != NULL) {
		free(Tables.symstr_tbl);
		Tables.symstr_tbl = NULL;
	}
	if(Text.bytes != NULL) {
		free(Text.bytes);
		Text.bytes = NULL;
	}

	/* free text sections */
	for(curt = Text.secs; curt != NULL; curt = nextt) {
		nextt = curt->next;
		free(curt->bytes);
		free(curt);
	}
	Text.secs = NULL;

	/* free data sections */
	for(curd = Data.secs; curd != NULL; curd = nextd) {
		nextd = curd->next;
		free(curd->bytes);
		free(curd);
	}
	Data.secs = NULL;
}

static void ElfReset(void) {
	memcpy(&Tables, &Initial_Tables, sizeof(Tables));
	memset(&Text, 0, sizeof(Text));
	memset(&Data, 0, sizeof(Data));
	memset(&Ehdr, 0, sizeof(Ehdr));
	Is_Elf_Little = 0;
}


/*********************** loader interface ********************************/

/* types */
struct gliss_loader_t {
	Elf_Tables Tables;
	struct text_info Text;
	struct data_info Data;
	Elf32_Ehdr Ehdr;
	int Is_Elf_Little;
};

/**
 * Open an ELF file.
 * @param path	Path to the file.
 * @return		ELF handler or null if there is an error. Error code in errno.
 * 				Error code may any one about file opening and
 *				ENOMEM (for lack of memory) or EBADF (for ELF format error).
 */
gliss_loader_t *gliss_loader_open(const char *path) {
	gliss_loader_t *loader;
	int elf,res;
	assert(path);

	/* open the file */
	TRACE;
#	if defined(__WIN32) || defined(__WIN64)
		elf = open(path, O_RDONLY | O_BINARY);
#	else
		elf = open(path, O_RDONLY);
#	endif
	if(elf == -1)
		return NULL;

	/* allocate handler */
	TRACE;
	loader = (gliss_loader_t *)malloc(sizeof(gliss_loader_t));
	if(loader == NULL) {
		errno = ENOMEM;
		close(elf);
		//!!DEBUG!!
		printf("loader==NULL\n");
		return NULL;
	}

	/* load the ELF */
	TRACE;
	ElfReset();
	res = ElfRead(elf);
	assert(Text.secs != NULL);
	close(elf);
	if(res != 0) {
		//!!DEBUG!!
		printf("res!=0\n");
		ElfCleanup();
		free(loader);
		return NULL;
	}

	/* record data */
	TRACE;
	loader->Tables = Tables;
	loader->Text = Text;
	loader->Data = Data;
	loader->Ehdr = Ehdr;
	loader->Is_Elf_Little = Is_Elf_Little;

	/* !!TODO!! after data stored into loader,
	clean the globals of Elf_Tables (just a shallow clean,
	real clean will be done by loader when destroyed) */
	ElfReset();

	return loader;
}


/**
 * Close the given opened file.
 * @param loader	Loader to work on.
 */
void gliss_loader_close(gliss_loader_t *loader)
{
	assert(loader);
	/*gliss_loader_t *backup = malloc(sizeof(gliss_loader_t));*/

	/* backup globals */
	/*backup->Tables = Tables;
	backup->Text = Text;
	backup->Data = Data;
	backup->Ehdr = Ehdr;
	backup->Is_Elf_Little = Is_Elf_Little;*/

	/* replace globals by loader's data */
	Tables = loader->Tables;
	Text = loader->Text;
	Data = loader->Data;
	Ehdr = loader->Ehdr;
	Is_Elf_Little = loader->Is_Elf_Little;

	/* destroy loader's data */
	ElfCleanup();

	/* restore previous globals */
	/*Tables = backup->Tables;
	Text = backup->Text;
	Data = backup->Data;
	Ehdr = backup->Ehdr;
	Is_Elf_Little = backup->Is_Elf_Little;*/

	/* destroy loader */
	free(loader);
}


/**
 * Load the opened ELF program into the given platform (main memory chosen).
 * @param loader	program ELF loader
 * @param memory	memory to load in
 */
void gliss_loader_load(gliss_loader_t *loader, gliss_platform_t *pf) {
    struct data_secs* ptr;
    struct text_secs* ptr_tex;
    uint8_t*          rbuffer;
    unsigned int i;
	assert(loader->Text.secs != NULL);

	/* adapt the next line if you have an harvard mem archi */
	gliss_memory_t *memory = gliss_get_memory(pf, GLISS_MAIN_MEMORY);

	/* load text part */
	TRACE;
	ptr_tex = loader->Text.secs;
    while(ptr_tex != NULL)
    {
        TRACE;
        if(ptr_tex->size)
			gliss_mem_write(memory, ptr_tex->address, ptr_tex->bytes, ptr_tex->size);
		ptr_tex = ptr_tex->next;
	}

	/* load data part */
	TRACE;
	ptr = loader->Data.secs;
    while(ptr != NULL)
    {
		TRACE;
		if(ptr->size)
			gliss_mem_write(memory, ptr->address, ptr->bytes, ptr->size);
		ptr = ptr->next;
	}

	/* initialize BSS part */
#	ifdef GLISS_NOBITS_INIT
		for(i = 0; i < loader->Tables.sechdr_tbl_size; i++) {
			Elf32_Shdr *sec = &loader->Tables.sec_header_tbl[i];
			if(sec->sh_type == SHT_NOBITS) {
				int j;
				for(j = 0; j < sec->sh_size; j += sizeof(uint32_t))
					gliss_mem_write32(memory, sec->sh_addr + j, 0);
			}
		}
#	endif
}


/**
 * Get the address of the entry point of the program.
 * @param loader	Used loader.
 * @return			entry point address.
 */
gliss_address_t gliss_loader_start(gliss_loader_t *loader)
{
	assert(loader);
	return loader->Text.txt_addr;
}


/**
 * Get the number of sections in the executable.
 * @param loader	Current loader.
 * @return			Number of sections.
 */
int gliss_loader_count_sects(gliss_loader_t *loader)
{
	return loader->Tables.sechdr_tbl_size;
}


/**
 * Get information about a section.
 * @param loader	Current loader.
 * @param sect		Section index (starting from 0).
 * @param data		Data structure to store information in.
 */
void gliss_loader_sect(gliss_loader_t *loader, int sect, gliss_loader_sect_t *data) {
	Elf32_Shdr *s;

	/* get the section */
	assert(sect < loader->Tables.sechdr_tbl_size);
	s = &loader->Tables.sec_header_tbl[sect];

	/* fill address and size */
	data->addr = s->sh_addr;
	data->size = s->sh_size;

	/* get the type */
	switch(s->sh_type) {
	case SHT_PROGBITS:
		if(s->sh_flags & SHF_ALLOC) {
			if(s->sh_flags & SHF_EXECINSTR)
				data->type = GLISS_LOADER_SECT_TEXT;
			else
				data->type = GLISS_LOADER_SECT_DATA;
		}
		else
			data->type = GLISS_LOADER_SECT_BSS;
		break;
	default:
		data->type = GLISS_LOADER_SECT_UNKNOWN;
		break;
	}

	/* get the name */
	data->name = loader->Tables.sec_name_tbl + loader->Tables.sec_header_tbl[sect].sh_name;
}


/**
 * Get the number of symbols in the given loader.
 * @param loader	Current loader.
 * @return			Number of symbols.
 */
int gliss_loader_count_syms(gliss_loader_t *loader)
{
	/* NON c'est le numero de la section des symboles !!! */
	int i = loader->Tables.symtbl_ndx;
	return loader->Tables.sec_header_tbl[i].sh_size / loader->Tables.sec_header_tbl[i].sh_entsize;
}


/**
 * Get information about a symbol.
 * @param loader	Current loader.
 * @param sym		Symbol index.
 * @param data		Dat structure to store information in.
 */
void gliss_loader_sym(gliss_loader_t *loader, int sym, gliss_loader_sym_t *data) {
	assert(sym < gliss_loader_count_syms(loader));
	Elf32_Sym *s;

	/* get the descriptor */
	assert(sym < gliss_loader_count_syms(loader));
	s = &loader->Tables.sym_tbl[sym];

	/* get name */
	data->name = loader->Tables.symstr_tbl + loader->Tables.sym_tbl[sym].st_name;

	/* get value and section */
	data->value = s->st_value;
	data->sect = s->st_shndx;
	data->size = s->st_size;

	/* get the binding */
	switch(ELF32_ST_BIND(s->st_info)) {
	case STB_LOCAL: data->bind = GLISS_LOADER_LOCAL; break;
	case STB_GLOBAL: data->bind = GLISS_LOADER_GLOBAL; break;
	case STB_WEAK: data->bind = GLISS_LOADER_WEAK; break;
	default: data->bind = GLISS_LOADER_NO_BINDING; break;
	}

	/* get the type */
	switch(ELF32_ST_TYPE(s->st_info)) {
	case STT_FUNC: data->type = GLISS_LOADER_SYM_CODE; break;
	case STT_OBJECT: data->type = GLISS_LOADER_SYM_DATA; break;
	default: data->type = GLISS_LOADER_SYM_NO_TYPE; break;
	}
}


/* MEMORY_PAGE_SIZE gotten from mem.c,
!!WARNING!! value could change between archis and between systems */
#define MEMORY_PAGE_SIZE 4096

/**
 * get the initial value of the brk address
 * @param    loader  Loader containing code and data size informations.
 * @return	     the initial brk value
 */
gliss_address_t gliss_brk_init(gliss_loader_t *loader)
{
     int n = loader->Tables.pgm_hdr_tbl_size;
     Elf32_Phdr *seg_tbl = loader->Tables.pgm_header_tbl;
     gliss_address_t brk_init = 0;
     int i;

     /* brk will be the highest (vaddr + memsz) value
	among all loadable segments */

     for (i = 0; i < n; i++)
     {
	     if (seg_tbl[i].p_type == PT_LOAD)
	     {
		     gliss_address_t new_brk = seg_tbl[i].p_vaddr + seg_tbl[i].p_memsz;
		     if (new_brk > brk_init)
			     brk_init = new_brk;
	     }
     }

     /* MEMORY_PAGE_SIZE gotten from mem.h */
     return (brk_init + MEMORY_PAGE_SIZE - 1) & ~(MEMORY_PAGE_SIZE - 1);
}
