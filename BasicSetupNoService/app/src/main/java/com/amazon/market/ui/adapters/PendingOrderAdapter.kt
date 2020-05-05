package com.amazon.market.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.amazon.market.R
import com.amazon.market.etc.Helper
import kotlinx.android.synthetic.main.pending_order_item.view.*


class PendingOrderAdapter(
    val context: Context) : RecyclerView.Adapter<PendingOrderAdapter.ViewHolder>()  {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingOrderAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.pending_order_item, parent, false)
        val v = ViewHolder(view)

        return v

    }

    override fun getItemCount(): Int {
        try {
            return 2
        } catch (e: Exception) {
            return 0
        }
    }

    override fun onBindViewHolder(holder: PendingOrderAdapter.ViewHolder, position: Int) {
        if(position == 1) {
            holder.bottom_view.bottom_view.visibility = View.GONE
        }
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val bottom_view = view.bottom_view
    }
    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}