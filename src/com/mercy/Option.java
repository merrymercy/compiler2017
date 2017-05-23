package com.mercy;

/**
 * Created by mercy on 17-5-20.
 */
public class Option {
    // I/O
    public static String inFile = "testcase/test.c";
    public static String outFile = "out.asm";

    // DEBUG
    public static boolean printInsturction = false;
    public static boolean printBasicBlocks = false;
    public static boolean printRemoveInfo = true;

    // OPTIMIZATION
    public static boolean enableInstructionSelection = true;
    public static boolean enableInlineFunction = true;
    public static boolean enableCommonExpressionElimination = true;

    public static boolean enableOutputIrrelevantElimination = true;
}
