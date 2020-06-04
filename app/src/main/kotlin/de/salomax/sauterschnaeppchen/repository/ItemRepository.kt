package de.salomax.sauterschnaeppchen.repository

import android.app.Application
import android.content.Context
import android.net.ParseException
import android.net.Uri
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
                if (url == prefManager.getString("pdfLink", null)) {
                    downloadPdf(url) { pdfStream ->
                        // delete the old
                        itemDao.deleteAll()
                        // insert the new
                        try {
                            itemDao.insertItems(parsePdf(pdfStream!!))
                        } catch (e: ParseException) {
                            liveError.postValue(context.getString(R.string.error_invalid_pdf))
                        }
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
     * https://www.foto-video-sauter.de/INTERSHOP/static/BOS/Calumet-Site/SauterDE/Calumet-SauterDE/de_DE/pdf/second-hand/schnaeppchen-liste-second-hand-20200211.pdf
     * https://www.foto-video-sauter.de/INTERSHOP/static/BOS/Calumet-Site/SauterDE/Calumet-SauterDE/de_DE/pdf/second-hand/Second%20Hand%20Liste_25.03.2020.pdf
     * https://www.foto-video-sauter.de/INTERSHOP/static/BOS/Calumet-Site/SauterDE/Calumet-SauterDE/de_DE/pdf/second-hand/Second%20Hand%20Liste%20Stand%2031.03.2020_2.pdf
     * https://www.foto-video-sauter.de/INTERSHOP/static/BOS/Calumet-Site/SauterDE/Calumet-SauterDE/de_DE/pdf/second-hand/Second_Hand_Liste_15_4_2020.pdf
     * https://www.foto-video-sauter.de/INTERSHOP/static/BOS/Calumet-Site/SauterDE/Calumet-SauterDE/de_DE/pdf/second-hand/Second%20Hand%20Liste%20Stand%20KW20_2020.pdf
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
                val link = doc.select("a.btn[href$=.pdf]").last().attr("abs:href")
                Log.v("sauterschnaeppchen", "Fetched pdf link: $link")
                if (link.isEmpty()) {
                    liveError.postValue(context.getString(R.string.error_no_pdf))
                    liveNetwork.postValue(false)
                } else {
                    // return result
                    result(link)
                    // save (new) pdf date to sharedPrefs
                    // - secondhand-liste-20200211.pdf
                    // - Second Hand Liste_25.03.2020.pdf // 25 03 2020
                    prefManager
                        .edit()
                        .putString("pdfLink", link)
                        .putString("pdfTitle", Uri.parse(link).lastPathSegment
                            ?.replace(".pdf", "")
                            ?.replace("Second Hand", "")
                            ?.replace("Liste", "")
                            ?.replace("Stand", "")
                            ?.replace("_", " ")
                            ?.replace("  ", " ")
                            ?.trim()
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
