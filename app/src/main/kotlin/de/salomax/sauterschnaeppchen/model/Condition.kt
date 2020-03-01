package de.salomax.sauterschnaeppchen.model

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
}
