package com.apk.koshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.NotificationAdapter
import com.apk.koshub.models.NotificationItem
import com.apk.koshub.models.NotificationViewModel
import com.apk.koshub.fragments.HomeFragment
import android.widget.TextView

class NotifFragment : Fragment() {

    private lateinit var viewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter

    private val userId = 36

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_notification, container, false)
        val rv = v.findViewById<RecyclerView>(R.id.rvNotifications)
        val backBtn = v.findViewById<View>(R.id.btnBackNotif)
        val markAll = v.findViewById<TextView>(R.id.markRead)

        backBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
        markAll.setOnClickListener {
            viewModel.markAllRead(userId)
        }

        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotificationAdapter { item ->
            viewModel.markAsRead(item.id)
        }
        rv.adapter = adapter

        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        viewModel.notifications.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        viewModel.loadNotifications(userId)

        return v
    }
}

