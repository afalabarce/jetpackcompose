package io.github.afalabarce.jetpackcompose.svg

class ResourceCollector {
    private val resources = mutableMapOf<String, String>()

    fun addResources(values: Iterable<ResourceEntry>) {
        resources.putAll(values)
    }

    fun getValue(name: String): String? {
        var curName = name

        do {
            val value = resources[curName] ?: return null

            if (!value.startsWith("@")) {
                return value
            }

            curName = value.split("/").last()
        } while (true)
    }
}