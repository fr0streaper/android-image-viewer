package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.arch.persistence.room.Room
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.android.synthetic.main.activity_main.*
import ru.ifmo.ctddev.fr0streaper.imageviewer.database.Database

class MainActivity : AppCompatActivity(), ImageLoaderServiceReceiver.Receiver {

    var receiver: ImageLoaderServiceReceiver? = ImageLoaderServiceReceiver(Handler())
    private val descriptionAdapter: DescriptionAdapter = DescriptionAdapter(this, receiver = receiver!!)

    override fun onReceiveResult(resultCode: Int, data: Bundle) {
        when (resultCode) {
            200 -> {
                val image = data.getSerializable("image") as Image
                descriptionAdapter.dataset.add(image)
                descriptionAdapter.notifyItemInserted(descriptionAdapter.dataset.size - 1)
            }
        }
    }

    private fun resetDescriptionAdapter() {
        descriptionAdapter.apply {
            imagePage = 1
            searchQuery = search_editText.text.toString()
            dataset.clear()
            notifyDataSetChanged()
        }

        initDescriptions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        ImageUtilities.database = Room.databaseBuilder(this, Database::class.java, "favorites").build()

        search.setOnClickListener {
            resetDescriptionAdapter()
        }

        favorites.setOnClickListener {
            Toast.makeText(this, "ゴゴゴゴゴゴゴゴゴ", Toast.LENGTH_SHORT).show()

            if (descriptionAdapter.isMainScreen) {
                search_editText.apply {
                    isEnabled = false
                    visibility = View.INVISIBLE
                }
                search.apply {
                    isEnabled = false
                    visibility = View.INVISIBLE
                }

                favorites.setImageResource(R.drawable.baseline_grade_black_24)
            } else {
                search_editText.apply {
                    isEnabled = true
                    visibility = View.VISIBLE
                }
                search.apply {
                    isEnabled = true
                    visibility = View.VISIBLE
                }

                favorites.setImageResource(R.drawable.outline_grade_black_24)
            }

            descriptionAdapter.isMainScreen = !descriptionAdapter.isMainScreen
            resetDescriptionAdapter()
        }

        initDescriptions()
    }

    override fun onResume() {
        super.onResume()
        descriptionAdapter.receiver.setReceiver(this)

        initDescriptions()
    }

    override fun onPause() {
        super.onPause()
        descriptionAdapter.receiver.setReceiver(null)
    }

    private fun initDescriptions() {
        val layoutManager = LinearLayoutManager(this)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.isNestedScrollingEnabled = true

        recyclerView.adapter = descriptionAdapter

        if (descriptionAdapter.isMainScreen) {
            ImageUtilities.downloadPreviewList(this, 1, descriptionAdapter.receiver, descriptionAdapter.searchQuery)
        } else {
            ImageUtilities.loadFavoritesPage(this, 1, descriptionAdapter.receiver)
        }
    }
}
