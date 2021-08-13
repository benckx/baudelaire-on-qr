package be.encelade.baudelaire

import com.google.zxing.BarcodeFormat.QR_CODE
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.io.File
import java.net.URL
import java.nio.file.Path

fun main() {
    val charSetName = "UTF-8"
    val qrSize = 400
    val limit = 900
    val readMeLines = mutableListOf<String>()

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

                    if (textAndTitle.length <= limit) {
                        val matrix = MultiFormatWriter().encode(textAndTitle, QR_CODE, qrSize, qrSize)
                        val imagePath = "img/qr$i.png"
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
}

private fun getResourceAsFile(path: String): File {
    return File(getResourceAsURL(path).toURI())
}

private fun getResourceAsURL(path: String): URL {
    return object {}.javaClass.getResource(path)!!
}
