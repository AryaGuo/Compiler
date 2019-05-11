package Compiler;

public class Config {
    public static int REGISTER_WIDTH = 8;

    public enum Allocator {
        NaiveAllocator, SimpleGraphAllocator
    }

    public static boolean useCommonAssignElimination = true;
    public static boolean useOutputIrrelevantElimination = true;
    public static boolean useBackupOptimization = true;
    public static boolean useLocalValueNumbering = false;
    public static boolean useDeadCodeElimination = true;
    public static boolean useInlineOptimization = true;
    public static int inlineMaxDepth = 4;
    public static int inlineOperationsThreshold = 20;

    public static Allocator allocator = Allocator.SimpleGraphAllocator;
    public static boolean printAST = false;
    public static boolean printIR = false;
    public static boolean printIRAfterLocalValueNumbering = false;
    public static boolean printIRAfterDeadCodeElimination = false;
    public static boolean printIRAfterAllocator = false;
    public static boolean printIRWithFrame = false;
    public static boolean printToAsmFile = true;
}
