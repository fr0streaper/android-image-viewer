package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

class DescriptionAdapter(
    val context: Context,
    var dataset: MutableList<Image> = mutableListOf(),
    val receiver: ImageLoaderServiceReceiver
) :
    RecyclerView.Adapter<DescriptionAdapter.DescriptionHolder>() {

    var imagePage = 1
    var searchQuery = ""
    var isMainScreen = true

    inner class DescriptionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description = itemView.description!!
        val descriptionLayout = itemView.description_layout!!
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
                    if (isMainScreen) {
                        ImageUtilities.downloadPreviewList(context, imagePage, receiver, searchQuery)
                    } else {
                        ImageUtilities.loadFavoritesPage(context, imagePage, receiver)
                    }
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

        GlobalScope.launch {
            val color: Int = if (ImageUtilities.isFavorite(image)) {
                R.color.colorFavorite
            } else {
                R.color.colorWhite
            }

            GlobalScope.launch(Dispatchers.Main) {
                holder.descriptionLayout.setBackgroundResource(color)
            }
        }

        Glide.with(context)
            .asBitmap()
            .load(image.localPreviewPath)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            val fragment = ImageDetailFragment()

            fragment.arguments = Bundle().apply {
                putSerializable("image", image as Serializable)
            }
            fragment.show((context as FragmentActivity).supportFragmentManager, "ImageDetailFragment")
        }

        holder.itemView.setOnLongClickListener {
            GlobalScope.launch {
                val color: Int = if (ImageUtilities.isFavorite(image)) {
                    ImageUtilities.deleteFavorite(image)
                    R.color.colorWhite
                } else {
                    ImageUtilities.insertFavorite(image)
                    R.color.colorFavorite
                }

                GlobalScope.launch(Dispatchers.Main) {
                    holder.descriptionLayout.setBackgroundResource(color)
                }
            }
            true
        }

    }

}