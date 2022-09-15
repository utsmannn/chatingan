package com.utsman.chatingan.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.main.repository.MainRepository
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class MainViewModel(
    private val repository: MainRepository
) : ViewModel() {
    val userState = repository.userState.asLiveData(viewModelScope.coroutineContext)

    fun checkUser() = viewModelScope.launch {
        repository.checkUser()
    }


    companion object : KoinInjector {
        private const val DUMMY_TOKEN = "cAkWaq8RTomSDObkjFEh0m:APA91bHNAXSZY5vmblgw07K9rrown06VlASV4XCHfaBLhXwAlDPztQdTLWP5kxQeh2PFW-Oh-zZRLbnOnyawSzXqWsdJb-HBSquFj2oj564gAkpUQl0988Qkl_ciDNz9kRrJbvXn5Sew"

        override fun inject(): Module {
            return module {
                viewModel { MainViewModel(get()) }
            }
        }
    }
}