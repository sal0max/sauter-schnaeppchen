package de.salomax.sauterschnaeppchen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.salomax.sauterschnaeppchen.data.Item
import de.salomax.sauterschnaeppchen.repository.ItemRepository

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: ItemRepository = ItemRepository.getInstance(application)

    private var liveItems: LiveData<Array<Item>> = repository.getItems()
    private var liveError: LiveData<String?> = repository.getError()
    private var liveNetwork: LiveData<Boolean> = repository.getNetwork()

    fun getItems(): LiveData<Array<Item>> {
        return liveItems
    }

    fun refreshItems() {
        liveItems = repository.getItems()
    }

    fun getError(): LiveData<String?> {
        return liveError
    }

    fun getNetwork(): LiveData<Boolean> {
        return liveNetwork
    }


}
