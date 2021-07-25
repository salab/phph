#include <stdio.h>
char SPC = ' ';
char TAB = '\t';
char NL = '\n';
int TABSIZE = 8;
void main(int argc, char *argv[]) {
    FILE *fp;
    int c;
    int col;
    col = 0;
    if (argc < 2) {
        fp = stdin;
    }
    else {
        if ((fp = fopen(argv[1], "r")) == NULL) {
            fprintf(stderr, "Can't open file: %s\n", argv[1]);
            exit(1);
        }
    }
    while ((c = fgetc(fp)) != EOF) {
        if (c == NL) {
            printf("(%d)\n", col);
            col = 0;
            continue;
        }
        putchar(c);
        col = col + 1;
    }
    fclose(fp);
}
