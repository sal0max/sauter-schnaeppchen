package de.salomax.sauterschnaeppchen.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.itextpdf.text.pdf.PdfReader
import de.salomax.sauterschnaeppchen.data.AppDatabase
import de.salomax.sauterschnaeppchen.data.Item
import de.salomax.sauterschnaeppchen.data.ItemDao
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.io.InputStream

class ItemRepository(context: Context) {

    companion object {
        private var instance: ItemRepository? = null

        fun getInstance(application: Application): ItemRepository {
            if (instance == null) {
                synchronized(ItemRepository::class) {
                    instance = ItemRepository(application)
                }
            }
            return instance!!
        }
    }

    private var itemDao: ItemDao = AppDatabase.getInstance(context).itemDao()

    private val liveItems = itemDao.getAll()
    private var liveError = MutableLiveData<String?>()

    fun getError(): LiveData<String?> {
        return liveError
    }

    fun getItems(): LiveData<Array<Item>> {
        // get from network
        getPdfLink { url ->
            url?.let {
                // TODO only if new pdf
                downloadPdf(url) { pdfStream ->
                    itemDao.insertItems(parsePdf(pdfStream!!))
                }
            }
        }
        // get from db
        return liveItems
    }

    private fun parsePdf(pdfStream: InputStream): Array<Item> {
        val reader = PdfReader(pdfStream)
        val parser = PdfReaderContentParser(reader)
        val strategy = parser.processContent(TextExtractionStrategy())
        reader.close()

        return strategy.articles.toTypedArray()
    }

    /*
     * network call #1 - open website and get link to pdf
     */
    private fun getPdfLink(result: (String?) -> Unit) {
        OkHttpClient().newCall(
            Request.Builder()
                .url("https://www.foto-video-sauter.de/used")
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                liveError.postValue(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val doc = Jsoup.parse(response.body?.byteStream()!!, null, "foto-video-sauter.de")
                val link = doc.select("a[alt=Second-Hand-Artikel-Liste]").attr("href")
                result(link)
            }
        })
    }

    /*
     * network call #2 - download pdf (as stream)
     */
    private fun downloadPdf(pdfLink: String, result: (InputStream?) -> Unit) {
        OkHttpClient().newCall(
            Request.Builder()
                .url(pdfLink)
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                liveError.postValue(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                result(response.body?.byteStream())
            }
        })
    }

}
