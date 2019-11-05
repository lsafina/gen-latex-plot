const val PREFIX = "stats"

const val ROOT_DIR = "latexPlotData"
const val TESTS_DIR = "$ROOT_DIR/statistics"
const val LATEX_GEN_DIR = "$ROOT_DIR/genLatex"
const val LATEX_PROCESS_DIR = "$ROOT_DIR/processLatex/"
const val LATEX_GEN_PLOT = "$LATEX_PROCESS_DIR/plot"
const val PDF_GEN_DIR = "$ROOT_DIR/genPDF"

val listOfTempDirs = listOf(
    PDF_GEN_DIR,
    LATEX_GEN_PLOT,
    LATEX_GEN_DIR
)

enum class TEST_TYPES(val value: String) {
    ALL("all"),
    COMMS_ONLY("comms-only"),
    INCREASING_IFS_NO_RECURSION("increasing-ifs-no-recursion"),
    INCREASING_IFS_PROCEDURES("increasing-ifs-procedures"),
    INCREASING_PROCESSES("increasing-processes")
}

enum class STRATEGY(val value: String) {
    RANDOM("Random"),
    INTERACTION_FIRST("InteractionsFirst"),
    CONDITION_FIRST("ConditionsFirst"),
    SHORTEST_FIRST("ShortestFirst"),
    LONGEST_FIRST("LongestFirst"),
    UNMARKED_FIRST("UnmarkedFirst"),
    UNMARKED_THEN_CONDITION("UnmarkedThenCondition"),
    UNMARKED_THEN_RANDOM("UnmarkedThenRandom"),
    UNMARKED_THEN_SELECTIONS("UnmarkedThenSelections")
}

enum class X(val value: String) {
    NUMBER_OF_ACTIONS("numberOfActions"),
    NUMBER_OF_PROCESSES("numberOfProcesses"),
    NUMBER_OF_PROCEDURES("numberOfProcedures"),
    NUMBER_OF_CONDITIONALS("numberOfConditionals"),
    AVG_NUMBER_OF_CONDITIONALS("avgNumberOfConditionalsInProcesses")
}

enum class Y(val value: String) {
    TIME("time(msec)"),
    NODES("nodes"),
    BAD_LOOPS("badLoops")
}