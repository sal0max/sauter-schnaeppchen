package de.salomax.sauterschnaeppchen.view

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import de.salomax.sauterschnaeppchen.R
import de.salomax.sauterschnaeppchen.data.Item
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
        private val btnExpand: ImageButton = itemView.findViewById(R.id.btnExpand)

        fun bind(item: Item) {
            desc.text = item.description
            serialNr.text = item.serialNumber?.let { "S/N: $it" }
            condition.text = item.condition?.toString()
            itemNr.text = item.articleNumber
            price.text = item.price?.let { NumberFormat.getCurrencyInstance(Locale.GERMANY).format(it) }
            btnExpand.setOnClickListener { btnView ->
                //creating a popup menu
                val popup = PopupMenu(itemView.context, btnView)
                //inflating menu from xml resource
                popup.inflate(R.menu.options_menu)
                //adding click listener
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.searchGoogle -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/search?q=${item.description}"))
                            startActivity(itemView.context, intent, null)
                            true
                        }
                        R.id.searchDslrForum -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cse.google.com/cse?cx=partner-pub-7419312382987712%3A6700817117&q=${item.description}"))
                            startActivity(itemView.context, intent, null)
                            true
                        }
                        else -> false
                    }
                }
                //displaying the popup
                popup.show()
            }
        }
    }

    override fun getPopupText(position: Int): String {
        return items?.get(position)?.description?.substring(0, 1) ?: ""
    }

}
