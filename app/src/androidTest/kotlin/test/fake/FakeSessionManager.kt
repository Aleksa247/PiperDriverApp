package test.fake

import com.piperrideshare.driver.services.session.ISessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionManager : ISessionManager {
    private val _token = MutableStateFlow<String?>(null)
    private val _userId = MutableStateFlow<String?>(null)
    private val _name = MutableStateFlow<String?>(null)
    private val _fcmToken = MutableStateFlow<String?>(null)

    override val userToken: StateFlow<String?> get() = _token
    override val userId: StateFlow<String?> get() = _userId
    override val name: StateFlow<String?> get() = _name
    override val fcmToken: StateFlow<String?> get() = _fcmToken

    override suspend fun saveAuthInfo(
        token: String,
        userId: String?,
        name: String?,
    ) {
        _token.value = token
        _userId.value = userId
        _name.value = name
    }

    override suspend fun saveFcmToken(token: String) {
        _fcmToken.value = token
    }

    override suspend fun clearSession() {
        _token.value = null
        _userId.value = null
        _name.value = null
        _fcmToken.value = null
    }
}
