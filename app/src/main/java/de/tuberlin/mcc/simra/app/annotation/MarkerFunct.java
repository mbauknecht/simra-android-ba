package de.tuberlin.mcc.simra.app.annotation;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.activities.ShowRouteActivity;
import de.tuberlin.mcc.simra.app.entities.DataLog;
import de.tuberlin.mcc.simra.app.entities.DataLogEntry;
import de.tuberlin.mcc.simra.app.entities.IncidentLog;
import de.tuberlin.mcc.simra.app.entities.IncidentLogEntry;
import de.tuberlin.mcc.simra.app.entities.IncidentLogEntry.INCIDENT_TYPE;
// import de.tuberlin.mcc.simra.app.services.OBSService;
import java9.util.function.Function;
import java9.util.function.Predicate;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;

/**
 * Convenience functions for working with the Map and setting or deleting markers
 */
public class MarkerFunct {

    private static final String TAG = "MarkerFunct_LOG";
    private final String userAgent = "SimRa/alpha";
    private ShowRouteActivity activity;
    private GeocoderNominatim geocoderNominatim;
    private int state;
    private Map<Integer, Marker> markerMap = new HashMap<>();
    private IncidentLog incidentLog;
    private DataLog gpsDataLog;
    private Drawable markerAutoGenerated_edit;
    private Drawable markerCustom_edit;
    private Drawable markerAutoGenerated_ready;
    private Drawable markerCustom_ready;
    private int bikeType;
    private int phoneLocation;
    private boolean child;
    private boolean trailer;

    public MarkerFunct(ShowRouteActivity activity, DataLog gpsDataLog, IncidentLog incidentLog, int bikeType, int phoneLocation, boolean child, boolean trailer) {
        this.activity = activity;
        this.gpsDataLog = gpsDataLog;
        this.incidentLog = incidentLog;
        this.bikeType = bikeType;
        this.phoneLocation = phoneLocation;
        this.child = child;
        this.trailer = trailer;

        // Preload the Icons for event markers
        // (1) Automatically recognized, not yet annotated
        markerAutoGenerated_edit = activity.getResources().getDrawable(R.drawable.edit_event_blue, null);

        // (2) Custom, not yet annotated
        markerCustom_edit = activity.getResources().getDrawable(R.drawable.edit_event_green, null);

        // (3) Automatically recognized, annotated
        markerAutoGenerated_ready = activity.getResources().getDrawable(R.drawable.edited_event_blue, null);

        // (4) Custom, not yet annotated
        markerCustom_ready = activity.getResources().getDrawable(R.drawable.edited_event_green, null);

        this.state = activity.state;
    }


    public void updateIncidentMarkers(IncidentLog incidentLog) {
        Collection<IncidentLogEntry> incidents = incidentLog.getIncidents().values();
        List<IncidentLogEntry> regularIncidents = simpleFilter(incidents, x -> INCIDENT_TYPE.isRegular(x.incidentType));
        for (IncidentLogEntry incident : regularIncidents) {
            setMarker(incident);
        }
    }

    /*public void updateOBSMarkers(IncidentLog incidentLog, Context context) {
        Collection<IncidentLogEntry> incidents = incidentLog.getIncidents().values();

        List<IncidentLogEntry> obsIncidents = simpleFilter(incidents, x -> INCIDENT_TYPE.isOBS(x.incidentType));
        List<IncidentLogEntry> obsAvg2sIncidents = simpleFilter(obsIncidents, x -> x.incidentType == INCIDENT_TYPE.OBS_AVG2S);
        List<IncidentLogEntry> obsMinKalmanIncidents = simpleFilter(obsIncidents, x -> {
            if (x.incidentType != INCIDENT_TYPE.OBS_MIN_KALMAN) return false;

            try {
                OBSService.ClosePassEvent event = new OBSService.ClosePassEvent(x.description.split("\n", -1)[2].replace("[", "").replace("]", ""));
                Log.d(TAG,"x.description: " + x.description);
                return Integer.parseInt(event.leftSensor.get(0)) < 50;
            } catch (Exception e) {
                return false;
            }
        });

        // Removing Avg2s markers if MinKalman markers are already placed at the same position
        obsAvg2sIncidents = simpleFilter(obsAvg2sIncidents, x -> {
            Double latitude = x.latitude, longitude = x.longitude;

            return simpleFilter(obsMinKalmanIncidents, y -> latitude.equals(y.latitude) && longitude.equals(y.longitude)).size() == 0;
        });

        setDistanceMarkers(obsAvg2sIncidents, context.getColor(R.color.distanceMarkerWarning));
        setDistanceMarkers(obsMinKalmanIncidents, context.getColor(R.color.distanceMarkerDanger));
    }*/

