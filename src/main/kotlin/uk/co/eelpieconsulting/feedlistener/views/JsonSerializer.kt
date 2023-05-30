package uk.co.eelpieconsulting.feedlistener.views

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule


class JsonSerializer {

    private val mapper = ObjectMapper().registerKotlinModule()

    init {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
    }

    fun serialize(`object`: Any?): String {
        return try {
            mapper.writeValueAsString(`object`)
        } catch (var3: JsonProcessingException) {
            throw RuntimeException(var3)
        }
    }
}
