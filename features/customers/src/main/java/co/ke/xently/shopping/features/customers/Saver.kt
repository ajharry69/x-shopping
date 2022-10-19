package co.ke.xently.shopping.features.customers

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import co.ke.xently.shopping.libraries.data.source.Customer

val customerSaver: Saver<Customer?, Any> = run {
    val idKey = "id"
    val nameKey = "name"
    val taxPinKey = "taxPin"
    val physicalAddressKey = "physicalAddress"
    val phoneNumberKey = "phoneNumber"
    val exemptionNumberKey = "exemptionNumber"
    mapSaver(
        save = {
            mapOf(
                idKey to it?.id,
                nameKey to it?.name,
                taxPinKey to it?.taxPin,
                physicalAddressKey to it?.physicalAddress,
                phoneNumberKey to it?.phoneNumber,
                exemptionNumberKey to it?.exemptionNumber,
            )
        },
        restore = {
            if (it[idKey] == null) {
                null
            } else {
                Customer(
                    id = it[idKey].toString().toInt(),
                    name = it[nameKey].toString(),
                    taxPin = it[taxPinKey].toString(),
                    physicalAddress = it[physicalAddressKey]?.toString(),
                    phoneNumber = it[phoneNumberKey]?.toString(),
                    exemptionNumber = it[exemptionNumberKey]?.toString() ?: "",
                )
            }
        },
    )
}