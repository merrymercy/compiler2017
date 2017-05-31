package com.mercy;

/**
 * Created by mercy on 17-5-20.
 */
public class Option {
    /***** CONSTANT *****/
    public static int REG_SIZE = 8;
    public static int STACK_VAR_ALIGNMENT_SIZE = 4;
    public static int CLASS_MEMBER_ALIGNMENT_SIZE = 4;
    public static int FRAME_ALIGNMENT_SIZE = 16;

    /***** I/O *****/
    public static String inFile = "testcase/test.c";
    public static String outFile = "out.asm";

    public static final int TEST_LEVEL_DEVELOP = 0;
    public static final int TEST_LEVEL_EASY    = 1;
    public static final int TEST_LEVEL_FULL    = 2;
    public static final int TEST_LEVEL_STRICT  = 3;

    /***** DEBUG *****/
    public static int testLevel = TEST_LEVEL_DEVELOP;

    public static boolean printRemoveInfo = true;
    public static boolean printInlineInfo = true;
    public static boolean printInstruction = false;
    public static boolean printBasicBlocks = false;

    public static boolean printNaiveAllocatorInfo = true;
    public static boolean printGlobalAllocationInfo = false;

    /***** OPTIMIZATION *****/
    public static boolean enableGlobalRegisterAllocation = true;

    // ast-ir level
    public static boolean enablePrintExpand                 = true;
    public static boolean enableInlineFunction              = true;
    public static boolean enableSelfInline                  = true;
    public static boolean enableCommonAssignElimination     = true;
    public static boolean enableInstructionSelection        = true;

    // control flow
    public static boolean enableControlFlowOptimization     = true;

    // data flow
    public static boolean enableDataFlowOptimization        = true;
    public static boolean enableCommonExpressionElimination = true;
    public static boolean enableConstantPropagation         = true;
    public static boolean enableDeadcodeElimination         = true;

    // other
    public static boolean enableLeafFunctionOptimization    = true;
    public static boolean enableOutputIrrelevantElimination = true;
}
