#include <stdio.h>

int main(int argc,char **argv,char **envp){
    while(argc){
        printf("argv[%d]={%s}\n",argc-1,argv[argc-1]);
        argc--;
    }
    while(envp[argc]){
        printf("envp[%d]={%s}\n",argc,envp[argc]);
        argc++;
    }
    return argc;
}
