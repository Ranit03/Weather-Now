package com.example.weathernow.Activities

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.weathernow.Models.WeatherModel
import com.example.weathernow.R
import com.example.weathernow.Utilities.ApiUtilities
import com.example.weathernow.databinding.ActivityLoginBinding
import com.example.weathernow.databinding.ActivityLogoutBinding
import com.example.weathernow.databinding.ActivityMainBinding
import com.example.weathernow.logout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 101

    private val apiKey = "c7fbba90f3b5f3e516a41e52ba6ef64f"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        binding.citySearch.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                getCityWeather(binding.citySearch.text.toString())

                val view = this.currentFocus
                if (view != null) {
                    val imm: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                    imm.hideSoftInputFromWindow(view.windowToken, 0)

                    binding.citySearch.clearFocus()
                }
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }
        binding.currentLocation.setOnClickListener {
            getCurrentLocation()
        }
        var logOut = findViewById<Button>(R.id.Logout)
        logOut.setOnClickListener {
            val intent = Intent(this, logout::class.java)
            startActivity(intent)
        }



    }

    private fun getCityWeather(city: String) {
        binding.progressBar.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getCityWeatherData(city, apiKey)?.enqueue(
            object : Callback<WeatherModel> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    if (response.isSuccessful) {
                        binding.progressBar.visibility = View.GONE

                        response.body()?.let {
                            setDeta(it)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "No City Found", Toast.LENGTH_SHORT)
                            .show()
                        binding.progressBar.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {

                }

            }
        )
    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {

        ApiUtilities.getApiInterface()?.getCurrentWeatherData(latitude, longitude, apiKey)
            ?.enqueue(object : Callback<WeatherModel> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    if (response.isSuccessful) {
                        binding.progressBar.visibility = View.GONE

                        response.body()?.let {
                            setDeta(it)
                        }
                    }


                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {

                }

            })
    }

    private fun getCurrentLocation() {
        if (checkPermissions()) {

            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProvider.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = location

                            binding.progressBar.visibility = View.VISIBLE

                            fetchCurrentLocationWeather(
                                location.latitude.toString(),
                                location.longitude.toString()
                            )


                        }

                    }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()


        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            LOCATION_REQUEST_CODE
        )

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true

        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {

            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDeta(body: WeatherModel) {
        binding.apply {
            val currentDate = SimpleDateFormat("dd/MM/yyyy hh:mm").format(Date())
            dateTime.text = currentDate.toString()
            maxtemp.text = "Max " + k2c(body?.main?.temp_max!!) + "°"

            mintemp.text = "Min " + k2c(body?.main?.temp_min!!) + "°"
            temp.text = "" + k2c(body?.main?.temp!!) + "°"
            weatherTitle.text = body.weather[0].main
            sunriseValue.text = ts2td(body.sys.sunrise.toLong())

            sunsetValue.text = ts2td(body.sys.sunset.toLong())

            pressureValue.text = body.main.pressure.toString()

            humidityValue.text = body.main.humidity.toString() + "%"

            tempFValue.text = "" + (k2c(body.main.temp).times(1.8)).plus(32)
                .roundToInt() + "°"

            citySearch.setText(body.name)
            feelslike.text = "" + k2c(body?.main?.feels_like!!) + "°"
            windValue.text = body.wind.speed.toString() + "m/s"

            groundValue.text = body.main.grnd_level.toString()

            seaValue.text = body.main.sea_level.toString()

            countryValue.text = body.sys.country


        }

        updateUI(body.weather[0].id)

    }

    private fun updateUI(id: Int) {

        binding.apply {
            when (id) {
                in 200..232 -> {

                    weatherImg.setImageResource(R.drawable.ic_storm_weather)

                    mainLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.thunderstrom_bg)

                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.thunderstrom_bg)

                }
                in 300..321 -> {

                    weatherImg.setImageResource(R.drawable.ic_few_clouds)

                    mainLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.drizzle_bg)

                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.drizzle_bg)


                }
                in 500..531 -> {

                    weatherImg.setImageResource(R.drawable.ic_rainy_weather)

                    mainLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.rain_bg)

                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.rain_bg)

                }
                in 600..622 -> {

                    weatherImg.setImageResource(R.drawable.ic_snow_weather)

                    mainLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.snow_bg)

                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.snow_bg)

                }
                in 701..781 -> {

                    weatherImg.setImageResource(R.drawable.ic_broken_clouds)

                    mainLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.atmosphere_bg)


                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.atmosphere_bg)

                }
                800 -> {

                    weatherImg.setImageResource(R.drawable.ic_clear_day)

                    mainLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clear_bg)

                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clear_bg)

                }
                in 801..804 -> {

                    weatherImg.setImageResource(R.drawable.ic_cloudy_weather)

                    mainLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clouds_bg)

                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clouds_bg)

                }
                else -> {

                    weatherImg.setImageResource(R.drawable.ic_unknown)

                    mainLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.unknown_bg)

                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.unknown_bg)


                }

            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ts2td(ts: Long): String {

        val localTime = ts.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()

    }

    private fun k2c(t: Double): Double {
        var intTemp = t
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }




}


