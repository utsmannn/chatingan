package com.utsman.chatingan.lib.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageEntity
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.transaction.ContactAndLastMessage
import com.utsman.chatingan.lib.data.transaction.MessageAndSender
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatinganDao {

    /**
     * Contact
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateContact(contact: ContactEntity)

    @Query("DELETE FROM ContactEntity WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)

    @Query("SELECT * FROM ContactEntity")
    fun getAllContact(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM ContactEntity WHERE id = :contactId")
    fun getContactFlow(contactId: String): Flow<ContactEntity>

    @Query("SELECT * FROM ContactEntity WHERE id = :contactId")
    suspend fun getContactById(contactId: String): ContactEntity?

    @Query("SELECT * FROM ContactEntity WHERE email = :email")
    suspend fun getContactByEmail(email: String): ContactEntity?

    @Query("SELECT EXISTS(SELECT * FROM ContactEntity WHERE email = :email)")
    suspend fun isContactExist(email: String): Boolean

    @Query("SELECT * FROM ContactEntity ORDER BY lastMessageUpdate DESC")
    fun getContactAndLastMessages(): Flow<List<ContactAndLastMessage>>

    @Query("UPDATE ContactEntity SET isTyping = :isTyping WHERE email = :email")
    suspend fun updateTypingByEmail(email: String, isTyping: Boolean)

    /**
     * Messages
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: MessageEntity)

    @Update
    suspend fun updateMessage(messageEntity: MessageEntity)

    @Query("UPDATE MessageEntity SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM MessageEntity WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("SELECT * FROM MessageEntity WHERE :receiverId IN (receiverId, senderId) ORDER BY date")
    fun getAllMessage(receiverId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM MessageEntity WHERE receiverId = :receiverId")
    fun getMessagesFromReceiver(receiverId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM MessageEntity WHERE senderId = :senderId")
    fun getMessagesFromSender(senderId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM MessageEntity WHERE receiverId = :receiverId ORDER BY date LIMIT 1")
    fun getLastMessage(receiverId: String): Flow<MessageEntity>

    @Query("SELECT COUNT(*) FROM MessageEntity WHERE senderId = :senderId AND status != :readStatus")
    suspend fun getUnreadCount(senderId: String, readStatus: String): Int

    @Transaction
    @Query("SELECT * FROM MessageEntity WHERE id = :messageId")
    fun getMessageAndSender(messageId: String): Flow<MessageAndSender>

    @Query("SELECT EXISTS(SELECT * FROM MessageEntity WHERE id = :messageId AND status = :statusString)")
    suspend fun isMessageMatchStatus(messageId: String, statusString: String): Boolean

    @Query("SELECT * FROM MessageEntity WHERE :contactId IN (receiverId, senderId) AND status = :statusString ORDER BY date")
    fun getMessagesByStatus(contactId: String, statusString: String): Flow<List<MessageEntity>>
}