package com.amazon.market.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazon.market.R
import com.amazon.market.model.Products


class ProductsAdapter(
    val context: Context) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>()  {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.adapter, parent, false)
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

    override fun onBindViewHolder(holder: ProductsAdapter.ViewHolder, position: Int) {

    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    }
    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}