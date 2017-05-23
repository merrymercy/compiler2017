int counter = 0;
class ConstructorTester {
    int ct;
    ConstructorTester() {
        ct = counter++;
        println("Constructed - " + toString(this.ct));
    }
}
void testConstructor() {
    ConstructorTester a = new ConstructorTester;
    ConstructorTester[] b = new ConstructorTester[5];
    ConstructorTester[][] c = new ConstructorTester[4][4];
    ConstructorTester[][][] d = new ConstructorTester[3][3][3];
    ConstructorTester[][][][] e = new ConstructorTester[2][2][2][];

    int i; int j; int k;
    for (i = 0; i < e.size(); i++) {
        for (j = 0; j < e[i].size(); j++) {
            for (k = 0; k < e[j].size(); k++) {
                e[i][j][k] = new ConstructorTester[1];
            }
        }
    }
}

class SideEffectTester {
    int ct;

    SideEffectTester() {
        ct = 0;
    }

    SideEffectTester getSelf() {
        ct++;
        return this;
    }
}
void testSideEffect() {
    SideEffectTester a = new SideEffectTester;

    println(toString(a.ct));
    a.getSelf();
    println(toString(a.ct));
    a.getSelf().ct++;
    println(toString(a.ct));
    ++a.getSelf().ct;
    println(toString(a.ct));
    println(toString(++a.getSelf().ct));
    println(toString(a.getSelf().ct++));
    println(toString(a.ct));
}

bool error() {
    println("ERROR!");
}
void testManyLogic(int n) {
    int i;
    bool a = true;  //getInt() == 1;
    bool b = false; //getInt() == 1;

    for (i = 0; i < n; i++) {
        if (a && a && a && a && a && a && a && a && a && a && a && a && a && a && a && a &&
            a && a && a && a && a && a && a && a && a && a && a && a && a && a && a && a) {
            println("1 - true");
        } else {
            println("1 - false");
        }

        if (b || b || b || b || b || b || b || b || b || b || b || b || b || b || b || b ||
            b || b || b || b || b || b || b || b || b || b || b || b || b || b || b || b ) {
            println("2 - true");
        } else {
            println("2 - false");
        }

        if (!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!a) {
            println("3 - true");
        } else {
            println("3 - false");
        }
    }

    // short cut
    if (a || error()) {
        println("4 - true");
    } else {
        println("4 - false");
    }

    if (b && error()) {
        println("5 - true");
    } else {
        println("5 - false");
    }
}

void testArray(int n) {
    int [][][][][][][][][][] a = new int[2][2][2][2][2][2][2][2][2][2];

    int i; int i1; int i2; int i3; int i4; int i5; int i6; int i7; int i8; int i9; int i10;
    for (i = 0; i < n; i++) {
        int s = 0;

        for (i1 = 0; i1 < a.size(); i1++)
            for (i2 = 0; i2 < a.size(); i2++)
            for (i3 = 0; i3 < a.size(); i3++)
            for (i4 = 0; i4 < a.size(); i4++)
            for (i5 = 0; i5 < a.size(); i5++)
            for (i6 = 0; i6 < a.size(); i6++)
            for (i7 = 0; i7 < a.size(); i7++)
            for (i8 = 0; i8 < a.size(); i8++)
            for (i9 = 0; i9 < a.size(); i9++)
            for (i10 = 0; i10 < a.size(); i10++)
                a[i1][i2][i3][i4][i5][i6][i7][i8][i9][i10] = ~(i1 + i2 - i3 * i4 / (i5+93) % (i6+93) & i7 | i8 ^ i9 + i10);

        for (i1 = 0; i1 < a.size(); i1++)
            for (i2 = 0; i2 < a.size(); i2++)
            for (i3 = 0; i3 < a.size(); i3++)
            for (i4 = 0; i4 < a.size(); i4++)
            for (i5 = 0; i5 < a.size(); i5++)
            for (i6 = 0; i6 < a.size(); i6++)
            for (i7 = 0; i7 < a.size(); i7++)
            for (i8 = 0; i8 < a.size(); i8++)
            for (i9 = 0; i9 < a.size(); i9++)
            for (i10 = 0; i10 < a.size(); i10++)
                s = s + a[i1][i2][i3][i4][i5][i6][i7][i8][i9][i10];

        println(toString(i) + " " + toString(s));
    }
}

class MemberTester {
    int x;
    MemberTester next1;
    int y;
    MemberTester next2;
}

void testMember(int n) {
    MemberTester root = new MemberTester;
    MemberTester now = root;
    int i;
    for (i = 0; i < n; i++) {
        now.next2 = new MemberTester;
        now = now.next2;
    }

    root.next2.next2.next2.next2.next2.next2.x = 197;
    root.next2.next2.next2.x = 297;

    println(toString(root.next2.next2.next2.x + root.next2.next2.next2.next2.next2.next2.x));
}

int testInstructionSelection(int n) {
    int i;
    int [] a = new int [n];
    int [] b = new int [n];
    int [] c = new int [n];

    a[0] = 1; a[1] = 2;
    b[0] = 2; b[1] = 1;
    c[0] = 0; c[1] = 0;
    for (i = 2; i < n; i++) {
        a[i] = (a[i-1] * 4 + a[i-2] + 2) & ((1 << 20) - 1);
        b[i] = (3 + b[i-1] * 8 + a[i-2]) & ((1 << 20) - 1);
        c[i] = (a[i] + b[i] * 2) & ((1 << 20) - 1);
    }

    int sum = 0;
    for (i = 0; i < n; i++) {
        sum = (a[i] + b[i] * 2 + c[i] + 9)  & ((1 << 20) - 1);;
    }

    return sum;
}

int main() {
    testConstructor();
    testSideEffect();
    testManyLogic(10);
    testArray(10);
    testMember(10);

    int i;
    int sum = 0;
    for (i = 0; i < 1200; i++)
        sum = (sum + testInstructionSelection(100000)) & ((1 << 20) - 1);;
    println(toString(sum));


    return 0;
}

/*!! metadata:
=== comment ===
some correctness test
=== input ===
=== assert ===
output
=== timeout ===
1.0
=== output ===
Constructed - 0
Constructed - 1
Constructed - 2
Constructed - 3
Constructed - 4
Constructed - 5
Constructed - 6
Constructed - 7
Constructed - 8
Constructed - 9
Constructed - 10
Constructed - 11
Constructed - 12
Constructed - 13
Constructed - 14
Constructed - 15
Constructed - 16
Constructed - 17
Constructed - 18
Constructed - 19
Constructed - 20
Constructed - 21
Constructed - 22
Constructed - 23
Constructed - 24
Constructed - 25
Constructed - 26
Constructed - 27
Constructed - 28
Constructed - 29
Constructed - 30
Constructed - 31
Constructed - 32
Constructed - 33
Constructed - 34
Constructed - 35
Constructed - 36
Constructed - 37
Constructed - 38
Constructed - 39
Constructed - 40
Constructed - 41
Constructed - 42
Constructed - 43
Constructed - 44
Constructed - 45
Constructed - 46
Constructed - 47
Constructed - 48
Constructed - 49
Constructed - 50
Constructed - 51
Constructed - 52
Constructed - 53
Constructed - 54
Constructed - 55
Constructed - 56
0
1
3
5
7
8
9
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
1 - true
2 - false
3 - false
4 - true
5 - false
0 -2176
1 -2176
2 -2176
3 -2176
4 -2176
5 -2176
6 -2176
7 -2176
8 -2176
9 -2176
494
973680
=== phase ===
optim extended
=== is_public ===
True

!!*/