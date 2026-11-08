package com.example.carrental

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CarStoreTest {
    private val mustang get() = CarStore.cars[2]

    @Before
    fun setUp() {
        CarStore.reset()
    }

    private fun models() = CarStore.available().map { it.model }

    @Test
    fun startsWith500CreditsAndFiveCars() {
        assertEquals(500, CarStore.balance)
        assertEquals(5, CarStore.available().size)
    }

    @Test
    fun bookingTotalIsDaysTimesDailyCost() {
        assertEquals(285, Booking(mustang, 3, "Sam").total)
    }

    @Test
    fun bookingDeductsCreditsAndHidesCar() {
        CarStore.book(Booking(mustang, 3, "Sam"))
        assertEquals(215, CarStore.balance)
        assertFalse(mustang in CarStore.available())
    }

    @Test
    fun cancellingRefundsCreditsAndRestoresCar() {
        val booking = Booking(mustang, 3, "Sam")
        CarStore.book(booking)
        CarStore.cancel(booking)
        assertEquals(500, CarStore.balance)
        assertTrue(mustang in CarStore.available())
    }

    @Test
    fun searchMatchesNameOrModelIgnoringCase() {
        CarStore.query = "FORD"
        assertEquals(listOf("Mustang"), models())
        CarStore.query = "civ"
        assertEquals(listOf("Civic"), models())
    }

    @Test
    fun searchWithNoMatchReturnsEmptyList() {
        CarStore.query = "zzz"
        assertTrue(CarStore.available().isEmpty())
    }

    @Test
    fun sortsByRatingYearAndCost() {
        CarStore.sort = 1
        assertEquals(listOf("Mustang", "Model 3", "Civic", "Corolla", "CX-5"), models())
        CarStore.sort = 2
        assertEquals(listOf("Model 3", "Civic", "Mustang", "Corolla", "CX-5"), models())
        CarStore.sort = 3
        assertEquals(listOf("CX-5", "Corolla", "Civic", "Mustang", "Model 3"), models())
    }

    @Test
    fun sortingDoesNotChangeBaseList() {
        val before = CarStore.cars.toList()
        CarStore.sort = 3
        CarStore.available()
        assertEquals(before, CarStore.cars)
    }

    @Test
    fun rentedCarsAreNotListedAsFavourites() {
        CarStore.favourites.add(mustang.id)
        assertEquals(1, CarStore.favouriteCars().size)
        CarStore.book(Booking(mustang, 1, "Sam"))
        assertTrue(CarStore.favouriteCars().isEmpty())
    }
}