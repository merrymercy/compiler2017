package com.mercy.compiler.Utility;

/**
 * Created by mercy on 17-5-19.
 */

public class Triple<A, B, C> {
    public A first;
    public B second;
    public C third;

    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}