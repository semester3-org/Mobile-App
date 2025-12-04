package com.apk.koshub.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.NotificationAdapter
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.NotificationViewModel
import com.apk.koshub.utils.SharedPrefHelper

class NotifFragment : Fragment() {

    private lateinit var viewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter

    private lateinit var db: DatabaseHelper
    private lateinit var pref: SharedPrefHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_notification, container, false)

        db = DatabaseHelper(requireContext())
        pref = SharedPrefHelper(requireContext())

        val rv = v.findViewById<RecyclerView>(R.id.rvNotifications)
        val backBtn = v.findViewById<View>(R.id.btnBackNotif)
        val markAll = v.findViewById<TextView>(R.id.markRead)

        val userId = db.getUser()?.id ?: pref.getUserId()
        Log.d("NOTIF_DEBUG", "NotifFragment userId=$userId")

        if (userId <= 0) {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show()
            return v
        }

        backBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotificationAdapter(mutableListOf()) { clickedItem ->
            viewModel.markAsRead(clickedItem.id)
        }
        rv.adapter = adapter

        markAll.setOnClickListener {
            viewModel.markAllRead(userId)
        }

        viewModel.notifications.observe(viewLifecycleOwner) { list ->
            Log.d("NOTIF_DEBUG", "observe size=${list.size}")
            adapter.submitList(list)
        }

        viewModel.errorMsg.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Notif error: $msg", Toast.LENGTH_SHORT).show()
                Log.e("NOTIF_DEBUG", "error=$msg")
            }
        }

        viewModel.loadNotifications(userId)

        return v
    }
}
