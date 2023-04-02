package io.github.afalabarce.jetpackcompose.svg

import javax.xml.parsers.DocumentBuilderFactory

typealias ResourceEntry = Pair<String, String>

class AndroidResourceParser(private val drawableResource: String) {
    private val factory = DocumentBuilderFactory.newInstance()
    private val builder = factory.newDocumentBuilder()
    private val drawable by lazy {
        builder.parse(drawableResource.byteInputStream(Charsets.UTF_8)).apply { documentElement.normalize() }
    }

    fun values(type: String): Iterable<ResourceEntry> = drawable
        .getElementsByTagName(type).iterable.map { node ->
            ResourceEntry(
                node.attributes["name"]!!,
                node.textContent
            )
        }
}