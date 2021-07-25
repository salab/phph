#include <stdio.h>
char TAB = '\t';
char SPC = ' ';
char NL = '\n';
int TABSIZE = 8;
void main(int argc, char *argv[]) {
    FILE *fp;
    int c;
    int col;
    if (argc > 1) {
        if ((fp = fopen(argv[1], "r")) == NULL) {
            fprintf(stderr, "Can't open file: %s\n", argv[1]);
            exit(1);
        }
    }
    else {
        fp = stdin;
    }
    col = 0;
    while ((c = fgetc(fp)) != EOF) {
        if (c == TAB) {
            do {
                putchar(SPC);
                col = col + 1;
            } while (col % TABSIZE != 0);
        }
        else {
            if (c == NL) {
                col = 0;
                continue;
            }
            col = col = 1;
            putchar(c);
        }
    }
    fclose(fp);
}
