package com.utsman.chatingan.lib.receiver

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.lib.Chatingan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import java.lang.reflect.Type

class ChatinganReceiver(private val context: Context) {

    private val preferences by lazy {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private val isDirectToChatingan: Boolean
        get() = Chatingan.getSafeInstance() != null

    private fun listNotifierType(): Type {
        return object : TypeToken<List<MessageNotifier>>() {}.type
    }

    private fun convertFromString(rawString: String?): List<MessageNotifier> {
        return if (rawString.isNullOrEmpty()) {
            emptyList()
        } else {
            Gson().fromJson(rawString, listNotifierType())
        }
    }

    private fun convertFromMessageNotifier(notifiers: List<MessageNotifier>): String {
        return Gson().toJson(notifiers, listNotifierType())
    }

    private val _messagesFlow: MutableStateFlow<MessageNotifier?> = MutableStateFlow(null)

    internal val messagesFlow: Flow<MessageNotifier>
        get() {
            return if (isDirectToChatingan) {
                _messagesFlow.filterNotNull()
            } else {
                flowFromPreferences()
            }
        }

    private fun flowFromPreferences(): Flow<MessageNotifier> {
        val currentListString = preferences.getString(MESSAGE_KEY, "").orEmpty()
        val currentList = convertFromString(currentListString)
        return currentList.asFlow()
    }

    fun addMessageNotifier(notifier: MessageNotifier) {
        if (isDirectToChatingan) {
            _messagesFlow.value = notifier
        } else {
            emitToPreferences(notifier)
        }
    }

    private fun emitToPreferences(notifier: MessageNotifier) {
        val currentListString = preferences.getString(MESSAGE_KEY, "").orEmpty()
        val currentList = convertFromString(currentListString) + notifier
        val newListString = convertFromMessageNotifier(currentList)
        preferences.edit()
            .putString(MESSAGE_KEY, newListString)
            .apply()
    }

    fun clearPrefReceiver() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val MESSAGE_KEY = "message"
        private const val PREFERENCES_NAME = "messages_queue"
    }
}