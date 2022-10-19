package co.ke.xently.shopping.features.users.repositories.exceptions

import co.ke.xently.shopping.libraries.data.source.remote.HttpException

internal class VerificationHttpException(val code: List<String> = emptyList()) : HttpException()