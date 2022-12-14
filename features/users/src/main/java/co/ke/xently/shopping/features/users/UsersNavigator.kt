package co.ke.xently.shopping.features.users

import com.ramcosta.composedestinations.navigation.DestinationsNavigator

interface UsersNavigator : DestinationsNavigator {
    /**
     * @see [DestinationsNavigator.popBackStack]
     */
    fun navigateToMainScreen(): Boolean
}