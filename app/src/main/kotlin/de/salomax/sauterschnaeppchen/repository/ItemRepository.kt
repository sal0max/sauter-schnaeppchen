package de.salomax.sauterschnaeppchen.repository

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.itextpdf.text.pdf.PdfReader
import de.salomax.sauterschnaeppchen.R
import de.salomax.sauterschnaeppchen.data.AppDatabase
import de.salomax.sauterschnaeppchen.data.Item
import de.salomax.sauterschnaeppchen.data.ItemDao
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ItemRepository(val context: Context) {

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

    private var prefManager = PreferenceManager.getDefaultSharedPreferences(context)
    private var itemDao: ItemDao = AppDatabase.getInstance(context).itemDao()

    private val liveItems = itemDao.getAll()
    private var liveError = MutableLiveData<String?>()
    private var liveNetwork = MutableLiveData<Boolean>()

    fun getNetwork(): LiveData<Boolean> {
        return liveNetwork
    }

    fun getError(): LiveData<String?> {
        return liveError
    }

    fun getItems(): LiveData<Array<Item>> {
        liveNetwork.postValue(true)
        // get from network
        getPdfLink { url ->
            url?.let {
                // download and parse pdf only if it is new - check based on filename
                if (url != prefManager.getString("pdfLink", null)) {
                    downloadPdf(url) { pdfStream ->
                        itemDao.deleteAll() // delete the old
                        itemDao.insertItems(parsePdf(pdfStream!!)) // insert the new
                    }
                } else {
                    Log.v("sauterschnaeppchen", "Pdf hasn't changed: not downloading again.")
                    liveNetwork.postValue(false)
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
                liveNetwork.postValue(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val doc = Jsoup.parse(response.body?.byteStream()!!, null, "https://www.foto-video-sauter.de")
                val link = doc.select("a.btn[target=_blank]").last().attr("abs:href")
                Log.v("sauterschnaeppchen", "Fetched pdf link: $link")
                if (link.isEmpty()) {
                    liveError.postValue(context.getString(R.string.error_no_pdf))
                    liveNetwork.postValue(false)
                } else {
                    // return result
                    result(link)
                    // save (new) pdf date to sharedPrefs
                    val match = "\\d+\\.pdf".toRegex().find(link)
                    prefManager
                        .edit()
                        .putString(
                            "pdfLink",
                            link
                        )
                        .putString(
                            "pdfTitle",
                            if (match?.value != null) {
                                LocalDate.parse(match.value, DateTimeFormatter.ofPattern("yyyyMMdd'.pdf'"))
                                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            } else {
                                null
                            }
                        )
                        .apply()
                }
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
                liveNetwork.postValue(false)
            }

            override fun onResponse(call: Call, response: Response) {
                result(response.body?.byteStream())
                liveNetwork.postValue(false)
            }
        })
    }

}
