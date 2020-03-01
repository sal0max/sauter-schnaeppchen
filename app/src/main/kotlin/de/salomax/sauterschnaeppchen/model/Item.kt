package de.salomax.sauterschnaeppchen.model

import java.text.NumberFormat
import java.util.*

data class Item(
    var description: String? = null,
    var condition: Condition? = null,
    var articleNumber: String? = null,
    var serialNumber: String? = null,
    var price: Number? = null,
    var targetSystem: System? = null

) {
    override fun toString(): String {
        return "Article(\n" +
                "   description  : $description\n" +
                "   condition    : $condition\n" +
                "   articleNumber: $articleNumber\n" +
                "   serialNumber : $serialNumber\n" +
                "   price        : ${price?.let { NumberFormat.getCurrencyInstance(Locale.GERMANY).format(it) }}\n" +
                "   targetSystem : $targetSystem\n" +
                ")"
    }
}
