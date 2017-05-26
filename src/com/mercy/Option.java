package com.mercy;

/**
 * Created by mercy on 17-5-20.
 */
public class Option {
    // CONSTANT
    public static int REG_SIZE = 8;
    public static int STACK_VAR_ALIGNMENT_SIZE = 4;
    public static int FRAME_ALIGNMENT_SIZE = 16;

    // I/O
    public static String inFile = "testcase/test.c";
    public static String outFile = "out.asm";

    // DEBUG
    public static boolean printRemoveInfo = false;
    public static boolean printInlineInfo = false;
    public static boolean printInsturction = false;
    public static boolean printBasicBlocks = false;

    public static boolean printNaiveAllocatorInfo = false;
    public static boolean printUseDefInfo = true;

    // OPTIMIZATION
    public static boolean enableRegisterAllocation = true;

    public static boolean enableInstructionSelection        = false;
    public static boolean enableInlineFunction              = false;
    public static boolean enableCommonExpressionElimination = false;

    public static boolean enableControlFlowOptimization     = false;
    public static boolean enableOutputIrrelevantElimination = false;

    // STEADY
    public static boolean enablePrintExpand = true;
}
