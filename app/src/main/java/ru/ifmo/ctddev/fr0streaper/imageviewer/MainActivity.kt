package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ImageLoaderServiceReceiver.Receiver {

    private val descriptionAdapter: DescriptionAdapter = DescriptionAdapter(this)
    var receiver: ImageLoaderServiceReceiver? = ImageLoaderServiceReceiver(Handler())

    override fun onReceiveResult(resultCode: Int, data: Bundle) {
        when (resultCode) {
            200 -> {
                val image = data.getSerializable("image") as Image
                descriptionAdapter.dataset.add(image)
                descriptionAdapter.notifyItemInserted(descriptionAdapter.dataset.size - 1)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        receiver!!.setReceiver(this)

        initDescriptions()
    }

    override fun onPause() {
        super.onPause()
        receiver!!.setReceiver(null)
    }

    private fun initDescriptions() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.isNestedScrollingEnabled = true

        recyclerView.adapter = descriptionAdapter

        ImageUtilities.downloadPreviewList(this, 1, receiver!!)
    }
}
