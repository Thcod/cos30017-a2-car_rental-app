package com.example.carrental

/** In-memory data only (nothing is saved to disk). */
object CarStore {
    const val START_BALANCE = 500
    const val MAX_PER_RENTAL = 400

    var balance = START_BALANCE
        private set

    val cars = listOf(
        Car(1, "Toyota", "Corolla", 2018, 4.0f, 85000, 45, R.drawable.car_1),
        Car(2, "Honda", "Civic", 2021, 4.5f, 32000, 60, R.drawable.car_2),
        Car(3, "Ford", "Mustang", 2020, 4.8f, 28000, 95, R.drawable.car_3),
        Car(4, "Mazda", "CX-5", 2016, 3.5f, 120000, 40, R.drawable.car_4),
        Car(5, "Tesla", "Model 3", 2023, 4.7f, 9000, 110, R.drawable.car_5)
    )

    val bookings = mutableListOf<Booking>()
    val favourites = mutableSetOf<Int>()

    var query = ""
    var sort = 0 // 0 = default, 1 = rating, 2 = year, 3 = cost

    private fun isRented(car: Car) = bookings.any { it.car.id == car.id }

    /** Search + sort applied to a copy; the base list is never changed. */
    fun available(): List<Car> {
        val result = cars.filter {
            !isRented(it) && (query.isBlank() ||
                    it.name.contains(query, true) || it.model.contains(query, true))
        }
        return when (sort) {
            1 -> result.sortedByDescending { it.rating }
            2 -> result.sortedByDescending { it.year }
            3 -> result.sortedBy { it.dailyCost }
            else -> result
        }
    }

    fun favouriteCars() = cars.filter { it.id in favourites && !isRented(it) }

    fun book(booking: Booking) {
        balance -= booking.total
        bookings.add(booking)
    }

    fun cancel(booking: Booking) {
        balance += booking.total
        bookings.remove(booking)
    }

    /** Restores the starting state so each test begins clean. */
    fun reset() {
        balance = START_BALANCE
        bookings.clear()
        favourites.clear()
        query = ""
        sort = 0
    }
}