package de.salomax.sauterschnaeppchen.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.itextpdf.text.pdf.PdfReader
import de.salomax.sauterschnaeppchen.model.Item
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.io.InputStream

object ItemRepository {

    fun getItems(pdfStream: InputStream): MutableLiveData<Array<Item>> {
        val items = MutableLiveData<Array<Item>>()
        items.value = parsePdf(pdfStream)
        return items
    }

    fun getItems(result: (Array<Item>?, String?) -> Unit) {
        getPdfLink { url, error ->
            if (error != null) {
                result(null, error)
            }
            else {
                // -----------------
                downloadPdf(url!!) { pdfStream, error2 ->
                    if (error2 != null) {
                        result(null, error2)
                    } else {
                        result(parsePdf(pdfStream!!), null)
                    }
                }
                // -----------------
            }
        }
    }

    // network
    private fun getPdfLink(result: (String?, String?) -> Unit) {
        OkHttpClient()
            .newCall(
                Request.Builder()
                    .url("https://www.foto-video-sauter.de/used")
                    .build()
            )
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    result(null, e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    val doc = Jsoup.parse(response.body?.byteStream()!!, null, "foto-video-sauter.de")
                    val link = doc.select("a[alt=Second-Hand-Artikel-Liste]").attr("href")
                    Log.d("s", link)
                    result(link, null)
                }
            })
    }

    // network
    private fun downloadPdf(pdfLink: String, result: (InputStream?, String?) -> Unit) {
        OkHttpClient()
            .newCall(
                Request.Builder()
                    .url(pdfLink)
                    .build()
            )
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    result(null, e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    result(response.body?.byteStream(), null)
                }
            })
    }

    private fun parsePdf(pdfStream: InputStream): Array<Item> {
        val reader = PdfReader(pdfStream)
        val parser =
            PdfReaderContentParser(
                reader
            )
        val strategy = parser.processContent(TextExtractionStrategy())
        reader.close()

        return strategy.articles.toTypedArray()
    }

}
