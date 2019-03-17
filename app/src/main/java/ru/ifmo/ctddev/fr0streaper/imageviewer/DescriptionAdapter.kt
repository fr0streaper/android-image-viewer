package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item.view.*
import java.util.concurrent.Executors
import java.io.Serializable

class DescriptionAdapter(val context: Context, var dataset: MutableList<Image> = mutableListOf()) :
    RecyclerView.Adapter<DescriptionAdapter.DescriptionHolder>() {

    private var imagePage = 1
    private val executor = Executors.newFixedThreadPool(4)

    inner class DescriptionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description = itemView.description!!
        val author = itemView.authorName!!
        val image = itemView.imageView!!
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1)) {
                    ++imagePage
                    ImageUtilities.downloadPreviewList(context, imagePage, (context as MainActivity).receiver!!)
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescriptionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)

        return DescriptionHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: DescriptionHolder, position: Int) {

        val image = dataset[position]
        holder.author.text = image.user?.name
        holder.description.text = image.description

        executor.execute {
            val preview = BitmapFactory.decodeFile(image.localPreviewPath)
            (this@DescriptionAdapter.context as Activity).runOnUiThread {
                holder.image.setImageBitmap(preview)
            }
        }

        holder.itemView.setOnClickListener {
            val fragment = ImageDetailFragment()

            fragment.arguments = Bundle().apply {
                putSerializable("image", image as Serializable)
            }
            fragment.show((context as FragmentActivity).supportFragmentManager, "ImageDetailFragment")
        }
    }

}