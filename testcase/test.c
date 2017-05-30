int main() {
    int  i;
    int j;
    int n  = 5000;

    int sum = 0;

    for (i = 1; i < n; i++)
        for (j = 1; j < n; j++) {
            sum = sum + i % j;
        }

    println(toString(sum));
}