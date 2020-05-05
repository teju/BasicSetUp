package com.amazon.market.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazon.market.R
import com.amazon.market.model.Products
import kotlinx.android.synthetic.main.products_adapter.view.*


class ProductsAdapter(
    val context: Context,
    val productsarr: ArrayList<Products>) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>()  {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.products_adapter, parent, false)
        val v = ViewHolder(view)
        return v
    }

    override fun getItemCount(): Int {
        try {
            return productsarr.size
        } catch (e: Exception) {
            return 0
        }
    }

    override fun onBindViewHolder(holder: ProductsAdapter.ViewHolder, position: Int) {
        holder.product_img.setImageResource(productsarr.get(position).image)
        holder.product_name.setText(productsarr.get(position).name)
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val product_img = view.product_img
        val product_name = view.product_name
    }
    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}