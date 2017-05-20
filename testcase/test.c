class A {
  B() {}
}

class B {
  A() {}
}

int main() {
  A b = new A;
  B a = new B;
}
