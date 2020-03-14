package de.salomax.sauterschnaeppchen.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.*

@Entity(tableName = "items")
data class Item(
    @ColumnInfo(name = "description")
    var description: String = "",

    @ColumnInfo(name = "condition")
    var condition: Condition? = null,

    @PrimaryKey
    @ColumnInfo(name = "article_number")
    var articleNumber: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "serial_number")
    var serialNumber: String? = null,

    @ColumnInfo(name = "price")
    var price: Float? = null,

    @ColumnInfo(name = "target_system")
    var targetSystem: TargetSystem? = null
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

/**
 *
 */
enum class Condition {
    A, AB, B, BC, C, D;

    override fun toString(): String {
        return when (this) {
            A  -> "Wie neu"
            AB -> "Sehr guter Zustand"
            B  -> "Guter Zustand"
            BC -> "Akzeptabler Zustand"
            C  -> "Stark gebraucht"
            D  -> "Kaputt (?)"
        }
    }

    companion object {
        fun valueOf(value: Int): Condition? = values().find { it.ordinal == value }
    }

}

/**
 *
 */
@Suppress("EnumEntryName", "SpellCheckingInspection", "NonAsciiCharacters")
enum class TargetSystem {
    Canon,
    Fujifilm,
    Leica,
    Nikon,
    Olympus,
    Panasonic,
    Pentax,
    Sony,
    Alpa,

    Bronica,
    Contax,
    Hasselblad,
    Mamiya,
    Minolta,
    Rollei,
    Pentacon,
    Voigtländer,
    Yashica,

    UNKNOWN;

    companion object {

        fun valueOf(value: Int): TargetSystem? = values().find { it.ordinal == value }

        fun find(type: String?): TargetSystem? =
            when {
                type == null -> null
                type.contains("Canon")
                    .or(type.contains("EOS"))
                    .or(type.contains("FD")) -> Canon
                type.contains("Fujifilm")
                    .or(type.contains("Fujiflm"))
                    .or(type.contains("Fuji")) -> Fujifilm
                type.contains("Leica")
                    .or(type.contains("ZM"))
                    .or(type.contains("Schraubleica"))
                    .or(type.contains("Summicron"))
                    .or(type.contains("Sonnar"))
                    .or(type.contains("Summilux")) -> Leica
                type.contains("Nikon")
                    .or(type.contains(" EF")) -> Nikon
                type.contains("Olympus")
                    .or(type.contains("Olmypus")) -> Olympus
                type.contains("Panasonic") -> Panasonic
                type.contains("Pentax") -> Pentax
                type.contains("Sony")
                    .or(type.contains("Batis"))
                    .or(type.contains("E-Mount", ignoreCase = true)) -> Sony
                type.contains("Alpa") -> Alpa
                type.contains("Bronica") -> Bronica
                type.contains("Contax") -> Contax
                type.contains("Hasselblad") -> Hasselblad
                type.contains("Mamiya") -> Mamiya
                type.contains("Minolta") -> Minolta
                type.contains("Rollei")
                    .or(type.contains("QBM")) -> Rollei
                type.contains("Pentacon")
                    .or(type.contains("Praktica")) -> Pentacon
                type.contains("Voigtländer") -> Voigtländer
                type.contains("Yashica") -> Yashica
                type.contains("SB-? ?\\d+".toRegex()) -> Nikon // Nikon flashes: e.g. "SB-28", "SB600", "SB-800", "SB 900"
                else -> UNKNOWN
            }
    }

}
