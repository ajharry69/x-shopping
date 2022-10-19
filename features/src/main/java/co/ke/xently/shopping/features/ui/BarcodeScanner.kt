package co.ke.xently.shopping.features.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.ke.xently.shopping.features.R
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class BarcodeResult {
    data class Success(val rawValue: String) : BarcodeResult()
    data class Error(val exception: MlKitException) : BarcodeResult() {
        operator fun invoke(): MlKitException {
            return exception
        }

        operator fun invoke(context: Context): String {
            return when (exception.errorCode) {
                MlKitException.CODE_SCANNER_CANCELLED -> context.getString(R.string.error_barcode_scanner_cancelled)
                MlKitException.CODE_SCANNER_CAMERA_PERMISSION_NOT_GRANTED ->
                    context.getString(R.string.error_barcode_scanner_camera_permission_not_granted)
                MlKitException.CODE_SCANNER_APP_NAME_UNAVAILABLE ->
                    context.getString(R.string.error_barcode_scanner_app_name_unavailable)
                else -> context.getString(R.string.error_barcode_scanner_default_message, this)
            }
        }
    }
}

@Composable
fun rememberBarcodeScanner(): GmsBarcodeScanner {
    val context = LocalContext.current
    return remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_CODE_128,
            )
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }
}

suspend fun GmsBarcodeScanner.awaitBarcodeResult(playTone: Boolean = true) =
    suspendCancellableCoroutine<BarcodeResult> { continuation ->
        startScan().addOnSuccessListener { barcode: Barcode ->
            val success = BarcodeResult.Success(barcode.rawValue!!).also {
                if (playTone) {
                    ToneGenerator(AudioManager.STREAM_RING, 100)
                        .startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                }
            }
            continuation.resume(success)
        }.addOnFailureListener { e: Exception ->
            continuation.resume(BarcodeResult.Error(e as MlKitException))
        }
    }
