// cover all
int Wallace = 1 << 10;
class sometimes {
    int naive;
    string young;
    void make_money() {
        this.naive++;
    }
}

int main() {
    sometimes keep = new sometimes;
    keep.naive = 0;
    while(getInt() < Wallace) {
        keep.make_money();
    }
    int i; int sum = 0;
    for (i = 100; i >= 0; i--)
        sum = sum + i;

    bool odd;
    if (sum % 2 == 0)
        odd = true;
    else
        odd = false;

    return 0;
}