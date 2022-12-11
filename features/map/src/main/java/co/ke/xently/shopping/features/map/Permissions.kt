package co.ke.xently.shopping.features.map

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

object Permissions {
    @JvmInline
    value class Granted(val value: Boolean)

    @Composable
    fun requestLocationPermission(
        vararg permissions: String,
        shouldRequestPermission: Boolean = true,
        onLocationPermissionChanged: (Granted) -> Unit = {},
    ): MultiplePermissionsState {
        var showRationale by rememberSaveable { mutableStateOf(true) }

        val permissionState = rememberMultiplePermissionsState(
            permissions = permissions.toList().ifEmpty {
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            },
        )

        if (!shouldRequestPermission) {
            return permissionState
        }

        val onLocationPermissionChangedUpdated by rememberUpdatedState(newValue = onLocationPermissionChanged)

//    if (!permissionState.permissionRequested) {
        if (!permissionState.allPermissionsGranted) {
            SideEffect(permissionState::launchMultiplePermissionRequest)
        }
        // If the user denied the permission but a rationale should be shown, or the user sees
        // the permission for the first time, explain why the feature is needed by the app and allow
        // the user to be presented with the permission again or to not see the rationale any more.
        else if (permissionState.shouldShowRationale && showRationale) {
            AlertDialog(
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
                onDismissRequest = { showRationale = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            permissionState.launchMultiplePermissionRequest()
                            showRationale = false
                        },
                    ) { Text(stringResource(R.string.request_permission_button_label)) }
                },
                dismissButton = {
                    TextButton(onClick = { showRationale = false }) {
                        Text(stringResource(R.string.never_show_again_button_label))
                    }
                },
                text = { Text(stringResource(R.string.location_permission_rationale)) },
            )
        } else {
            LaunchedEffect(onLocationPermissionChangedUpdated) {
                onLocationPermissionChangedUpdated(Granted(permissionState.allPermissionsGranted))
            }
        }
        return permissionState
    }
}