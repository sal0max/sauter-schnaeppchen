package de.salomax.sauterschnaeppchen.repository

import android.annotation.SuppressLint
import com.itextpdf.text.pdf.parser.ImageRenderInfo
import com.itextpdf.text.pdf.parser.RenderListener
import com.itextpdf.text.pdf.parser.TextRenderInfo
import de.salomax.sauterschnaeppchen.data.Item
import de.salomax.sauterschnaeppchen.data.Condition
import de.salomax.sauterschnaeppchen.data.TargetSystem
import java.text.NumberFormat
import java.util.*

/**
 * @see com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
 */
class TextExtractionStrategy : RenderListener {

    // used to store everything
//    private val allText = StringBuffer()

    // used to store each table entry
    private val currentText = StringBuffer()

    // used to store an Article
    private var item: Item? = null

    // all parsed articles
    var articles = mutableListOf<Item>()

    // the entire pdf
//    val text: String
//        get() {
//            return allText.toString()
//        }


    override fun beginTextBlock() {
        // ignored
    }

    override fun endTextBlock() {
        val currentTextTrimmed = currentText.toString().trim()

        // save
//        allText.append("$currentTextTrimmed\n")

        // PARSE: condition & serialNr - always in the same field **********************************
        if (
            currentTextTrimmed.contains("Zust.:") ||
            currentTextTrimmed.contains("Zustand:") ||
            currentTextTrimmed.contains("SN:")
        ) {
            // condition ***************************************************************************
            var condition: String? = currentTextTrimmed
                .replace("Zustand: ", "Zust.:")
                .replace("Zustand:", "Zust.:")
            condition = condition?.let {
                "Zust\\.:\\S+".toRegex()
                    .find(it)?.value
                    ?.replace("Zust.:", "")
                    ?.replace("/", "")
            }
            item?.condition = condition?.let { Condition.valueOf(it) }
            // serialNr ****************************************************************************
            val serialNr = currentTextTrimmed.replace("SN: ", "SN:")
            item?.serialNumber = "SN:\\S+".toRegex().find(serialNr)?.value?.replace("SN:", "")
        }

        // PARSE: price, e. g. "1.299,00 €" ********************************************************
        else if (currentTextTrimmed.contains("€") || "(\\d+.)?\\d+,\\d{2}".toRegex().matches(currentTextTrimmed)) {
            item?.price = NumberFormat.getInstance().parse(
                currentTextTrimmed.replace(" €", "")
            )?.toFloat()
            /*
             * END (price is always last) **********************************************************
             */
            item?.let { if (it.description.isNotBlank()) articles.add(it) }
            item = Item()
        }

        // PARSE: itemNr ***************************************************************************
        else if (
            "\\d{1,2}\\w{2,4}\\S{5,6}".toRegex().matches(currentTextTrimmed)
        ) {
            item?.articleNumber = currentTextTrimmed
        }

        // PARSE: desc *****************************************************************************
        else {
            // filter out
            if (
                currentTextTrimmed != "Diff.-Best." &&
                currentTextTrimmed != "MwSt." &&
                currentTextTrimmed != "Verkauf solange der Vorrat reicht." &&
                currentTextTrimmed != "12 Monate Gewährleistung." &&
                currentTextTrimmed != "Vorbehaltlich Zwischenverkauf." &&
                currentTextTrimmed != "Foto-Video Sauter GmbH Co. KG" &&
                currentTextTrimmed != "Foto-Video Sauter GmbH Co.KG" &&
                currentTextTrimmed != "Sonnenstraße 26" &&
                currentTextTrimmed != "80331 München" &&
                currentTextTrimmed != "Sonnenstr. 26; 80331 München" &&
                currentTextTrimmed != "Daten" &&
                currentTextTrimmed != "Beschreibung" &&
                currentTextTrimmed != "Zustand" &&
                currentTextTrimmed != "Artikelnr." &&
                currentTextTrimmed != "Steuer-Art" &&
                currentTextTrimmed != "Excl MwST" &&
                currentTextTrimmed != "incl. MwSt." &&
                currentTextTrimmed != "verkauf@foto-video-sauter.de" &&
                currentTextTrimmed != "089 55 15 04 0" &&
                currentTextTrimmed != "Foto-Video-Sauter  -  Second-Hand-Artikel" &&
                currentTextTrimmed != "*      Mit" &&
                currentTextTrimmed != "bezeichenete Artikel werden nach  § 25a UStG als Gebrauchtwaren differenzbesteuert." &&
                currentTextTrimmed != "." &&
                !currentTextTrimmed.startsWith("+49 (0)") &&
                !currentTextTrimmed.contains("Seite \\d+ von".toRegex()) &&
                !currentTextTrimmed.contains("Stand\\s+\\d+".toRegex())
            ) {
                // clean sloppy descriptions
                var fixedDesc = currentTextTrimmed.replace("+", " + ")
                fixedDesc = fixedDesc.replace("\\s+".toRegex(), " ")
                fixedDesc = fixedDesc.replace("Zub.", "Zubehör")
                fixedDesc = fixedDesc.replace("Sobl.", "Sonnenblende")
                fixedDesc = fixedDesc.replace("Ta.", "Tasche")
                fixedDesc = fixedDesc.replace(" f ", " für ")
                fixedDesc = fixedDesc.capitalizeWords()
                // finish
                item?.description = if (item?.description.isNullOrBlank()) fixedDesc else item?.description + " $fixedDesc"
                item?.targetSystem = TargetSystem.find(item?.description)
            }
        }

        // reset buffer
        currentText.delete(0, currentText.length)
    }

    override fun renderText(renderInfo: TextRenderInfo) {
        currentText.append(renderInfo.text)
    }

    override fun renderImage(renderInfo: ImageRenderInfo) {
        // ignored
    }

    @SuppressLint("DefaultLocale")
    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") {
            if (it.matches("[A-Z]{4,}.*".toRegex()))
                it.toLowerCase(Locale.GERMAN).capitalize()
            else
                it
        }

}
