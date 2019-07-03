const val PREFIX = "stats"
const val ROOT_DIR = "latexPlotData"
const val TESTS_DIR = "$ROOT_DIR/latex/statistics"
const val LATEX_GEN_DIR = "$ROOT_DIR/genLatex"
const val LATEX_PROCESS_DIR = "$ROOT_DIR/latex/gen"
const val PDF_DIR = "$ROOT_DIR/genPDF"

val listOfDirs = listOf(
    PDF_DIR,
    LATEX_PROCESS_DIR,
    LATEX_GEN_DIR
)

val TEST_TYPES = listOf(
    "all",
    "comms-only",
    "increasing-ifs-no-recursion",
    "increasing-ifs-procedures",
    "increasing-processes",
    "test1",
    "test2",
    "test3-0",
    "test3-5",
    "test4"
)

val COLUMN_TYPES = listOf(
    "numberOfActions",
    "numberOfProcesses",
    "numberOfProcedures",
    "numberOfConditionals",
    "minLengthOfProcesses",
    "maxLengthOfProcesses",
    "avgLengthOfProcesses",
    "minNumberOfProceduresInProcesses",
    "maxNumberOfProceduresInProcesses",
    "avgNumberOfProceduresInProcesses",
    "minNumberOfConditionalsInProcesses",
    "maxNumberOfConditionalsInProcesses",
    "avgNumberOfConditionalsInProcesses",
    "numberOfProcessesWithConditionals",
    "minProcedureLengthInProcesses",
    "maxProcedureLengthInProcesses",
    "avgProcedureLengthInProcesses",
    "time(msec)",
    "nodes",
    "badLoops",
    "mainLength",
    "numOfProcedures",
    "minProcedureLength",
    "maxProcedureLength",
    "avgProcedureLength"
)


val STRATEGY = listOf(
    "Random",
    "InteractionsFirst",
    "ConditionsFirst",
    "ShortestFirst",
    "LongestFirst",
    "UnmarkedFirst",
    "UnmarkedThenCondition",
    "UnmarkedThenRandom",
    "UnmarkedThenSelections"
)

val X = listOf(
    "numberOfActions",
    "numberOfProcesses",
    "numberOfProcedures",
    "numberOfConditionals",
    "avgNumberOfConditionalsInProcesses"
)

val Y = listOf(
    "time(msec)",
    "nodes",
    "badLoops"
)