package com.example.myapplication.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.ProductSpec

class ProductListAdapter(
    val context: Activity,
    val isFromCart: Boolean,
    private var productList: List<ProductSpec>,
    val handler: (ProductSpec) -> Unit,
    ) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_product,
            parent, false))
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(productList[position])
    }
    inner class ViewHolder(private var rootView: View): RecyclerView.ViewHolder(rootView) {
        val specNameTxt: TextView = rootView.findViewById(R.id.specName)
        val specValueTxt: TextView = rootView.findViewById(R.id.specValue)
        val addCart: TextView = rootView.findViewById(R.id.add_to_cart)

        fun setData(data: ProductSpec){
            specNameTxt.text = data.lookupName
            specValueTxt.text = data.value
            if (isFromCart) addCart.text = "Remove" else addCart.text = "Add to Cart"
            addCart.setOnClickListener{
                if (!isFromCart) {
                    addCart.text = "Added"
                }
                handler(data)
            }
        }
    }
}

