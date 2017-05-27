#include <stdio.h>
#include <stdlib.h>

void aha() {
}

unsigned char *__lib_str_operator_ADD(unsigned char *a, unsigned char *b) {
    int l1 = *(((int *)a) - 1);
    int l2 = *(((int *)b) - 1);
    int l3 = l1 + l2;

    unsigned char *ret = (unsigned char *)malloc(l3 + 1 + sizeof(int));
    *((int *)ret) = l3;
    ret = ret + sizeof(int);
    for (int i = 0; i < l1; i++)
        ret[i] = a[i];
    ret = ret + l1;
    for (int i = 0; i < l2; i++)
        ret[i] = b[i];
    ret[l2] = 0;
    return ret - l1;
}

unsigned char *__lib_str_substring(unsigned char *a, int low, int high) {
    int l = high - low + 1;

    unsigned char *ret = (unsigned char *)malloc(l + 1 + sizeof(int));
    *((int *)ret) = l;
    ret = ret + sizeof(int);
    a += low;
    for (int i = 0; i < l; i++)
        ret[i] = a[i];
    ret[l] = 0;
    return ret;
}

unsigned char *toString(int x) {
    unsigned char *ret = (unsigned char*)malloc(12 + sizeof(int));
    ret += sizeof(int);
    unsigned char *p = ret;

    if (x < 0) {
        *p++ = '-';
        x = -x;
    }

    if (x == 0)
        *p++ = '0';

    unsigned char *begin = p;
    while (x) {
        int next = x / 10;
        *p++ = '0' + x - next * 10;
        x = next;
    }
    *p = 0;
    *(((int *)ret) - 1) = p - ret;

    p--;
    while (begin <= p) {
        char t = *begin;
        *begin = *p;
        *p = t;
        begin++;
        p--;
    }

    return ret;
}

int main() {

    return 0;
}
