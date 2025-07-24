package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.Constants.METHOD_NAME
import com.example.myapplication.Constants.NAMESPACE
import com.example.myapplication.Constants.SOAP_ACTION
import com.example.myapplication.Constants.URL
import com.example.myapplication.adapter.BannerSliderAdapter
import com.example.myapplication.adapter.ProductListAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.ProductSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val sliderHandler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val images = listOf(
        R.drawable.banner_img_1,
        R.drawable.banner_img_2,
        R.drawable.banner_img_3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBarsInsets.top, 0, systemBarsInsets.bottom) // Adjust for both top and bottom
            WindowInsetsCompat.CONSUMED
        }

        val adapter = BannerSliderAdapter(images)
        binding.viewPager.adapter = adapter
        setupIndicators(images.size)
        setCurrentIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setCurrentIndicator(position)
                sliderHandler.removeCallbacks(runnable)
                sliderHandler.postDelayed(runnable, 3000)
            }
        })

        runnable = Runnable {
            binding.viewPager.currentItem = (binding.viewPager.currentItem + 1) % images.size
        }

        // Launch network call in background
        CoroutineScope(Dispatchers.IO).launch {
            fetchProductSpecList { resultList ->
                runOnUiThread {
                    binding.specRecyclerView.visibility = View.VISIBLE
                    binding.loadingView.visibility = View.GONE
                    binding.specRecyclerView.adapter = ProductListAdapter(
                        context = this@MainActivity,
                        productList = resultList,
                        isFromCart = false) {
                        saveProductSpecList(this@MainActivity, it)
                        Toast.makeText(this@MainActivity, "Item added in cart", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.layoutMenuCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        binding.layoutMenuOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
        binding.layoutMenuOffers.setOnClickListener {
            startActivity(Intent(this, OffersActivity::class.java))
        }
        binding.layoutMenuAccount.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }
    private fun setupIndicators(count: Int) {
        val indicators = Array(count) { ImageView(this) }

        val sizeInDp = 8
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            sizeInDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val params = LinearLayout.LayoutParams(sizeInPx, sizeInPx)
        params.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i].setImageResource(R.drawable.indicator_inactive)
            indicators[i].layoutParams = params
            binding.indicatorLayout.addView(indicators[i])
        }
    }


    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = binding.indicatorLayout.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageResource(R.drawable.indicator_active)
            } else {
                imageView.setImageResource(R.drawable.indicator_inactive)
            }
        }
    }

    private fun fetchProductSpecList(onResult: (List<ProductSpec>) -> Unit) {
        val request = SoapObject(NAMESPACE, METHOD_NAME)
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = true
            setOutputSoapObject(request)
        }

        val httpTransport = HttpTransportSE(URL).apply { debug = true }

        try {
            httpTransport.call(SOAP_ACTION, envelope)

            val response = envelope.bodyIn as? SoapObject
            val result = response?.getProperty("GetSpecList_ListResult") as? SoapObject
            val diffgram = result?.getProperty("diffgram") as? SoapObject
            val documentElement = diffgram?.getProperty("DocumentElement") as? SoapObject

            val productList = mutableListOf<ProductSpec>()

            if (documentElement != null) {
                for (i in 0 until documentElement.propertyCount) {
                    val table = documentElement.getProperty(i) as? SoapObject
                    if (table != null) {
                        val id = table.getPropertySafelyAsString("Lookup_Id")?.toIntOrNull() ?: -1
                        val name = table.getPropertySafelyAsString("Lookup_Name") ?: ""
                        val value = table.getPropertySafelyAsString("Value") ?: ""
                        productList.add(ProductSpec(id, name, value))
                    }
                }
            }

            onResult(productList)

        } catch (e: Exception) {
            Log.e("SOAP_ERROR", "Error: ${e.message}", e)
            onResult(emptyList())
        }
    }
}