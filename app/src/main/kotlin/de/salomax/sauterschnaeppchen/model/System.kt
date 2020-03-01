package de.salomax.sauterschnaeppchen.model

enum class System {
    Canon,
    Fujifilm,
    Leica,
    Nikon,
    Olympus,
    Panasonic,
    Pentax,
    Sony,

    Bronica,
    Contax,
    Hasselblad,
    Mamiya,
    Minolta,
    Rollei;

    companion object {
        fun find(type: String?): System? =
            when {
                type == null -> null
                type.contains("Canon").or(type.contains(" FD")) -> Canon
                type.contains("Fujifilm") -> Fujifilm
                type.contains("Leica").or(type.contains(" ZM")) -> Leica
                type.contains("Nikon").or(type.contains(" EF")) -> Nikon
                type.contains("Olympus") -> Olympus
                type.contains("Panasonic") -> Panasonic
                type.contains("Pentax") -> Pentax
                type.contains("Sony") -> Sony
                type.contains("Bronica") -> Bronica
                type.contains("Contax") -> Contax
                type.contains("Hasselblad") -> Hasselblad
                type.contains("Mamiya") -> Mamiya
                type.contains("Minolta") -> Minolta
                type.contains("Rollei") -> Rollei
                type.contains("SB-? ?\\d+".toRegex()) -> Nikon // Nikon flashes: e.g. "SB-28", "SB600", "SB-800", "SB 900"
                else -> null
            }
    }

}
