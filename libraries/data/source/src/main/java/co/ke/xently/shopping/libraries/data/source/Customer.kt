package co.ke.xently.shopping.libraries.data.source

import co.ke.xently.shopping.libraries.data.source.local.CustomerEntity

data class Customer(
    val id: Int,
    val name: String,
    val taxPin: String,
    val physicalAddress: String?,
    val phoneNumber: String?,
    val exemptionNumber: String,
) {
    val asEntity
        get() = CustomerEntity(
            id = id,
            name = name,
            taxPin = taxPin,
            physicalAddress = physicalAddress,
            phoneNumber = phoneNumber,
            exemptionNumber = exemptionNumber,
        )

    override fun toString(): String {
        return "$name • $taxPin"
    }

    companion object {
        val DEFAULT_INSTANCE = Customer(
            id = 0,
            name = "John Doe",
            taxPin = "A000123456B",
            physicalAddress = null,
            phoneNumber = null,
            exemptionNumber = "",
        )
    }
}

val CustomerEntity.asUIInstance
    get() = Customer(
        id = id,
        name = name,
        taxPin = taxPin,
        physicalAddress = physicalAddress,
        phoneNumber = phoneNumber,
        exemptionNumber = exemptionNumber,
    )
