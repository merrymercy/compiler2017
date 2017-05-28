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

    public static final int TEST_LEVEL_DEVELOP = 0;
    public static final int TEST_LEVEL_EASY    = 1;
    public static final int TEST_LEVEL_FULL    = 2;
    public static final int TEST_LEVEL_STRICT  = 3;

    // DEBUG
    public static int testLevel = TEST_LEVEL_DEVELOP;

    public static boolean printRemoveInfo = true;
    public static boolean printInlineInfo = true;
    public static boolean printInsturction = false;
    public static boolean printBasicBlocks = false;

    public static boolean printNaiveAllocatorInfo = false;
    public static boolean printGlobalAllocationInfo = false;

    // OPTIMIZATION
    public static boolean enableGlobalRegisterAllocation = true;

    public static boolean enableInstructionSelection        = true;
    public static boolean enableInlineFunction              = true;
    public static boolean enableCommonExpressionElimination = true;

    public static boolean enableControlFlowOptimization     = true;

    public static boolean enableLeafFunctionOptimization    = false;
    public static boolean enableOutputIrrelevantElimination = false;
    // STEADY
    public static boolean enablePrintExpand = true;
}
