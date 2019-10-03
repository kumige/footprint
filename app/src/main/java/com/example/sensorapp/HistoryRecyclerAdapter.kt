package com.example.sensorapp

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rv_history_row.view.*

class HistoryRecyclerAdapter(private val history: List<History>
) : RecyclerView.Adapter<HistoryRecyclerAdapter.ItemHolder>() {

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val historyItem = history[position]
        holder.bindPhoto(historyItem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryRecyclerAdapter.ItemHolder {
        val inflatedView = parent.inflate(R.layout.rv_history_row, false)
        return ItemHolder(inflatedView)
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    override fun getItemCount() = history.size



    class ItemHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private lateinit var historyRun: History
        init {
            v.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun bindPhoto(historyItem: History) {
            historyRun = historyItem
            val date = historyItem.startTime.split("T")
            val time = date[1].slice(0..4)

            view.textView_date.text = "${date[0]} $time"
            view.textView_distance.text = "${historyItem.distance}m"
            view.textView_time.text = Utils().formatTimer(historyItem.duration, FORMAT_TIMER_PROFILE)
        }

        override fun onClick(v: View) {
            Log.d("dbg", "history item clicked")


            val context = itemView.context
            val showHistoryIntent = Intent(context, SingleRunActivity::class.java)
            showHistoryIntent.putExtra(RUN_KEY, Utils().historyToJsonString(historyRun))
            context.startActivity(showHistoryIntent)

        }

        companion object {
            private val RUN_KEY = "RUN"
        }
    }
}