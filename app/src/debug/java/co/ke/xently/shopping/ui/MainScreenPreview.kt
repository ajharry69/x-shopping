package co.ke.xently.shopping.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavOptionsBuilder
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.shopping.MainActivityViewModel
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.features.shoppinglist.repositories.IShoppingListRepository
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListGroup
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.GroupedShoppingListViewModel
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.users.BasicAuth
import co.ke.xently.shopping.features.users.repositories.IUserRepository
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import co.ke.xently.shopping.libraries.data.source.User
import co.ke.xently.shopping.navigation.Navigator
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@XentlyPreview
@Composable
private fun MainScreenPreview() {
    XentlyTheme {
        val navigator = object : DestinationsNavigator {
            override fun clearBackStack(route: String): Boolean {
                return true
            }

            override fun navigate(
                route: String,
                onlyIfResumed: Boolean,
                builder: NavOptionsBuilder.() -> Unit,
            ) {

            }

            override fun navigateUp(): Boolean {
                return true
            }

            override fun popBackStack(): Boolean {
                return true
            }

            override fun popBackStack(
                route: String,
                inclusive: Boolean,
                saveState: Boolean,
            ): Boolean {
                return true
            }
        }
        val userRepository = object : IUserRepository {
            override fun getAuthenticated(): Flow<Result<User?>> {
                return flowOf()
            }

            override fun signUp(user: User): Flow<Result<User>> {
                return flowOf()
            }

            override fun signIn(basicAuth: BasicAuth): Flow<Result<User?>> {
                return flowOf()
            }

            override fun signOut(): Flow<Result<Long>> {
                return flowOf()
            }

            override fun resetPassword(resetPassword: User.ResetPassword): Flow<Result<User>> {
                return flowOf()
            }

            override fun requestTemporaryPassword(email: String): Flow<Result<User>> {
                return flowOf()
            }

            override fun requestVerificationCode(): Flow<Result<User>> {
                return flowOf()
            }

            override fun verifyAccount(code: String): Flow<Result<User>> {
                return flowOf()
            }
        }
        val repository = object : IShoppingListRepository {
            override fun save(shoppingListItem: ShoppingListItem): Flow<Result<ShoppingListItem>> {
                return flowOf()
            }

            override fun get(
                config: PagingConfig,
                groupBy: GroupBy,
            ): Flow<PagingData<GroupedShoppingList>> {
                return flowOf()
            }

            override fun get(id: Long): Flow<Result<ShoppingListItem?>> {
                return flowOf()
            }

            override fun get(
                config: PagingConfig,
                group: ShoppingListGroup?,
            ): Flow<PagingData<ShoppingListItem>> {
                return flowOf()
            }

            override fun remove(id: Long): Flow<Result<Unit>> {
                return flowOf()
            }

            override fun getCount(groupBy: GroupBy): Flow<Map<Any, Int>> {
                return flowOf()
            }

        }
        MainScreen(
            shared = Shared(user = User.DEFAULT_INSTANCE.copy(name = "John Doe")),
            navigator = Navigator(navigator),
            viewModel = MainActivityViewModel(userRepository),
            gslViewModel = GroupedShoppingListViewModel(repository),
        )
    }
}