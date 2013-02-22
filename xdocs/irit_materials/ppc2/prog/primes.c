#include <stdlib.h>
#include <stdio.h>

int is_prim_num(int num, int *list_prim) {
	int i;

	for(i=2;i<(num/2)+1;i++) {
		list_prim++;
		if(num%i==0) {
			return 0;
		}
	}
	return 1;
}

int main(void) {
	FILE *file;
	int *max;
	int *prim_num;
	int *act_num;
	int i;
	
	file = fopen("prim_num","r");
        if (file == NULL){
            fprintf(stderr,"Cannot open 'prim_num`, please create one with :\n"
                           "[user@host simul]> echo 1000 > prim_num \n");
            return 1;
        }
	max = (int *)malloc(sizeof(int));
        if (max==NULL) {
            fprintf(stderr,"Cannot alocate memory \n");
            return 1;
        }
	fscanf(file,"%d",max);
	fclose(file);
	fprintf(stderr,"maximum value: %d\n",*max);
	prim_num=(int *)malloc(sizeof(int)*(*max));
        if (max==NULL) {
            fprintf(stderr,"Cannot alocate memory (%d)\n",*max);
            return 1;
        }

	*prim_num = 1;
	act_num=prim_num;
	for(i=2;i<= *max;i++) {
		act_num+=1;
		if(is_prim_num(i,prim_num)) {
			*act_num=1;
			fprintf(stderr,"+++ %d +++ is a prime number\n",i);
		} else {
			*act_num=0;
			fprintf(stderr,"--- %d --- is not a prime number\n",i);
		}
	}
	fprintf(stderr,"printing the prime numbers between 0 and %d:\n",(*max)+1);
	act_num=prim_num;
	for(i=1;i<= *max;i++) {
		if(*act_num==1) {
			fprintf(stderr,"%d ",i);
		}
		if(i!=*max)
			act_num++;
	}
	fprintf(stderr,"\n");
        return 0;
}
