
int main() {
int i;
    for (i = 0; i < 100;i ++)
        i = i + 2;

println("hell");
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