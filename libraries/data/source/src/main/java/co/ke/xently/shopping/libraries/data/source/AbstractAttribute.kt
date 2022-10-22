package co.ke.xently.shopping.libraries.data.source

abstract class AbstractAttribute {
    abstract val name: String
    abstract val value: String
    override fun toString() = "${name}:${value}"
}