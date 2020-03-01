package de.salomax.sauterschnaeppchen.repository

import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.ContentByteUtils
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor
import com.itextpdf.text.pdf.parser.RenderListener
import java.io.IOException

class PdfReaderContentParser(private val reader: PdfReader) {

    @Throws(IOException::class)
    fun <E : RenderListener> processContent(renderListener: E): E {
        val processor = PdfContentStreamProcessor(renderListener)

        for (i in 1..reader.numberOfPages) {
            val pageDic = reader.getPageN(i)
            val resourcesDic = pageDic.getAsDict(PdfName.RESOURCES)
            processor.processContent(ContentByteUtils.getContentBytesForPage(reader, i), resourcesDic)
        }

        return renderListener
    }

}
