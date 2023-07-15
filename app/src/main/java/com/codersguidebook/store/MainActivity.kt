package com.codersguidebook.store

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.codersguidebook.store.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import androidx.preference.PreferenceManager
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header

class MainActivity : AppCompatActivity() {

    private val storeViewModel: StoreViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    // TODO: put the ISO code for your store's base currency as the value of the defCurrency variable
    private val defCurrency = "GBP"
    private var exchangeData: JSONObject? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        /* FIXME: Here we manually define a list of products
            In reality, you may want to retrieve product information in real-time from your website. */
        val broccoli = Product(R.drawable.broccoli, "Broccoli", 1.40)
        val carrots = Product(R.drawable.carrot, "Carrots", 0.35)
        val strawberries = Product(R.drawable.strawberry, "Strawberries", 2.00)
        val items = listOf(broccoli, carrots, strawberries)
        storeViewModel.products.value = items

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        getCurrencyData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.currencies_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (exchangeData == null) {
            Toast.makeText(this, resources.getString(R.string.exchange_data_unavailable), Toast.LENGTH_SHORT).show()
            getCurrencyData()
        } else {
            when (item.itemId) {
                // TODO: Configure each currency exchange menu item here
                R.id.currency_gbp -> setCurrency("GBP")
                R.id.currency_usd -> setCurrency("USD")
                R.id.currency_eur -> setCurrency("EUR")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getCurrencyData(): JSONObject? {
        val client = AsyncHttpClient()

        // TODO: Replace YOUR-API-KEY-HERE with your exchange rate API key
        client.get("https://v6.exchangerate-api.com/v6/YOUR-API-KEY-HERE/latest/$defCurrency", object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseString: String?) {
                if (responseString != null) {
                    exchangeData = JSONObject(responseString)
                    val currencyPreference = sharedPreferences.getString("currency", defCurrency) ?: defCurrency
                    setCurrency(currencyPreference)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Toast.makeText(this@MainActivity, resources.getString(R.string.exchange_data_unavailable), Toast.LENGTH_SHORT).show()
                setCurrency(defCurrency)
            }
        })

        return null
    }

    private fun setCurrency(isoCode: String) {
        val exchangeRate = exchangeData?.getJSONObject("conversion_rates")?.getDouble(isoCode)

        // TODO: Define the base currency here
        var currency = Currency(defCurrency, "£", null)
        if (exchangeRate != null) {
            when (isoCode) {
                // TODO: Define each additional currency your store supports here
                "USD" -> currency = Currency(isoCode, "$", exchangeRate)
                "EUR" -> currency = Currency(isoCode, "€", exchangeRate)
            }
        }

        sharedPreferences.edit().apply {
            putString("currency", isoCode)
            apply()
        }

        storeViewModel.currency.value = currency
        storeViewModel.calculateOrderTotal()
    }
}