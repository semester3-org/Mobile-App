package com.apk.koshub.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosCardAdapter
import com.apk.koshub.api.ApiClient
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.KosItemCard
import com.apk.koshub.models.KosResponse
import com.apk.koshub.models.toKosItemCardForFavorite
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoriteFragment : Fragment() {

    private lateinit var rvFavorite: RecyclerView
    private lateinit var tvEmptyFavorite: TextView
    private lateinit var adapter: KosCardAdapter
    private lateinit var db: DatabaseHelper

    private val favoriteKos = mutableListOf<KosItemCard>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        rvFavorite = view.findViewById(R.id.rvFavorite)
        tvEmptyFavorite = view.findViewById(R.id.tvEmptyFavorite)
        db = DatabaseHelper(requireContext())

        adapter = KosCardAdapter(favoriteKos) { kos ->
            // TODO: klik detail kalau perlu
        }

        rvFavorite.layoutManager = LinearLayoutManager(requireContext())
        rvFavorite.adapter = adapter

        loadFavoriteFromServer()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadFavoriteFromServer() // refresh tiap balik ke fragment ini
    }

    private fun loadFavoriteFromServer() {
        val user = db.getUser()

        if (user == null) {
            favoriteKos.clear()
            adapter.updateList(favoriteKos)
            updateUI()
            return
        }

        ApiClient.instance.getFavoriteKos(userId = user.id)
            .enqueue(object : Callback<KosResponse> {

                override fun onResponse(
                    call: Call<KosResponse>,
                    response: Response<KosResponse>
                ) {
                    val body = response.body()
                    val rawError = response.errorBody()?.string()

                    // Debug log yang benar
                    Log.d("FAV_DEBUG", "code=${response.code()}")
                    Log.d("FAV_DEBUG", "body=$body")
                    Log.d("FAV_DEBUG", "rawError=$rawError")

                    if (response.isSuccessful && body?.isSuccess == true) {

                        val mapped = body.data.map { dto ->
                            dto.toKosItemCardForFavorite()
                        }

                        favoriteKos.clear()
                        favoriteKos.addAll(mapped)

                        // ✅ kirim list baru, bukan reference yang sama
                        adapter.updateList(mapped)

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal memuat favorite",
                            Toast.LENGTH_SHORT
                        ).show()
                        favoriteKos.clear()
                        adapter.updateList(emptyList())
                    }

                    updateUI()

                }

                override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateUI() {
        if (favoriteKos.isEmpty()) {
            tvEmptyFavorite.visibility = View.VISIBLE
            rvFavorite.visibility = View.GONE
        } else {
            tvEmptyFavorite.visibility = View.GONE
            rvFavorite.visibility = View.VISIBLE
        }
    }
}
