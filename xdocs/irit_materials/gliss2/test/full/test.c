#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int f(int x)
{
	return  x-10;
}

char** g(char **a)
{
	return a+1;
}

int h(int x)
{
	if (f(x))
		x = g(x);
	else
		x = g(f(x));
	return x * 2;
}

extern char **environ;



int main(int argc, char *argv[])
{
	int i = f(argc);
	char **t = g(argv);
	char **e = g(environ);
	fprintf(stderr, "argc = %d\n", argc);
	fprintf(stderr, "argv = %08X\n", argv);
	fprintf(stderr, "env = %08X\n", environ);
	for (i=0; i<10; i++)
	{
		printf("argv[%d](%08X):\t%s\n", i, argv[i], argv[i]);
	}
	printf("\n");
	printf("PATH:%s\n", getenv("PATH"));
	printf("USER:%s\n", getenv("USER"));
	for (i=0; i < 10; i++)
	//{
		//if (environ[i] == 0)
		//	break;
		//printf("envp[%d](@%08X):\t%s\n", i, &environ[i], environ[i]);
	//}
	//c = h(b);
	//d = f(g(h(f(d))));
	return 0;
}
