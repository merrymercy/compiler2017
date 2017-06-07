// Target: Simulate a Bulgarian-solitaire game.
// Possible opitimization: Dead code elimination, common expression, inline function, loop unrolling, etc.
// REMARKS: A funny game. If you like, you can try to prove that when n=1+2+..+i(i>0), the game will always stop
//          and converge to the only solution: {1,2,...i}.   :)

int n;
int h;
int now;
int[] a;
int A = 48271;
int M = 2147483647;
int Q;
int R;
int seed=1;
int random() {
    int tempseed = A * (seed % Q) - R * (seed / Q);
    if (tempseed >= 0)
		seed = tempseed;
    else
		seed = tempseed + M;
	return seed;
}
void initialize(int val) {
    seed = val;
}
void swap(int x,int y) {
    int temp = a[x];
    a[x] = a[y];
    a[y] = temp;
}
bool pd(int x) {
    for (;h <= x; ++h)
        if (x == h * (h + 1) / 2)
			return true;
    return false;
}
void show() {
    int i;
    for (i = 0; i < now; ++i)
        print(toString(a[i]) + " ");
    println("");
}
bool win()
{
    int i;
	int j;
	int[] b = new int[101];
	int temp;
    if (now != h)
		return false;
    for (j = 0; j < now; ++j)
        b[j] = a[j];
    for (i = 0;i < now - 1; ++i)
        for (j = i + 1;j < now; ++j)
            if (b[i] > b[j]) {
                temp = b[i];
                b[i] = b[j];
                b[j] = temp;
            }
    for (i = 0; i < now; ++i)
        if (b[i] != i + 1)
			return false;
    return true;
}
void merge()
{
    int i;
    for (i = 0;i < now; ++i)
        if (a[i] == 0) {
            int j;
            for (j = i+1; j < now; ++j)
                if (a[j] != 0) {
                    swap(i,j);
                    break;
                }
        }
    for (i=0;i<now;++i)
        if (a[i] == 0) {
            now = i;
            break;
        }
}
void move() {
    int i = 0;
    for (; i < now; ) {
        --a[i];
        i = i + 1;
    }
    a[now] = now;
    now++;
}
int main() {
	int T = 0;
	for (; T < 100; ++T) {
		int i = 0;
		int temp = 0;
		int count = 0;
		n = 5050;
		h = 0;
		a = new int[101];
		Q = M / A;
		R = M % A;
		if (!pd(n)) {
			println("Sorry, the number n must be a number s.t. there exists i satisfying n=1+2+...+i");
			return 1;
		}
		println("Let's start!");
		initialize(random());
		now = random() % 10 + 1;
		println(toString(now));
		for (; i < now - 1; ++i)
		{
			a[i] = random() % 10 + 1;
			while (a[i] + temp > n)
				a[i] = random() % 10 + 1;
			temp = temp + a[i];
		}
		a[now - 1] = n - temp;
		show();
		merge();
		while (!win()) {
			// println("step " + toString(++count) + ":");
			++count;
			move();
			merge();
			// show();
		}
		println("Total: " + toString(count) + " step(s)");
	}
    return 0;
}


