package ee.ttu.schedule.model

import java.io.Serializable

data class Event(
        val dateStart: Long,
        val dateEnd: Long,
        val description: String,
        val location: String,
        val summary: String,
        val id: Int? = null
) : Serializable