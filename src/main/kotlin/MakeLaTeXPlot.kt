import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.File
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    cleanFolders()
    generatePlot("comms-only", "numberOfActions", "time(msec)", listOf("Random", "InteractionsFirst", "ConditionsFirst"))
    runLatex()
    mergePDF()
}

fun generatePlot(testType: String, x: String, y: String, strategyList: List<String>) {
    println("[${LocalTime.now()}] Start generating laTex multi-lines plot files...")
    val latexGenDir = File(LATEX_GEN_DIR)

    val yy = if (y == "time(msec)") "time" else y
    val fileTitle = "stats-by-$x-to-$yy"
    val genLatexFile = File("$latexGenDir/$fileTitle.tex")

    genLatexFile.bufferedWriter().use { out ->

        for (strategy in strategyList) {
            val testFileName = "$PREFIX-$strategy-$testType"
            val testFile = File("$TESTS_DIR/$testFileName")

            if (testFile.exists()) {

                val pairXtoY = extractColumnsData(x, y, testFile)
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

fun generatePlots(listOfTests: List<String> = TEST_TYPES, listOfX: List<String> = X, listOfY: List<String> = Y, listOfStrategies: List<String> = STRATEGY) {
    println("[${LocalTime.now()}] Start generating laTex multi-lines plot files...")
    val latexGenDir = File(LATEX_GEN_DIR)

    var counter = 1

    for (x in listOfX) {
        for (y in listOfY) {

            val yy = if (y == "time(msec)") "time" else y
            val fileTitle = "stats-by-$x-to-$yy"
            val genLatexFile = File("$latexGenDir/$fileTitle.tex")

            genLatexFile.bufferedWriter().use { out ->
                for (testType in listOfTests) {
                    for (strategy in listOfStrategies) {
                        val testFileName = "$PREFIX-$strategy-$testType"
                        val testFile = File("$TESTS_DIR/$testFileName")

                        if (testFile.exists()) {

                            val pairXtoY = extractColumnsData(x, y, testFile)
                            out.appendln(
                                "\\pgfplotstableread{\n" +
                                        "$x $y-.mean $y-err"
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

                    for (strategy in listOfStrategies) {
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
                                "\\label{fig:test${counter++}}\n" +
                                "\\end{figure}\n\n"
                    )
                }
            }
        }
    }

}

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
        /*}
        catch (e: Exception) {
            println(indexOfX)
            println(lineSplit.size)
            println(testFile)
        }*/


    }
    return pairXtoY
}

fun runLatex() {
    println("[${LocalTime.now()}] Start processing laTex files...")
    val latexGenDir = File(LATEX_GEN_DIR)

    for (file in latexGenDir.list()) {
        println("[${LocalTime.now()}] Generate pdf for $file...")
        val processBuilder = ProcessBuilder()
        val waitFor = processBuilder.command(
            "bash",
            "-c",
            "cd $LATEX_GEN_DIR/; cp $file ../latex/gen/file.tex; cd ../latex/; lualatex -interaction nonstopmode main.tex; mv main.pdf ../genPDF/$file.pdf"
        ).start().waitFor(30, TimeUnit.SECONDS)
        if (!waitFor) println("[${LocalTime.now()}] FAIL. No pdf for $file was generated")

        processBuilder.command(
            "bash",
            "-c",
            "cd $LATEX_PROCESS_DIR/; rm file.tex"
        )
    }
}

fun mergePDF() {
    println("[${LocalTime.now()}] Start merging pdfs...")
    val ut = PDFMergerUtility()
    val dir = File(PDF_DIR)

    for (file in dir.list())
        ut.addSource("$dir/$file")
    ut.destinationFileName = "$dir/TestResults.pdf"
    ut.mergeDocuments()
}

fun cleanFolders() {
    for (dirName in listOfDirs) {
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

//region backlog
/*fun forallParam(testDir: String) {
    println("[${LocalTime.now()}] Start generating latex boilerplate...")
    val dir = File(testDir)
    //if (!dir.exists() || !dir.isDirectory) dir.mkdir()

    val res = File(.LATEX_GEN_DIR)
    if (!res.exists() || !res.isDirectory) res.mkdir()
    var counter = 0

    for (file in dir.list()) {
        if (.getTEST_TYPES.any { elem -> file.endsWith(elem) }) {
            for (.getX in COLUMN_TYPES_IMPORTANT) {
                println("[${LocalTime.now()}] Generating file for $file-by-$kotlin.getX")
                val test = File("$kotlin.LATEX_GEN_DIR/$file-by-$kotlin.getX.tex")
                test.bufferedWriter().use { out ->
                    for (y in COLUMN_TYPES_IMPORTANT) {
                        if (.getX != y) {
                            out.appendln(
                                "\\begin{figure}\n" +
                                        "\\begin{tikzpicture}\n" +
                                        "\\begin{axis}[xlabel=$kotlin.getX,ylabel=$y]\n" +
                                        "\\addplot [scatter,only marks] \n" +
                                        "table[.getX=$kotlin.getX,y=$y]\n" +
                                        "{statistics/$file};\n" +
                                        "\\end{axis}\n" +
                                        "\\end{tikzpicture}\n" +
                                        "\\caption[]{$kotlin.getX to $y ($file)}\n" +
                                        "\\label{fig:test${counter++}}\n" +
                                        "\\end{figure}\n"
                            )
                        }
                    }
                }
            }

        }
    }
}*/

/*
fun parseFileNamesByStrategy(){
    val dir = File(RESULT_DIR)
    val destDir = File("final")


    val tempDir = File("tempDir")
    tempDir.mkdir()

    COLUMN_TYPES_IMPORTANT.forEach { column ->
        println(column)
        val ut = PDFMergerUtility()
        for (file in dir.list()){
            if (file.contains(column)){
                ut.addSource("$dir/$file")
            }
        }
        ut.destinationFileName = "$destDir/by-$column-TestResults.pdf"
        ut.mergeDocuments()
    }
}

fun generateLaTexByParams(listOfTests: ArrayList<String>, listOfX: ArrayList<String>, listOfY: ArrayList<String>, listOfStrategies: ArrayList<String>) {
    println("[${LocalTime.now()}] Start generating laTex files...")
    val testsDir = File(.TESTS_DIR)
    val latexGenDir = File(.LATEX_GEN_DIR)

    var counter = 1

    for (x in listOfX) {
        for (y in listOfY) {

            val yy = if (y == "time(msec)") "time" else y
            val fileTitle = "stats-by-$x-to-$yy"
            val genLatexFile = File("$latexGenDir/$fileTitle.tex")

            genLatexFile.bufferedWriter().use { out ->
                out.appendln("\\section{$x to $y}")

                for (testType in listOfTests) {
                    out.appendln(
                        "\\clearpage\n" +
                                "\\subsection{$testType}\n" +
                                "\\clearpage"
                    )
                    for (file in testsDir.list().sorted()) {
                        if (file.endsWith("$testType.txt")) {
                            val strategy = file.split("-")[1]

                            out.appendln(
                                "\\begin{figure}\n" +
                                        "\\centering\n" +
                                        "\\begin{tikzpicture}\n" +
                                        "\\begin{axis}[xlabel=$x,ylabel=$y]\n" +
                                        "\\addplot [scatter,only marks] \n" +
                                        "table[x=$x,y=$y]\n" +
                                        "{statistics/$file};\n" +
                                        "\\end{axis}\n" +
                                        "\\end{tikzpicture}\n" +
                                        "\\caption[]{$x to $y ($strategy)}\n" +
                                        "\\label{fig:test${counter++}}\n" +
                                        "\\end{figure}\n\n"
                            )

                        }

                    }
                }

            }
        }
    }
}
*/
//endregion
