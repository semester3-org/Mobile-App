package com.apk.koshub.fragments

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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
            openDetail(kos)
        }

        // ===== RecyclerView: Grid =====
        val widthDp = resources.configuration.screenWidthDp
        val spanCount = when {
            widthDp >= 900 -> 4
            widthDp >= 600 -> 3
            else -> 2
        }

        rvFavorite.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = this@FavoriteFragment.adapter
            setHasFixedSize(true)

            // spacing (hapus kalau lu udah pakai margin di item)
            val spacingPx = dpToPx(12)
            // biar ga dobel itemDecoration kalau fragment ke-recreate
            if (itemDecorationCount == 0) {
                addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx, true))
            }
        }

        // load awal
        loadFavoriteFromServer()

        // dengerin perubahan favorite dari Detail
        parentFragmentManager.setFragmentResultListener("fav_changed", viewLifecycleOwner) { _, bundle ->
            val id = bundle.getInt("kos_id", -1)
            val fav = bundle.getBoolean("is_favorite", false)
            if (id == -1) return@setFragmentResultListener

            if (!fav) {
                // kalau di-unfavorite dari detail, buang dari list favorit
                val idx = favoriteKos.indexOfFirst { it.id == id }
                if (idx >= 0) {
                    favoriteKos.removeAt(idx)
                    adapter.updateList(favoriteKos)
                    updateUI()
                }
            } else {
                // kalau baru di-favorite, reload biar data lengkap masuk
                loadFavoriteFromServer()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // refresh tiap balik ke fragment ini
        loadFavoriteFromServer()
    }

    private fun openDetail(kos: KosItemCard) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetailKosFragment.newInstance(kosId = kos.id))
            .addToBackStack(null)
            .commit()
    }

    private fun loadFavoriteFromServer() {
        val user = db.getUser()

        if (user == null) {
            favoriteKos.clear()
            adapter.updateList(favoriteKos)
            updateUI()
            return
        }

        ApiClient.api.getFavoriteKos(userId = user.id)
            .enqueue(object : Callback<KosResponse> {

                override fun onResponse(call: Call<KosResponse>, response: Response<KosResponse>) {
                    val body = response.body()
                    val rawError = response.errorBody()?.string()

                    Log.d("FAV_DEBUG", "code=${response.code()}")
                    Log.d("FAV_DEBUG", "body=$body")
                    Log.d("FAV_DEBUG", "rawError=$rawError")

                    if (response.isSuccessful && body?.isSuccess == true) {

                        val mapped = body.data.map { dto ->
                            dto.toKosItemCardForFavorite()
                        }

                        favoriteKos.clear()
                        favoriteKos.addAll(mapped)
                        adapter.updateList(mapped)

                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat favorite", Toast.LENGTH_SHORT).show()
                        favoriteKos.clear()
                        adapter.updateList(emptyList())
                    }

                    updateUI()
                }

                override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Koneksi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
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

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    // ===== ItemDecoration buat spacing grid =====
    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacingPx: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return

            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacingPx - column * spacingPx / spanCount
                outRect.right = (column + 1) * spacingPx / spanCount
                if (position < spanCount) outRect.top = spacingPx
                outRect.bottom = spacingPx
            } else {
                outRect.left = column * spacingPx / spanCount
                outRect.right = spacingPx - (column + 1) * spacingPx / spanCount
                if (position >= spanCount) outRect.top = spacingPx
            }
        }
    }
}
