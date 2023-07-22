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
import androidx.preference.PreferenceManager
import com.braintreepayments.api.*
import com.braintreepayments.api.PayPalCheckoutRequest.USER_ACTION_COMMIT
import com.codersguidebook.store.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject


class MainActivity : AppCompatActivity(), PayPalListener {

    companion object {
        // TODO: Replace the value of the below variable with your Sandbox/Production Braintree tokenization key
        private const val TOKENIZATION_KEY = "YOUR-TOKENIZATION-KEY"
    }

    private val storeViewModel: StoreViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    // TODO: put the ISO code for your store's base currency as the value of the defCurrency variable
    private val defCurrency = "GBP"
    private var deviceData = ""
    private var exchangeData: JSONObject? = null
    private lateinit var braintreeClient: BraintreeClient
    private lateinit var paypalClient: PayPalClient
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

        braintreeClient = BraintreeClient(this, TOKENIZATION_KEY)
        paypalClient = PayPalClient(this, braintreeClient)

        getClientToken()
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

    private fun getClientToken() {
        // TODO: Replace YOUR-DOMAIN.com with your website domain
        AsyncHttpClient().get("https://YOUR-DOMAIN.com/store/client_token.php", object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseString: String?) {
                braintreeClient = BraintreeClient(this@MainActivity, responseString ?: TOKENIZATION_KEY)
                paypalClient = PayPalClient(this@MainActivity, braintreeClient)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                braintreeClient = BraintreeClient(this@MainActivity, TOKENIZATION_KEY)
                paypalClient = PayPalClient(this@MainActivity, braintreeClient)
            }
        })
    }

    fun initiatePayment() {
        if (storeViewModel.orderTotal.value == 0.00) return

        val orderTotal = storeViewModel.orderTotal.value.toString()
        saveOrderTotal(orderTotal)

        val request = PayPalCheckoutRequest(orderTotal)
        request.currencyCode = storeViewModel.currency.value?.code ?: defCurrency
        request.userAction = USER_ACTION_COMMIT

        paypalClient.tokenizePayPalAccount(this, request)
    }

    private fun saveOrderTotal(total: String?) = sharedPreferences.edit().apply {
        putString("orderTotal", total)
        apply()
    }

    override fun onPayPalFailure(error: Exception) {
        Toast.makeText(this, getString(R.string.paypal_error, error.message), Toast.LENGTH_LONG).show()
    }

    override fun onPayPalSuccess(payPalAccountNonce: PayPalAccountNonce) {
        collectDeviceData()

        val params = RequestParams().apply {
            put("amount", sharedPreferences.getString("orderTotal", null) ?: return)
            put("currency_iso_code", storeViewModel.currency.value?.code ?: defCurrency)
            put("payment_method_nonce", payPalAccountNonce)
            put("client_device_data", deviceData)
        }

        saveOrderTotal(null)

        // TODO: Replace YOUR-DOMAIN.com with your website domain
        AsyncHttpClient().post("https://YOUR-DOMAIN.com/store/process_transaction.php", params,
            object : TextHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, outcome: String?) {
                    if (outcome == "SUCCESSFUL") {
                        Toast.makeText(this@MainActivity, resources.getString(R.string.payment_successful), Toast.LENGTH_LONG).show()
                        clearCart()
                    } else Toast.makeText(this@MainActivity, resources.getString(R.string.payment_error), Toast.LENGTH_LONG).show()
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, outcome: String?, throwable: Throwable?) { }
            }
        )
    }

    private fun collectDeviceData() {
        DataCollector(braintreeClient).collectDeviceData(this) { data, _ ->
            deviceData = data ?: ""
        }
    }
}