void A() {
	int a = 0;
	int i = 0;
	int j = 0;
	for (i = 0; i < 1000; ++i) {
		for (j = 0; j < 1000; ++j) {
			a = a + i;
		}
	}
}

void B() {
	A();
}

void C() {
	B();
}

int main() {
	int i = 0;
	for (i = 0; i < 100; ++i) {
		A();
		B();
		C();
	}
	println("Hello World");
}
