package com.example.listycity

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CityRepository {
    private val db = Firebase.firestore
    private val citiesRef = db.collection("cities")
    private val _cities = mutableStateListOf<City>()

    init {
        citiesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            val updatedCities = snapshot?.documents
                ?.mapNotNull { it.toObject(City::class.java) }
                ?.sortedBy { it.name.lowercase() }
                .orEmpty()

            _cities.clear()
            _cities.addAll(updatedCities)
        }
    }

    val cities: List<City>
        get() = _cities

    fun addCity(city: City) {
        citiesRef.document(city.name).set(city)
    }

    fun updateCity(oldCity: City, updatedCity: City) {
        if (oldCity.name == updatedCity.name) {
            citiesRef.document(oldCity.name).set(updatedCity)
            return
        }

        db.runBatch { batch ->
            batch.set(citiesRef.document(updatedCity.name), updatedCity)
            batch.delete(citiesRef.document(oldCity.name))
        }
    }

    fun deleteCity(city: City) {
        citiesRef.document(city.name).delete()
    }
}
