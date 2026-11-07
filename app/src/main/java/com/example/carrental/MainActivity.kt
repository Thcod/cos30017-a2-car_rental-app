package com.example.carrental

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import com.example.carrental.databinding.ActivityMainBinding
import com.example.carrental.databinding.ItemBookingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private var list = listOf<Car>()
    private var index = 0

    private val rentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            val data = r.data
            if (r.resultCode == RESULT_OK && data != null) {
                val car = IntentCompat.getParcelableExtra(data, DetailsActivity.EXTRA_CAR, Car::class.java)!!
                val days = data.getIntExtra(DetailsActivity.EXTRA_DAYS, 1)
                val renter = data.getStringExtra(DetailsActivity.EXTRA_RENTER).orEmpty()
                val booking = Booking(car, days, renter)
                CarStore.book(booking)
                toast("Booked ${car.name} ${car.model} for ${booking.total} credits")
            } else {
                toast("Booking cancelled")
            }
            refresh()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Dark mode (set state before attaching the listener)
        b.darkSwitch.isChecked =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        b.darkSwitch.setOnCheckedChangeListener { _, on ->
            AppCompatDelegate.setDefaultNightMode(
                if (on) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Tabs
        listOf("Available", "Favourites", "My bookings").forEach {
            b.tabLayout.addTab(b.tabLayout.newTab().setText(it))
        }
        b.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = showTab(tab?.position ?: 0)
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Search
        b.searchView.setQuery(CarStore.query, false)
        b.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean {
                CarStore.query = q.orEmpty()
                index = 0
                refresh()
                return true
            }
        })

        // Sort
        val options = listOf("Sort: Default", "Rating (high to low)", "Year (newest to oldest)", "Cost (low to high)")
        b.sortSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        b.sortSpinner.setSelection(CarStore.sort)
        b.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos != CarStore.sort) {
                    CarStore.sort = pos
                    index = 0
                    refresh()
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        b.prevButton.setOnClickListener {
            index = (index - 1 + list.size) % list.size
            showCar()
        }

        b.nextButton.setOnClickListener {
            index = (index + 1) % list.size
            showCar()
        }

        b.favButton.setOnClickListener {
            val id = list[index].id
            if (!CarStore.favourites.remove(id)) CarStore.favourites.add(id)
            showCar()
            showFavourites()
        }

        b.rentButton.setOnClickListener {
            val intent = Intent(this, DetailsActivity::class.java)
                .putExtra(DetailsActivity.EXTRA_CAR, list[index])
            rentLauncher.launch(intent)
        }

        refresh()
    }

    private fun showTab(position: Int) {
        b.availableSection.isVisible = position == 0
        b.favouritesSection.isVisible = position == 1
        b.bookingsSection.isVisible = position == 2
    }

    private fun refresh() {
        list = CarStore.available()
        if (index >= list.size) index = 0
        b.balanceText.text = "Credits: ${CarStore.balance}"
        showCar()
        showFavourites()
        showBookings()
    }

    private fun showCar() {
        val car = list.getOrNull(index)
        b.carCard.isVisible = car != null
        b.emptyText.isVisible = car == null
        if (car == null) return
        val isFav = car.id in CarStore.favourites
        b.carImage.setImageResource(car.imageRes)
        b.carName.text = "${car.name} ${car.model}"
        b.carDetails.text = "${car.year}  •  ${"%,d".format(car.km)} km"
        b.ratingBar.rating = car.rating
        b.ratingText.text = "${car.rating}"
        b.priceText.text = "${car.dailyCost} credits/day"
        b.counterText.text = "${index + 1} of ${list.size}"
        b.favButton.text = if (isFav) "♥" else "♡"
        b.favButton.setTextColor(if (isFav) 0xFFFF5252.toInt() else 0xFFFFFFFF.toInt())
    }

    private fun showFavourites() {
        val favs = CarStore.favouriteCars()
        b.emptyFavText.isVisible = favs.isEmpty()
        b.favList.removeAllViews()
        favs.forEach { car ->
            // Reuses the booking row layout (text + one button)
            val row = ItemBookingBinding.inflate(layoutInflater, b.favList, false)
            row.bookingText.text = "${car.name} ${car.model}\n${car.dailyCost} credits/day"
            row.cancelBooking.text = "View"
            row.cancelBooking.setOnClickListener {
                // Clear the search so the favourite is always in the list
                CarStore.query = ""
                b.searchView.setQuery("", false)
                list = CarStore.available()
                index = list.indexOf(car)
                showCar()
                b.tabLayout.getTabAt(0)?.select()
            }
            b.favList.addView(row.root)
        }
    }

    private fun showBookings() {
        b.emptyBookingsText.isVisible = CarStore.bookings.isEmpty()
        b.bookingsList.removeAllViews()
        CarStore.bookings.toList().forEach { booking ->
            val row = ItemBookingBinding.inflate(layoutInflater, b.bookingsList, false)
            row.bookingText.text =
                "${booking.car.name} ${booking.car.model} - ${booking.renter}\n${booking.days} days, ${booking.total} credits"
            row.cancelBooking.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Cancel booking?")
                    .setMessage("${booking.total} credits will be refunded and the car will become available again.")
                    .setNegativeButton("Keep booking", null)
                    .setPositiveButton("Cancel booking") { _, _ ->
                        CarStore.cancel(booking)
                        toast("Booking cancelled, ${booking.total} credits refunded")
                        refresh()
                    }
                    .show()
            }
            b.bookingsList.addView(row.root)
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}