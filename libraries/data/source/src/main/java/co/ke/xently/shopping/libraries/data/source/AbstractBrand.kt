package co.ke.xently.shopping.libraries.data.source

abstract class AbstractBrand {
    abstract val name: String

    override fun toString() = name

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractBrand

        if (name != other.name) return false

        return true
    }
}