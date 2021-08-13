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
    val qrSize = 600

    val text = FileUtils.readFileToString(File(getResourceAsURL("/le_chat.txt").toURI()), charSetName)
    val matrix = MultiFormatWriter().encode(text, QR_CODE, qrSize, qrSize)
    MatrixToImageWriter.writeToPath(matrix, "png", Path.of("img/qr.png"))

    (1..134)
            .map { i -> getResourceAsURL("/texts/main$i.xml") }
            .map { url -> File(url.toURI()) }
            .map { file -> Jsoup.parse(file, charSetName, "http://example.com/").body() }
            .forEach { body ->
                val text = body
                        .getElementsByTag("p")
                        .joinToString("\n") { p -> p.text() }

                println(text)
            }
}

private fun getResourceAsURL(path: String): URL {
    return object {}.javaClass.getResource(path)!!
}
