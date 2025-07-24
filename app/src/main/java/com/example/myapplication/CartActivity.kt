package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.adapter.ProductListAdapter
import com.example.myapplication.databinding.ActivityCartBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.ProductSpec

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val productList = mutableListOf<ProductSpec>()
        productList.addAll(getProductSpecList(this))
        if (productList.isNotEmpty()) {
            binding.specRecyclerView.visibility = View.VISIBLE
            binding.empty.visibility = View.GONE
            binding.specRecyclerView.adapter = ProductListAdapter(
                context = this@CartActivity,
                productList = productList,
                isFromCart = true) {
                removeProductSpecList(context = this@CartActivity, it)
                productList.clear()
                productList.addAll(getProductSpecList(this))
                Toast.makeText(this, "Removed from cart", Toast.LENGTH_SHORT).show()
                binding.specRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
        else {
            binding.specRecyclerView.visibility = View.GONE
            binding.empty.visibility = View.VISIBLE
            log("Product list is empty")
        }
    }
}