package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Gym Tracker", appName)
  }

  @Test
  fun `verify body weight BMI, body fat, and muscle mass calculations`() {
    val entry = com.example.data.model.BodyWeightEntry(
      weight = 80.0,
      weightUnit = "kg"
    )
    val bmi = entry.calculateBmi(heightCm = 180.0)
    assertEquals(24.7, bmi!!, 0.1)

    val category = entry.getBmiCategory(heightCm = 180.0)
    assertEquals("Normal", category)

    val bodyFat = entry.calculateBodyFatPercentage(heightCm = 180.0, age = 25)
    assertEquals(19.2, bodyFat!!, 0.2)

    val muscleMass = entry.calculateMuscleMass(heightCm = 180.0, age = 25)
    assertEquals(64.6, muscleMass!!, 0.2)
  }
}
