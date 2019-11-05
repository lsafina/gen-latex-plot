import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.File
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt
import java.sql.DriverManager.println
import java.util.ArrayList


fun main() {
    cleanTempFolders() //remove all previously generated files

    //generate a data source for a plot with different test parameters and different extraction strategies
    generatePlot(TEST_TYPES.COMMS_ONLY, X.NUMBER_OF_ACTIONS, Y.TIME, listOf(
            STRATEGY.RANDOM,
            STRATEGY.INTERACTION_FIRST,
            STRATEGY.SHORTEST_FIRST,
            STRATEGY.SHORTEST_FIRST), "risl")

    generatePlot(TEST_TYPES.COMMS_ONLY, X.NUMBER_OF_ACTIONS, Y.NODES, listOf(
            STRATEGY.RANDOM,
            STRATEGY.INTERACTION_FIRST,
            STRATEGY.SHORTEST_FIRST,
            STRATEGY.SHORTEST_FIRST), "risl")

    runLatex() //compile latex file to pdf
    mergePDF() //combine all pdf to one TestResult.pdf
}

/***
 * generate a data source plot for each or required strategy with data of 2 required columns
 */
fun generatePlot(testType: TEST_TYPES, x: X, y: Y, strategyList: List<STRATEGY>, strategiesSuffix: String = "") {
    val testType = testType.value
    val x = x.value
    val y = y.value

    println("[${LocalTime.now()}] Start generating laTex multi-lines plot files...")

    val latexGenDir = File(LATEX_GEN_DIR)
    val yFixed = if (y == "time(msec)") "time" else y //for some mysterious reasons brackets breaks everything

    val fileTitle = "stats-by-$x-to-$yFixed-$strategiesSuffix.tex"
    val genLatexFile = File("$latexGenDir/$fileTitle")

    genLatexFile.bufferedWriter().use { out ->

        for (strategy in strategyList) {
            val strategy = strategy.value
            val testFileName = "$PREFIX-$strategy-$testType"
            val testFile = File("$TESTS_DIR/$testFileName")

            if (testFile.exists()) {

                val pairXtoY = extractColumnsData(x, y, testFile) //get data for a plot
                out.appendln(
                        "\\pgfplotstableread{\n" +
                                "$x $y-mean $y-err"
                )

                for ((valX, valsY) in pairXtoY.toSortedMap()) {
                    out.appendln(
                            "$valX ${mean(valsY)} ${stderr(valsY)}"
                    )
                }
                out.appendln("}\\${testType.filter { it.isLetter() }}$strategy")
            }
        }

        out.append(
                "\\begin{figure}[!h]\n" +
                        "\\centering\n" +
                        "\\begin{tikzpicture}[yscale=1.5, xscale=1.5]\n" +
                        "\\begin{axis}[xlabel=$x,ylabel=$y, legend style={at={(0.5,1.6)},anchor=north}]\n"
        )

        for (strategy in strategyList) {
            val strategy = strategy.value
            out.appendln(

                    "\\addplot+[smooth]\n" +
                            "table[\n" +
                            "    x=$x,\n" +
                            "    y=$y-mean]\n" +
                            "{\\${testType.filter { it.isLetter() }}$strategy};\n" +
                            "\\addlegendentry{$strategy}"
            )
        }

        out.append(
                "\\end{axis}\n" +
                        "\\end{tikzpicture}\n" +
                        "\\caption[]{$testType}\n" +
                        "\\label{fig:test}\n" +
                        "\\end{figure}\n\n"
        )
    }

}

/**
 * retrieves data of 2 required columns from the file with statistics
 */
private fun extractColumnsData(x: String, y: String, testFile: File): HashMap<Double, ArrayList<Double>> {
    val lines = testFile.readLines()
    val split = lines.first().split("\t")
    val indexOfX = split.indexOf(x)
    val indexOfY = split.indexOf(y)

    val pairXtoY = HashMap<Double, ArrayList<Double>>()

    for (line in lines.drop(1)) {
        val lineSplit = line.split("\t")
        //try {
        val valX = lineSplit[indexOfX].toDouble()
        val valY = lineSplit[indexOfY].toDouble()
        if (pairXtoY.containsKey(valX))
            pairXtoY[valX]?.add(valY)
        else {
            val list = ArrayList<Double>()
            list.add(valY)
            pairXtoY[valX] = list
        }
    }
    return pairXtoY
}

/**
 * for each generated plot template, put it for a processing by lualatex to LATEX_GEN_PLOT, remove after processing
 */
fun runLatex() {
    println("[${LocalTime.now()}] Start processing laTex files...")
    val latexGenDir = File(LATEX_GEN_DIR)

    for (file in latexGenDir.list()) { //TODO replace path strings on const vals, check if anything was generated
        println("[${LocalTime.now()}] Generate pdf for $file...")
        val processBuilder = ProcessBuilder()
        val waitFor = processBuilder.command(//copy each generated plot template for a folder where it will be compiled by lualatex
                "bash",
                "-c",
                "cd $LATEX_GEN_DIR/; cp $file ../processLatex/plot/file.tex; cd ../processLatex/; lualatex -interaction nonstopmode main.tex; mv main.pdf ../genPDF/$file.pdf"
        ).start().waitFor(30, TimeUnit.SECONDS)
        if (!waitFor) println("[${LocalTime.now()}] FAIL. No pdf for $file was generated")

        processBuilder.command( //remove used plot
                "bash",
                "-c",
                "cd $LATEX_GEN_PLOT/; rm file.tex"
        ).start()
    }
}

/**
 * merge pdf results together
 */
fun mergePDF() {    //TODO: if there is one file, do not merge but rename
    println("[${LocalTime.now()}] Start merging pdfs...")
    val dir = File(PDF_GEN_DIR)
    val ut = PDFMergerUtility()

    for (file in dir.list())
        ut.addSource("$dir/$file")
    ut.destinationFileName = "$dir/TestResults.pdf"
    ut.mergeDocuments()
}

/**
 * remove previously generated files
 */
fun cleanTempFolders() {
    for (dirName in listOfTempDirs) {
        val dir = File(dirName)
        dir.deleteRecursively()
        dir.mkdir()
    }
}

//region math
fun mean(list: ArrayList<Double>) = if (list.isEmpty()) 0.0 else list.average()

fun stddev(list: ArrayList<Double>): Double {
    if (list.isEmpty()) return 0.0
    var sum = 0.0
    for (x in list) sum += with((x - mean(list))) { pow(2) }
    return sqrt(sum / list.size - 1)
}

fun stderr(list: ArrayList<Double>): Double {
    val res = stddev(list) / sqrt(list.size.toDouble())
    return if (res.isNaN()) 0.0 else res
}
//endregion
