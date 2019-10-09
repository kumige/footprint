package com.example.sensorapp

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.android.synthetic.main.rv_history_row.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import kotlin.coroutines.coroutineContext

class HistoryRecyclerAdapter(
    private var history: MutableList<History>
) : RecyclerView.Adapter<HistoryRecyclerAdapter.ItemHolder>() {

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val historyItem = history.reversed()[position]
        holder.bindItem(historyItem, position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryRecyclerAdapter.ItemHolder {
        val inflatedView = parent.inflate(R.layout.rv_history_row, false)
        return ItemHolder(inflatedView, this)
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    override fun getItemCount() = history.size

    // Delete item from list and update recycler view
    private fun onItemDelete(position: Int) {
        val ints = mutableListOf<Int>()
        var i = 0
        while (history.size > i) {
            ints.add(i)
            i++
        }
        val reversedInts = ints.reversed()
        history.removeAt(reversedInts[position])
        doAsync {
            uiThread {
                notifyDataSetChanged()
            }
        }
    }


    class ItemHolder(private var view: View, private var adapter: HistoryRecyclerAdapter) :
        RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener,
        PopupMenu.OnMenuItemClickListener {

        private lateinit var historyRun: History
        private var itemPosition = 0

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun bindItem(historyItem: History, position: Int) {
            historyRun = historyItem
            itemPosition = position
            val date = historyItem.startTime.split("T")
            val time = date[1].slice(0..4)

            view.textView_date.text = "${date[0]} $time"
            view.textView_distance.text = "${historyItem.distance}m"
            view.textView_time.text =
                Utils().formatTimer(historyItem.duration, FORMAT_TIMER_PROFILE)
        }

        override fun onClick(v: View) {
            val context = itemView.context
            val showHistoryIntent = Intent(context, SingleRunActivity::class.java)
            showHistoryIntent.putExtra(RUN_KEY, Utils().historyToJsonString(historyRun))
            context.startActivity(showHistoryIntent)
        }

        override fun onLongClick(v: View?): Boolean {
            val popup = PopupMenu(v!!.context, v)
            popup.setOnMenuItemClickListener(this)
            popup.inflate(R.menu.popup_delete)
            popup.show()
            return true
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            val db = Room.databaseBuilder(
                view.context,
                AppDatabase::class.java, "user.db"
            ).build()

            doAsync {
                db.dao().deleteFromHistory(historyRun.id)

                adapter.onItemDelete(itemPosition)
            }
            return true
        }

        companion object {
            private val RUN_KEY = "RUN"
        }
    }
}