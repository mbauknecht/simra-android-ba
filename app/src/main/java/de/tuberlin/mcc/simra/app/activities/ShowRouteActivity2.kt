package de.tuberlin.mcc.simra.app.activities

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.View
import de.tuberlin.mcc.simra.app.databinding.ActivityShowRouteBinding
import de.tuberlin.mcc.simra.app.util.BaseActivity
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.util.*

class ShowRouteActivity2 : BaseActivity() {
   private lateinit var binding: ActivityShowRouteBinding// manually added
    companion object {
        private const val TAG = "ShowRouteActivity_LOG"
        private const val EXTRA_RIDE_ID = "EXTRA_RIDE_ID"
        private const val EXTRA_STATE = "EXTRA_STATE"
        private const val EXTRA_SHOW_RIDE_SETTINGS_DIALOG = "EXTRA_SHOW_RIDE_SETTINGS_DIALOG"

        fun startShowRouteActivity(rideId: Int, state: Int?, showRideSettingsDialog: Boolean, context: Context) {
            val intent = Intent(context, ShowRouteActivity2::class.java)
            intent.putExtra(EXTRA_RIDE_ID, rideId)
            intent.putExtra(EXTRA_STATE, state)
            intent.putExtra(EXTRA_SHOW_RIDE_SETTINGS_DIALOG, showRideSettingsDialog)
            context.startActivity(intent)
        }
    }

    // ... (rest of the class)

    private inner class RideUpdateTask(private val updateBoundaries: Boolean, private val calculateEvents: Boolean) :
            AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            //refreshRoute(rideId, updateBoundaries, calculateEvents)
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
           binding.loadingAnimationLayout.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            binding.loadingAnimationLayout.visibility = View.GONE
            if (updateBoundaries) {
                InfoWindow.closeAllInfoWindowsOn(binding.showRouteMap)
            }
        }
    }

    private inner class LoadOriginalDataLogTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
          /*  originalDataLog = DataLog.loadDataLog(rideId, this@ShowRouteActivity2)
            val originalRoute: Polyline = originalDataLog.rideAnalysisData.route
            incidentLog = IncidentLog.loadIncidentLogWithRideSettingsInformation(
                    rideId,
                    bike,
                    pLoc,
                    child == 1,
                    trailer == 1,
                    this@ShowRouteActivity2
            )
            if (editableRoute != null) {
                binding.showRouteMap.overlayManager.remove(editableRoute)
            }
            editableRoute = Polyline()
            editableRoute.points = ArrayList(originalRoute.points)
            editableRoute.width = 40.0f
            editableRoute.paint.color = getColor(R.color.colorPrimaryDark)
            editableRoute.paint.strokeCap = Paint.Cap.ROUND
            binding.showRouteMap.overlayManager.add(editableRoute)
            runOnUiThread {
                binding.routePrivacySlider.values = listOf(0F, originalRoute.points.size.toFloat())
                binding.routePrivacySlider.valueTo = originalRoute.points.size.toFloat()
                binding.routePrivacySlider.valueFrom = 0F
            }*/
            return null
        }
    }
}
