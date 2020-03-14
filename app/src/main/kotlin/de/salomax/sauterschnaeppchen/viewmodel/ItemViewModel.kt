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

    private var currentSortOrder = 1 // 0=price, 1=description
    private var currentFilter: TargetSystem? = null // TargetSystem

    /*
     * Items =======================================================================================
     */

    init {
        liveItems.addSource(dbLiveItems) { result: Array<Item>? ->
            filterAndSort(result)
        }
    }

    fun getItems(): LiveData<Array<Item>> {
        return liveItems
    }

    fun refreshItems() {
        dbLiveItems = repository.getItems()
    }

    fun filterBy(system: TargetSystem?) = dbLiveItems.value?.let { items ->
        currentFilter = system
        filterAndSort(items)
    }

    fun sortByPrice() = dbLiveItems.value?.let { items ->
        currentSortOrder = 0
        filterAndSort(items)
    }

    fun sortByDescription() = dbLiveItems.value?.let { items ->
        currentSortOrder = 1
        filterAndSort(items)
    }

    /*
     * does the heavy lifting
     */
    private fun filterAndSort(items: Array<Item>?) {
        // filter
        if (currentFilter != null)
            liveItems.value = items
                ?.filter { item -> item.targetSystem == currentFilter }
                ?.toTypedArray()
        else
            liveItems.value = items
        // sort
        when (currentSortOrder) {
            0 -> liveItems.value?.sortBy { item -> item.price }
            1 -> liveItems.value?.sortBy { item -> item.description }
        }
    }

    /*
     * Error =======================================================================================
     */

    fun getError(): LiveData<String?> {
        return liveError
    }

    /*
     * Network =====================================================================================
     */

    fun getNetwork(): LiveData<Boolean> {
        return liveNetwork
    }

}
