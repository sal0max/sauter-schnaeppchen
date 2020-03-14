package de.salomax.sauterschnaeppchen.view

import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.salomax.sauterschnaeppchen.R
import de.salomax.sauterschnaeppchen.data.SharedPreferenceStringLiveData
import de.salomax.sauterschnaeppchen.viewmodel.ItemViewModel
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var errorView: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        errorView = findViewById(R.id.errorView)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        // list
        recyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(createDivider(context))
            layoutManager = LinearLayoutManager(context)
            adapter = MyAdapter()
            FastScrollerBuilder(this).apply {
                useMd2Style()
            }.build()
        }

        // data
        val model = ViewModelProviders.of(this).get(ItemViewModel::class.java)
        // items
        model.getItems().observe(this, Observer {
            errorView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            (recyclerView.adapter as MyAdapter).setData(it)
        })
        // error
        model.getError().observe(this, Observer {
            if (!it.isNullOrBlank()) {
                recyclerView.visibility = View.GONE
                errorView.apply {
                    visibility = View.VISIBLE
                    text = it
                }
            }
        })
        // swipeRefresh
        model.getNetwork().observe(this, Observer {
            swipeRefresh.isRefreshing = it
        })

        // swipe2refresh
        swipeRefresh.apply {
            setColorSchemeColors(
                getColor(R.color.colorPrimary),
                getColor(R.color.colorAccent)
            )
            setOnRefreshListener { model.refreshItems() }
        }

        // title
        SharedPreferenceStringLiveData(PreferenceManager.getDefaultSharedPreferences(this), "pdfTitle", null).observe(this, Observer {
            title =
                if (it != null) getString(R.string.app_title, it)
                else getString(R.string.app_name)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.openGebrauchtpreislisteNikon -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.fotoversicherung.com/fotoversicherung/gebrauchtpreisliste-nikon/"))
                ContextCompat.startActivity(this, intent, null)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createDivider(context: Context): DividerItemDecoration {
        val a: TypedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        val divider = a.getDrawable(0)
        val inset = resources.getDimensionPixelSize(R.dimen.padding2x)
        val insetDivider = InsetDrawable(divider, inset, 0, inset, 0)
        a.recycle()

        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(insetDivider)
        return itemDecoration
    }

}
