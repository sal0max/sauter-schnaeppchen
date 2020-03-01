package de.salomax.sauterschnaeppchen.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import de.salomax.sauterschnaeppchen.R
import de.salomax.sauterschnaeppchen.model.Item
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.text.NumberFormat
import java.util.*

class MyAdapter :
    RecyclerView.Adapter<MyAdapter.ViewHolder>(), PopupTextProvider {

    private var items: Array<Item>? = null

    fun setData(items: Array<Item>?) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item,
            parent,
            false
        ) as ConstraintLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.get(position)?.let { holder.bind(it) }
        getItemId(position)
    }

    override fun getItemCount() = items?.size ?: 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val desc: TextView = itemView.findViewById(R.id.description)
        private val serialNr: TextView = itemView.findViewById(R.id.serialNumber)
        private val condition: TextView = itemView.findViewById(R.id.condition)
        private val itemNr: TextView = itemView.findViewById(R.id.articleNumber)
        private val price: TextView = itemView.findViewById(R.id.price)

        fun bind(item: Item) {
            desc.text = item.description
            serialNr.text = item.serialNumber?.let { "S/N: $it" }
            condition.text = item.condition?.toString()
            itemNr.text = item.articleNumber
            price.text =
                item.price?.let { NumberFormat.getCurrencyInstance(Locale.GERMANY).format(it) }
        }
    }

    override fun getPopupText(position: Int): String {
        return items?.get(position)?.description?.substring(0, 1) ?: ""
    }

}
