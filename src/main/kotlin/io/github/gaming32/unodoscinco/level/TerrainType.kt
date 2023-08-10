package io.github.gaming32.unodoscinco.level

enum class TerrainType {
    DEFAULT,
    FLAT,
    DEFAULT_1_1;

    companion object {
        val byId = buildMap {
            TerrainType.entries.forEach { put(it.id, it) }
        }.toSortedMap(String.CASE_INSENSITIVE_ORDER)
    }

    val id = name.lowercase()

    override fun toString() = id
}
