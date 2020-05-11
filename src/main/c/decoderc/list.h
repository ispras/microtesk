#include <stdlib.h>

typedef struct List List;

struct List
{
    int value;
    List* next;
};



void init(List **head, int data) {
    List *tmp = (List*) malloc(sizeof(List));
    tmp->value = data;
    tmp->next = (*head);
    (*head) = tmp;
}

List* getLast(List *head) {
    if (head == NULL) {
        return NULL;
    }
    while (head->next) {
        head = head->next;
    }
    return head;
}


void pushBack(List *head, int value) {
    List *last = getLast(head);
    List *tmp = (List*) malloc(sizeof(List));
    tmp->value = value;
    tmp->next = NULL;
    last->next = tmp;
}