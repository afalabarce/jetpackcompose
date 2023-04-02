package io.github.afalabarce.jetpackcompose.svg

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList

operator fun NamedNodeMap.get(name: String): String? = getNamedItem(name)?.nodeValue

fun NamedNodeMap.get(namespaceUri: String?, name: String): String? = getNamedItemNS(namespaceUri, name)?.nodeValue

class NodeListIterator(private val nodeList: NodeList) : Iterator<Node> {
    private var position = 0

    override fun hasNext(): Boolean {
        return position < nodeList.length
    }

    override fun next() = nodeList.item(position++)!!
}

val NodeList.iterable: Iterable<Node>
    get() {
        return object : Iterable<Node> {
            override fun iterator(): Iterator<Node> {
                return iterator
            }
        }
    }

val NodeList.iterator: Iterator<Node> get() = NodeListIterator(this)