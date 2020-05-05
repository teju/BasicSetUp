package com.amazon.market.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazon.market.R
import kotlinx.android.synthetic.main.banner.view.*
import kotlinx.android.synthetic.main.banner.view.llRoot
import kotlinx.android.synthetic.main.home_banner.view.*


class HomeBannerAdapter(
    val context: Context,
    val banners: ArrayList<Int>) : RecyclerView.Adapter<HomeBannerAdapter.ViewHolder>()  {
    var isHome = false
    lateinit var bannerAdapterListener : BanneradapterListener

    interface BanneradapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeBannerAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.home_banner, parent, false)
        val v = ViewHolder(view)
        return ViewHolder(view).listen { pos, type ->
            bannerAdapterListener.onClick(pos)
            //TODO do other stuff here
        }
        return v

    }

    override fun getItemCount(): Int {
        try {
            return banners?.size!!.toInt()
        } catch (e: Exception) {
            return 0
        }
    }

    override fun onBindViewHolder(holder: HomeBannerAdapter.ViewHolder, position: Int) {
        holder.default_banner.setImageDrawable(context.resources.getDrawable(R.drawable.default_banner))
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val default_banner = view.default_banner
        val llRoot = view.llRoot

    }
    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}