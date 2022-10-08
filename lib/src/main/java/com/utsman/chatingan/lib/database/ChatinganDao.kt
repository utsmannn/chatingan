package com.utsman.chatingan.lib.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageEntity
import com.utsman.chatingan.lib.data.entity.MessageInfoEntity
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.data.transaction.MessagesInfoAndReceiverContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatinganDao {

    /**
     * Contact
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("DELETE FROM ContactEntity WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)

    @Query("SELECT * FROM ContactEntity")
    fun getAllContact(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM ContactEntity WHERE id = :contactId")
    suspend fun getContactById(contactId: String): ContactEntity?

    @Query("SELECT * FROM ContactEntity WHERE email = :email")
    suspend fun getContactByEmail(email: String): ContactEntity?

    @Query("SELECT EXISTS(SELECT * FROM ContactEntity WHERE email = :email)")
    suspend fun isContactExist(email: String): Boolean

    /**
     * MessageInfo
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageInfo(messageInfo: MessageInfoEntity)

    @Update
    suspend fun updateMessageInfo(messageInfo: MessageInfoEntity)

    @Query("SELECT * FROM MessageInfoEntity WHERE id = :messageInfoId")
    suspend fun getMessageInfoById(messageInfoId: String): MessageInfoEntity

    @Query("DELETE FROM MessageInfoEntity WHERE id = :messageInfoId")
    suspend fun deleteMessageInfo(messageInfoId: String)

    @Transaction
    @Query("SELECT * FROM ContactEntity")
    fun getAllMessageInfoAndReceiverContact(): Flow<List<MessagesInfoAndReceiverContact>>

    // @Query(
    //    "SELECT * FROM user" +
    //    "JOIN book ON user.id = book.user_id"
    //)
    //fun loadUserAndBookNames(): Map<User, List<Book>>
    /*@Query(
        "SELECT * FROM MessageInfoEntity JOIN ContactEntity ON receiverId = ContactEntity.id"
    )
    fun getAllMessageInfoAndReceiverContact(): Flow<Map<MessageInfoEntity, ContactEntity>>*/

    @Transaction
    @Query("SELECT * FROM ContactEntity WHERE id = :messageInfoId")
    suspend fun getMessageInfoAndReceiverContact(messageInfoId: String): MessagesInfoAndReceiverContact

    @Query("SELECT EXISTS(SELECT * FROM MessageInfoEntity WHERE id = :messageInfoId)")
    suspend fun isMessageInfoExist(messageInfoId: String): Boolean

    /**
     * Messages
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: MessageEntity)

    @Update
    suspend fun updateMessage(messageEntity: MessageEntity)

    @Query("DELETE FROM MessageEntity WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("SELECT * FROM MessageEntity WHERE messageInfoId = :messageInfoId ORDER BY date")
    fun getAllMessage(messageInfoId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM MessageEntity WHERE messageInfoId = :messageInfoId ORDER BY date LIMIT 1")
    fun getLastMessage(messageInfoId: String): Flow<MessageEntity>
}