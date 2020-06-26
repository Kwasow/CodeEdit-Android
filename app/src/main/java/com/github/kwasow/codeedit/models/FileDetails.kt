package com.github.kwasow.codeedit.models

data class FileDetails(
    val name: String,
    val type: Type
) : Comparable<FileDetails> {

    enum class Type(private val value: Int) {
        DIRECTORY(0), TEXT(1), BINARY(2), OTHER(3);

        fun getValue(): Int {
            return value
        }
    }

    override fun compareTo(other: FileDetails): Int {
        return this.type.getValue().compareTo(other.type.getValue())
    }

}