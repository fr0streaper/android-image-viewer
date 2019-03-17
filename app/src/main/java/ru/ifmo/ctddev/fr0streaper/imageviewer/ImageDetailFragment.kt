package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.image_detail.view.*
import java.util.concurrent.Executors

class ImageDetailFragment : DialogFragment(), ImageLoaderServiceReceiver.Receiver {

    lateinit var image: Image
    lateinit var createdView: View
    private val executor = Executors.newFixedThreadPool(4)
    var receiver: ImageLoaderServiceReceiver? = ImageLoaderServiceReceiver(Handler())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null)
            if (arguments!!.containsKey("image"))
                image = arguments!!.getSerializable("image") as Image

        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialog)
    }

    override fun onResume() {
        super.onResume()
        receiver!!.setReceiver(this)

        initImage()
    }

    override fun onPause() {
        super.onPause()
        receiver!!.setReceiver(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.image_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createdView = view
        initImage()
    }

    private fun initImage() {
        ImageUtilities.downloadRegularImage(this.context!!, image, receiver!!)
    }

    override fun onReceiveResult(resultCode: Int, data: Bundle) {
        when (resultCode) {
            200 -> {
                image = data.getSerializable("image") as Image
                executor.execute {
                    val regular = BitmapFactory.decodeFile(image.localRegularPath)
                    (this@ImageDetailFragment.context as Activity).runOnUiThread {
                        createdView.image_regular.setImageBitmap(regular)
                        createdView.toolbar.title = image.user?.name
                        createdView.progress_circular.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }
}