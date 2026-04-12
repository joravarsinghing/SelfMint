package com.ravaroj.habitcurrency.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ravaroj.habitcurrency.data.local.WalletDataStore
import com.ravaroj.habitcurrency.data.repository.TaskRepository

class TasksViewModelFactory(
    private val taskRepository: TaskRepository,
    private val walletDataStore: WalletDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            return TasksViewModel(taskRepository, walletDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}