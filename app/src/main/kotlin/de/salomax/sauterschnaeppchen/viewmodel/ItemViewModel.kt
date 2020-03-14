package de.salomax.sauterschnaeppchen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import de.salomax.sauterschnaeppchen.data.Item
import de.salomax.sauterschnaeppchen.data.TargetSystem
import de.salomax.sauterschnaeppchen.repository.ItemRepository

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: ItemRepository = ItemRepository.getInstance(application)

    private var dbLiveItems = repository.getItems()
    private val liveItems = MediatorLiveData<Array<Item>>()
    private val liveError = repository.getError()
    private val liveNetwork = repository.getNetwork()

//    private var currentSortOrder = "description" // price, description
    private var currentFilter: TargetSystem? = null // TargetSystem

    init {
        liveItems.addSource(dbLiveItems) { result: Array<Item>? ->
            result?.let { items ->
                if (currentFilter != null)
                    liveItems.value = items
                        .filter { item -> item.targetSystem == currentFilter }
                        .toTypedArray()
                else
                    liveItems.value = items
            }
        }
    }

    fun getItems(): LiveData<Array<Item>> {
        return liveItems
    }

    fun filterBy(system: TargetSystem?) = dbLiveItems.value?.let { items ->
        if (system == null)
            liveItems.value = items
        else liveItems.value = items
            .filter { item -> item.targetSystem == system }
            .toTypedArray()
    }.also { currentFilter = system }

    fun refreshItems() {
        dbLiveItems = repository.getItems()
    }

    fun getError(): LiveData<String?> {
        return liveError
    }

    fun getNetwork(): LiveData<Boolean> {
        return liveNetwork
    }


}
