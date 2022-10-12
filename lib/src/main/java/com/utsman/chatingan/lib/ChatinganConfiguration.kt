package com.utsman.chatingan.lib

import android.content.Context
import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.model.Contact

data class ChatinganConfiguration(
    val fcmServerKey: String
) {

    internal data class ConfigData(
        val fcmServerKey: String,
        val contact: Contact
    ) {

        fun toConfiguration(): ChatinganConfiguration {
            return ChatinganConfiguration(fcmServerKey).also {
                it._contact = contact
            }
        }
    }

    private var _contact: Contact = Contact.empty()
    val contact: Contact
        get() = _contact

    internal fun updateContact(newContact: Contact) {
        _contact = newContact
    }

    private fun toConfigData(): ConfigData {
        return ConfigData(fcmServerKey, contact)
    }

    fun savePref(context: Context) {
        ChatinganPreferences.save(context, PREF_KEY, this.toConfigData())
    }

    override fun toString(): String {
        return "ChatinganConfiguration(fcmServerKey=$fcmServerKey,contact=$contact)"
    }

    companion object {
        private const val PREF_KEY = "config"

        fun getPref(context: Context): ChatinganConfiguration {
            val configData = ChatinganPreferences.read<ConfigData>(context, PREF_KEY)
            return configData?.toConfiguration() ?: throw ChatinganException("No config saved!")
        }
    }
}