/*!! metadata:
=== comment ===
bulgarian-5110379024-wuhang.mx
=== input ===

=== assert ===
output
=== timeout ===
4.0
=== output ===
Let's start!
5
7 8 2 4 5029
Total: 5332 step(s)
Let's start!
6
2 2 2 8 8 5028
Total: 5433 step(s)
Let's start!
4
10 10 5 5025
Total: 5428 step(s)
Let's start!
6
3 6 1 8 8 5024
Total: 5526 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
6
8 4 3 8 5 5022
Total: 5224 step(s)
Let's start!
8
7 2 8 6 3 3 8 5013
Total: 5618 step(s)
Let's start!
3
4 2 5044
Total: 5045 step(s)
Let's start!
2
1 5049
Total: 4949 step(s)
Let's start!
7
6 10 5 6 5 4 5014
Total: 5616 step(s)
Let's start!
8
5 10 6 10 9 2 9 4999
Total: 4899 step(s)
Let's start!
2
7 5043
Total: 5144 step(s)
Let's start!
5
1 8 8 5 5028
Total: 5432 step(s)
Let's start!
7
2 1 10 7 8 1 5021
Total: 5526 step(s)
Let's start!
7
4 4 1 3 10 5 5023
Total: 5528 step(s)
Let's start!
9
7 2 10 9 9 8 10 4 4991
Total: 5698 step(s)
Let's start!
4
10 1 9 5030
Total: 5233 step(s)
Let's start!
9
6 6 6 2 8 6 9 8 4999
Total: 5606 step(s)
Let's start!
3
10 5 5035
Total: 5336 step(s)
Let's start!
9
6 1 5 2 9 5 1 1 5020
Total: 5121 step(s)
Let's start!
6
9 5 7 7 9 5013
Total: 5617 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
8
10 3 3 7 1 7 8 5011
Total: 5618 step(s)
Let's start!
9
7 9 2 2 5 10 8 4 5003
Total: 5508 step(s)
Let's start!
8
6 5 1 8 4 1 8 5017
Total: 4917 step(s)
Let's start!
5
10 4 3 2 5031
Total: 5334 step(s)
Let's start!
4
8 6 10 5026
Total: 4926 step(s)
Let's start!
8
8 4 9 8 10 6 9 4996
Total: 5698 step(s)
Let's start!
3
1 7 5042
Total: 5043 step(s)
Let's start!
4
8 8 6 5028
Total: 5432 step(s)
Let's start!
5
10 5 9 2 5024
Total: 5326 step(s)
Let's start!
6
7 2 4 7 5 5025
Total: 4925 step(s)
Let's start!
6
3 6 1 9 6 5025
Total: 5526 step(s)
Let's start!
6
1 9 5 7 2 5026
Total: 4926 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
2
5 5045
Total: 5046 step(s)
Let's start!
8
2 2 6 3 5 10 6 5016
Total: 5621 step(s)
Let's start!
10
8 6 8 1 1 4 1 7 3 5011
Total: 5214 step(s)
Let's start!
3
2 3 5045
Total: 4945 step(s)
Let's start!
10
4 10 9 10 6 10 2 2 3 4994
Total: 5700 step(s)
Let's start!
4
7 9 4 5030
Total: 5232 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
7
3 1 7 2 8 9 5020
Total: 5526 step(s)
Let's start!
4
3 5 10 5032
Total: 5335 step(s)
Let's start!
10
10 7 3 7 9 9 2 4 4 4995
Total: 5800 step(s)
Let's start!
6
3 1 6 2 3 5035
Total: 5339 step(s)
Let's start!
4
6 10 8 5026
Total: 4926 step(s)
Let's start!
10
4 9 2 8 9 3 4 1 3 5007
Total: 5711 step(s)
Let's start!
5
9 4 6 7 5024
Total: 5425 step(s)
Let's start!
3
2 7 5041
Total: 5143 step(s)
Let's start!
9
3 2 9 5 5 10 7 3 5006
Total: 5611 step(s)
Let's start!
8
4 2 9 7 4 4 9 5011
Total: 4911 step(s)
Let's start!
6
1 3 6 9 3 5028
Total: 5433 step(s)
Let's start!
8
3 4 7 1 6 8 4 5017
Total: 5119 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
5
10 6 1 8 5025
Total: 5526 step(s)
Let's start!
2
8 5042
Total: 5144 step(s)
Let's start!
7
3 8 2 3 6 3 5025
Total: 5427 step(s)
Let's start!
2
9 5041
Total: 4941 step(s)
Let's start!
8
7 1 1 3 4 1 8 5025
Total: 5527 step(s)
Let's start!
2
3 5047
Total: 5048 step(s)
Let's start!
7
2 6 4 6 7 10 5015
Total: 5517 step(s)
Let's start!
4
4 6 3 5037
Total: 4937 step(s)
Let's start!
5
8 8 6 7 5021
Total: 5525 step(s)
Let's start!
10
8 10 1 2 3 4 9 4 9 5000
Total: 5801 step(s)
Let's start!
5
3 9 5 1 5032
Total: 5133 step(s)
Let's start!
2
5 5045
Total: 5046 step(s)
Let's start!
6
5 9 9 10 7 5010
Total: 5617 step(s)
Let's start!
5
6 3 2 3 5036
Total: 5339 step(s)
Let's start!
7
5 2 7 5 1 9 5021
Total: 5525 step(s)
Let's start!
3
3 10 5037
Total: 5240 step(s)
Let's start!
2
8 5042
Total: 5144 step(s)
Let's start!
6
4 8 1 3 5 5029
Total: 5131 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
9
1 4 10 1 7 2 6 3 5016
Total: 5419 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
5
2 2 6 7 5033
Total: 4933 step(s)
Let's start!
5
3 10 4 4 5029
Total: 4929 step(s)
Let's start!
10
6 3 10 7 5 9 8 10 10 4982
Total: 5786 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
3
3 6 5041
Total: 5042 step(s)
Let's start!
2
3 5047
Total: 5048 step(s)
Let's start!
4
10 1 6 5033
Total: 5336 step(s)
Let's start!
10
4 7 3 7 7 3 1 5 5 5008
Total: 5714 step(s)
Let's start!
6
10 2 5 8 9 5016
Total: 5518 step(s)
Let's start!
8
6 7 7 9 5 6 9 5001
Total: 5707 step(s)
Let's start!
8
4 7 6 8 5 8 5 5007
Total: 4907 step(s)
Let's start!
3
8 9 5033
Total: 5334 step(s)
Let's start!
3
8 1 5041
Total: 4941 step(s)
Let's start!
5
10 7 3 9 5021
Total: 5526 step(s)
Let's start!
10
10 8 2 4 3 9 1 2 8 5003
Total: 5710 step(s)
Let's start!
5
10 8 6 9 5017
Total: 5618 step(s)
Let's start!
6
1 8 9 4 9 5019
Total: 4919 step(s)
Let's start!
9
10 1 5 5 10 10 8 9 4992
Total: 5798 step(s)
Let's start!
8
9 2 6 1 3 10 7 5012
Total: 5618 step(s)
Let's start!
3
10 6 5034
Total: 5336 step(s)
Let's start!
3
10 3 5037
Total: 5240 step(s)
Let's start!
4
1 3 5 5041
Total: 5142 step(s)
Let's start!
1
5050
Total: 4950 step(s)
Let's start!
2
5 5045
Total: 5046 step(s)
=== phase ===
optim pretest
=== is_public ===
True

!!*/

