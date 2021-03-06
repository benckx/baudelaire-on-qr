package be.encelade.baudelaire

import com.google.zxing.BarcodeFormat.QR_CODE
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.decoder.Version
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.io.File
import java.net.URL
import java.nio.file.Path

fun main() {
    FileUtils.deleteDirectory(File("img/"))
    File("img").mkdir()

    val charSetName = "UTF-8"
    val imageSize = 430
    val readMeLines = mutableListOf<String>()
    val textLengths = mutableListOf<Int>()

    // https://www.qrcode.com/en/about/version.html
    val hints = mutableMapOf<EncodeHintType, Any>()
    hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
    hints[EncodeHintType.QR_VERSION] = Version.getVersionForNumber(22)!!
    hints[EncodeHintType.MARGIN] = 1
    val limit = 1003

    (1..134)
            .forEach { i ->
                val file = getResourceAsFile("/texts/main$i.xml")
                val body = Jsoup.parse(file, charSetName, "http://example.com/").body()
                val titles = body.getElementsByTag("h3")
                if (titles.isNotEmpty()) {
                    val text = body
                            .getElementsByTag("p")
                            .joinToString("\n") { p -> p.text() }
                            .sanitize()

                    val title = titles.first()!!.text().sanitize()
                    val textAndTitle = title + "\n\n" + text
                    textLengths += text.length

                    if (textAndTitle.length <= limit) {
                        val imagePath = "img/qr$i.png"
                        val matrix = MultiFormatWriter().encode(textAndTitle, QR_CODE, imageSize, imageSize, hints)
                        MatrixToImageWriter.writeToPath(matrix, "png", Path.of(imagePath))

                        readMeLines += "### $title"
                        readMeLines += "![]($imagePath)"
                    }
                }
            }

    val readMeFile = File("README.md")
    readMeFile.delete()

    val header = FileUtils.readFileToString(getResourceAsFile("/header.md"), charSetName)
    val content = header + readMeLines.joinToString("\n")
    FileUtils.writeStringToFile(readMeFile, content, charSetName)

    println("text length (min/avg/max): ${textLengths.minOrNull()} / ${textLengths.average().toInt()} / ${textLengths.maxOrNull()}")
}

private fun String.sanitize(): String {
    return this
            .replace("??", "oe")
            .replace("???", "'")
}

private fun getResourceAsFile(path: String): File {
    return File(getResourceAsURL(path).toURI())
}

private fun getResourceAsURL(path: String): URL {
    return object {}.javaClass.getResource(path)!!
}
