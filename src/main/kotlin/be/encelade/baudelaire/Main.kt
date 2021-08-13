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
    val size = 550
    val limit = 1264
    val readMeLines = mutableListOf<String>()
    val textLengths = mutableListOf<Int>()
    val hints = mutableMapOf<EncodeHintType, Any>()
    hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
    hints[EncodeHintType.QR_VERSION] = Version.getVersionForNumber(29)!!
    hints[EncodeHintType.MARGIN] = 1
//    hints[EncodeHintType.CHARACTER_SET] = charSetName

    (1..134)
            .forEach { i ->
                val file = getResourceAsFile("/texts/main$i.xml")
                val body = Jsoup.parse(file, charSetName, "http://example.com/").body()
                val titles = body.getElementsByTag("h3")
                if (titles.isNotEmpty()) {
                    val text = body
                            .getElementsByTag("p")
                            .joinToString("\n") { p -> p.text() }
                            .replace("œ", "oe")

                    val title = titles.first()!!.text()
                    val textAndTitle = title + "\n\n" + text
                    textLengths += text.length

                    if (textAndTitle.length < limit) {
                        val imagePath = "img/qr$i.png"
                        val matrix = MultiFormatWriter().encode(textAndTitle, QR_CODE, size, size, hints)
                        MatrixToImageWriter.writeToPath(matrix, "png", Path.of(imagePath))

                        ///![](https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png =250x250)

                        readMeLines += "### $title"
                        readMeLines += "![]($imagePath | width=$size)"
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

private fun getResourceAsFile(path: String): File {
    return File(getResourceAsURL(path).toURI())
}

private fun getResourceAsURL(path: String): URL {
    return object {}.javaClass.getResource(path)!!
}
