package com.example.carrental

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.example.carrental.databinding.ActivityDetailsBinding
import com.google.android.material.snackbar.Snackbar

class DetailsActivity : AppCompatActivity() {
    private lateinit var b: ActivityDetailsBinding
    private lateinit var car: Car
    private val days get() = b.daysSlider.value.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(b.root)

        car = IntentCompat.getParcelableExtra(intent, EXTRA_CAR, Car::class.java)!!
        b.carImage.setImageResource(car.imageRes)
        b.carName.text = "${car.name} ${car.model}"
        b.carDetails.text = "Year: ${car.year}\nKilometres: ${car.km}\nRate: ${car.dailyCost} credits/day"
        b.ratingBar.rating = car.rating

        b.daysSlider.addOnChangeListener { _, _, _ -> updateTotal() }
        updateTotal()

        b.saveButton.setOnClickListener { save() }
        // Cancel and the system Back button both finish with RESULT_CANCELED
        b.cancelButton.setOnClickListener { finish() }
    }

    private fun updateTotal() {
        b.daysLabel.text = if (days == 1) "1 day" else "$days days"
        b.totalText.text = "${car.dailyCost * days} credits total"
        b.balanceInfo.text = "Your balance: ${CarStore.balance} credits"
    }

    private fun save() {
        val name = b.renterName.text.toString().trim()
        val total = car.dailyCost * days
        val error = when {
            name.isEmpty() -> "Please enter your name"
            !name.matches(Regex("[A-Za-z ]+")) -> "Name can only contain letters"
            total > CarStore.MAX_PER_RENTAL -> "A rental cannot exceed ${CarStore.MAX_PER_RENTAL} credits"
            total > CarStore.balance -> "Not enough credits (balance: ${CarStore.balance})"
            else -> null
        }
        if (error != null) {
            Log.w(TAG, "Validation failed: $error")
            if (name.isEmpty() || !name.matches(Regex("[A-Za-z ]+"))) b.renterName.error = error
            Snackbar.make(b.root, error, Snackbar.LENGTH_LONG).show()
            return
        }
        val result = Intent()
            .putExtra(EXTRA_CAR, car)
            .putExtra(EXTRA_DAYS, days)
            .putExtra(EXTRA_RENTER, name)
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        private const val TAG = "CarRental"
        const val EXTRA_CAR = "car"
        const val EXTRA_DAYS = "days"
        const val EXTRA_RENTER = "renter"
    }
}