package uz.zero.auth.utils

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringSetConverter : AttributeConverter<Set<String>, String> {
    override fun convertToDatabaseColumn(attribute: Set<String>?): String? {
        return attribute?.joinToString(",")
    }

    override fun convertToEntityAttribute(dbData: String?): Set<String> {
        return dbData?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }
}
