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
    public static boolean printRemoveInfo = false;
    public static boolean printInlineInfo = false;

    // OPTIMIZATION
    public static boolean enableRegisterAllocation = true;

    public static boolean enableInstructionSelection = false;
    public static boolean enableInlineFunction = false;
    public static boolean enableCommonExpressionElimination = false;

    public static boolean enableOutputIrrelevantElimination = false;
}
