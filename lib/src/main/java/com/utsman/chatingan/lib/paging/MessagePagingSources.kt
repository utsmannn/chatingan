package com.utsman.chatingan.lib.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.utsman.chatingan.lib.data.entity.MessageEntity
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.database.ChatinganDao
import kotlinx.coroutines.delay

class MessagePagingSources(
    private val contact: Contact,
    private val dao: ChatinganDao
) : PagingSource<Int, MessageEntity>() {
    override fun getRefreshKey(state: PagingState<Int, MessageEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            return anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override val jumpingSupported: Boolean
        get() = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MessageEntity> {
        val page = params.key ?: 0
        val limit = params.loadSize
        val firstOffset = page * params.loadSize

        return try {
            val data = dao.getAllMessagePaged(contact.id, limit, firstOffset)
            val result = LoadResult.Page(
                data = data,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (data.isEmpty()) null else page + 1
            )
            result
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}