    private <T> List<T> simpleFilter(Collection<T> c, Predicate<? super T> filter) {
        return StreamSupport.stream(c).filter(filter).collect(Collectors.toList());
    }

    private <S, T> List<T> simpleMap(Collection<S> c, Function<? super S, ? extends T> mapper) {
        return StreamSupport.stream(c).map(mapper).collect(Collectors.toList());
    }

    /**
     * Because custom markers should only be placed on the actual route, after the
     * user taps onto the map we're determining the GeoPoint on the route that
     * is clostest to the location the user has actually tapped.
     * => this is done via the GeoPointWrapper class.
     *
     * @param geoPoint
     * @param gpsDataLog
     * @return
     */
    public DataLogEntry getClosesDataLogEntryToGeoPoint(GeoPoint geoPoint, DataLog gpsDataLog) {
        List<GeoPointWrapper> wrappedGPS = new ArrayList<>();
        List<GeoPoint> gpsDataLogGeoPoints = gpsDataLog.rideAnalysisData.route.getPoints();
        for (int i = 0; i < gpsDataLogGeoPoints.size(); i++) {
            wrappedGPS.add(new GeoPointWrapper(gpsDataLogGeoPoints.get(i), geoPoint, gpsDataLog.onlyGPSDataLogEntries.get(i)));
        }
        Collections.sort(wrappedGPS, (GeoPointWrapper o1, GeoPointWrapper o2) -> {
            if (o1.distToReference < o2.distToReference) return -1;
            if (o1.distToReference > o2.distToReference) return 1;
            else return 0;
        });
        return wrappedGPS.get(0).dataLogEntry;
    }


    public void addCustomMarker(GeoPoint geoPoint) {
        DataLogEntry closestDataLogEntry = getClosesDataLogEntryToGeoPoint(geoPoint, gpsDataLog);
        // set Marker for new AccEvent, refresh map
        // IncidentLogEntry newIncidentLogEntry = incidentLog.updateOrAddIncident(IncidentLogEntry.newBuilder().withBaseInformation(closestDataLogEntry.timestamp, closestDataLogEntry.latitude, closestDataLogEntry.longitude).withRideInformation(bikeType,child,trailer,phoneLocation,INCIDENT_TYPE.NOTHING,null,false,null).build());
        IncidentLogEntry newIncidentLogEntry = incidentLog.updateOrAddIncident(IncidentLogEntry.newBuilder().withBaseInformation(closestDataLogEntry.timestamp, closestDataLogEntry.latitude, closestDataLogEntry.longitude).withIncidentType(INCIDENT_TYPE.NOTHING).build());
        setMarker(newIncidentLogEntry);
        activity.getmMapView().invalidate();

        // Now we display a dialog box to allow the user to decide if she/he is happy
        // with the location of the custom marker.

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(activity.getResources().getString(R.string.customIncidentAddedTitle));
        alertDialog.setMessage(activity.getResources().getString(R.string.customIncidentAddedMessage));
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        // NEGATIVE BUTTON: marker wasn't placed in the right location, remove from
        // map & markerMap.
        // Removal from ride.events and file not necessary as the new event hasn't been
        // added to those structures yet.
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, activity.getResources().getString(R.string.no),
                (DialogInterface dialog, int which) -> {
                    Marker custMarker = markerMap.get(newIncidentLogEntry.key);
                    activity.getmMapView().getOverlays().remove(custMarker);
                    //mother.getmMapView().getOverlayManager().remove(custMarker);
                    activity.getmMapView().invalidate();
                    markerMap.remove(custMarker);
                    incidentLog.removeIncident(newIncidentLogEntry);
                });

