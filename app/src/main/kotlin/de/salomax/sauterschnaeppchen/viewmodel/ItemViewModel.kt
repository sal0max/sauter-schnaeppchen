package de.salomax.sauterschnaeppchen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.salomax.sauterschnaeppchen.model.Item
import de.salomax.sauterschnaeppchen.repository.ItemRepository
import java.io.InputStream

class ItemViewModel : ViewModel() {
    var repository: ItemRepository = ItemRepository

    var liveData: MutableLiveData<Array<Item>> = MutableLiveData()
    var liveError: MutableLiveData<String> = MutableLiveData()

    fun loadItems(pdf: InputStream){
        liveData.value = repository.getItems(pdf).value
    }

    fun loadItems() {
        repository.getItems {items, error ->
            if (error != null) {
                liveError.postValue(error)
            }
            else
                liveData.postValue(items)
        }
    }

}
