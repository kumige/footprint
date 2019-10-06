package com.example.sensorapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.sensorapp.*
import com.example.sensorapp.adapters.MainRecyclerAdapter
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_history.*
import org.jetbrains.anko.doAsync

class HistoryFragment: Fragment() {

    private lateinit var history: List<History>

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: MainRecyclerAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = rootView.findViewById(R.id.fragmentHistoryRecyclerView)
        linearLayoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(activity!!.applicationContext, DividerItemDecoration.VERTICAL))
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfileData()

    }


    private fun loadProfileData() {
        val db = Room.databaseBuilder(
            activity!!.applicationContext,
            AppDatabase::class.java, "user.db"
        ).build()

        doAsync {
            //db.dao().insertRun(History(0, "${LocalDateTime.now()}", 344, 1337))

            history = db.dao().getAllHistory()

            adapter = MainRecyclerAdapter(history)
            fragmentHistoryRecyclerView.adapter = adapter
        }
    }
}