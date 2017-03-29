class A {
    A this() {
        return this;
    }

    A test() {
        getThis().getThis();
    }
}

int main() {

}


/*A object;
int main() {
    object.func1(2, 3);
    return 0;
}


class A {
    void func1(int x, int y) {
        func2(this.x*x, this.y*y);
    }

    void func2(int xx, int yy) {
        func1(x*this.x, y*this.y);
    }

    int x; int y;
}*/