class A {
    A next;
    int x;
}

int main() {
    A a;

    a.next.next.next.x = 1;
}