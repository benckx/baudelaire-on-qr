package be.encelade.baudelaire

import com.google.zxing.BarcodeFormat.QR_CODE
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL
import java.nio.file.Path

fun main() {
    val charSetName = "UTF-8"

    val text = FileUtils.readFileToString(File(getResourceAsURL("/le_chat.txt").toURI()), charSetName)
    val matrix = MultiFormatWriter().encode(text, QR_CODE, 800, 800)
    MatrixToImageWriter.writeToPath(matrix, "png", Path.of("qr.png"))
}

private fun getResourceAsURL(path: String): URL {
    return object {}.javaClass.getResource(path)!!
}