        // POSITIVE BUTTON: user approves of button. Add to ride.events & file.
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getResources().getString(R.string.yes),
                (DialogInterface dialog, int which) -> {
                });

        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        alertDialog.show();
    }

    public void setMarker(IncidentLogEntry incidentLogEntry) {
        Marker incidentMarker = new Marker(activity.getmMapView());
        /** I don't know why this exists, but this removes markers when adding new markers. DSP code.
         Marker previousMarker = markerMap.get(incidentLogEntry.key);
         if (previousMarker != null) {
         activity.getmMapView().getOverlays().remove(previousMarker);
         }
         */
        // Add the marker + corresponding key to map so we can manage markers if
        // necessary (e.g., remove them)
        markerMap.put(incidentLogEntry.key, incidentMarker);
        GeoPoint currentLocHelper = new GeoPoint(incidentLogEntry.latitude, incidentLogEntry.longitude);
        incidentMarker.setPosition(currentLocHelper);
        /* Different marker icons for ....
         * A) annotated y/n
         * B) default/custom
         */

        if (incidentLogEntry.isReadyForUpload()) {
            if (incidentLogEntry.key >= 1000) {
                incidentMarker.setIcon(markerCustom_ready);
            } else {
                incidentMarker.setIcon(markerAutoGenerated_ready);
            }
        } else if (incidentLogEntry.incidentType.equals(INCIDENT_TYPE.AUTO_GENERATED)) {
            incidentMarker.setIcon(markerAutoGenerated_edit);
        } else {
            incidentMarker.setIcon(markerCustom_edit);
        }


        InfoWindow infoWindow = new MyInfoWindow(R.layout.incident_bubble,
                activity.getmMapView(), activity, state, incidentLogEntry);
        incidentMarker.setInfoWindow(infoWindow);

        activity.getmMapView().getOverlays().add(incidentMarker);
        activity.getmMapView().invalidate();
    }

    private void setDistanceMarkers(List<IncidentLogEntry> incidents, @ColorInt Integer color) {
        List<IGeoPoint> points = simpleMap(incidents, x -> (IGeoPoint) new LabelledGeoPoint(x.latitude, x.longitude, ""));
        SimplePointTheme adapter = new SimplePointTheme(points, true);

        Paint pointStyle = new Paint();
        pointStyle.setStyle(Paint.Style.FILL);
        pointStyle.setColor(color);

        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(points.size() > 10000 ? SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION : SimpleFastPointOverlayOptions.RenderingAlgorithm.MEDIUM_OPTIMIZATION)
                .setRadius(10).setIsClickable(false).setCellSize(15).setPointStyle(pointStyle)
                .setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE);

        SimpleFastPointOverlay pointOverlay = new SimpleFastPointOverlay(adapter, opt);
        activity.getmMapView().getOverlays().add(pointOverlay);
//        activity.getmMapView().invalidate();
    }

    public void deleteAllMarkers() {
        for (Map.Entry<Integer, Marker> markerEntry : markerMap.entrySet()) {
            activity.getmMapView().getOverlays().remove(markerEntry.getValue());
        }
        activity.getmMapView().invalidate();
    }

    class SimpleThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread myThread = new Thread(r);
            myThread.setPriority(Thread.MIN_PRIORITY);
            return myThread;
        }
    }
}