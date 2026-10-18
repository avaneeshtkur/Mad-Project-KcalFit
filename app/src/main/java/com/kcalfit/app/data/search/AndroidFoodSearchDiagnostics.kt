package com.kcalfit.app.data.search

import android.util.Log
import com.kcalfit.app.BuildConfig

object AndroidFoodSearchDiagnostics : FoodSearchDiagnostics {
    override fun record(event: String, durationMs: Long, count: Int, status: Int, retry: Int) {
        if (BuildConfig.DEBUG) Log.d("FoodSearch", "event=$event durationMs=$durationMs count=$count status=$status retry=$retry")
    }
}
