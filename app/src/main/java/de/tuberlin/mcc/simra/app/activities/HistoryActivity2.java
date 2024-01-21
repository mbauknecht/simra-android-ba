package de.tuberlin.mcc.simra.app.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.tuberlin.mcc.simra.app.R;
import de.tuberlin.mcc.simra.app.databinding.ActivityHistoryBinding;
import de.tuberlin.mcc.simra.app.entities.MetaData;
import de.tuberlin.mcc.simra.app.entities.Profile;
import de.tuberlin.mcc.simra.app.services.UploadService;
import de.tuberlin.mcc.simra.app.util.BaseActivity;
import de.tuberlin.mcc.simra.app.util.IOUtils;
import de.tuberlin.mcc.simra.app.util.SharedPref;

import static de.tuberlin.mcc.simra.app.util.IOUtils.copyTo;
import static de.tuberlin.mcc.simra.app.util.SharedPref.lookUpBooleanSharedPrefs;
import static de.tuberlin.mcc.simra.app.util.SharedPref.writeBooleanToSharedPrefs;
import static de.tuberlin.mcc.simra.app.util.Utils.fireProfileRegionPrompt;

public class HistoryActivity2 extends BaseActivity {
    private static final String TAG = "HistoryActivity_LOG";
    ActivityHistoryBinding binding;
    boolean exitWhenDone = false;
    String[] ridesArr;
    BroadcastReceiver br;

    public static void startHistoryActivity2(Context context) {
        Intent intent = new Intent(context, HistoryActivity2.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.toolbar.setTitle("");
        binding.toolbar.toolbar.setSubtitle("");
        binding.toolbar.toolbarTitle.setText(R.string.title_activity_history);
        binding.toolbar.backButton.setOnClickListener(v -> finish());

        binding.listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            LinearLayout historyButtons = binding.buttons;
            boolean isUp = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isUp && view.getLastVisiblePosition() + 1 == totalItemCount) {
                    historyButtons.animate().translationX(historyButtons.getWidth() / 2f);
                    isUp = false;
                } else if (!isUp && !(view.getLastVisiblePosition() + 1 == totalItemCount)) {
                    historyButtons.animate().translationX(0);
                    isUp = true;
                }
            }
        });

        binding.upload.setOnClickListener(view -> {
            if (!lookUpBooleanSharedPrefs("uploadWarningShown", false, "simraPrefs", HistoryActivity2.this)) {
                fireUploadPrompt();
            } else if (Profile.loadProfile(null, HistoryActivity2.this).region == 0) {
                fireProfileRegionPrompt(SharedPref.App.Regions.getLastSeenRegionsID(HistoryActivity2.this), HistoryActivity2.this);
            } else {
                Intent intent = new Intent(HistoryActivity2.this, UploadService.class);
                startService(intent);
                Toast.makeText(HistoryActivity2.this, getString(R.string.upload_started), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshMyRides() {
        List<String[]> metaDataLines = new ArrayList<>();
        File metaDataFile = IOUtils.Files.getMetaDataFile(this);

        if (metaDataFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(metaDataFile));
                br.readLine(); // skip the first line containing headers
                br.readLine(); // skip the second line
                String line;

                while ((line = br.readLine()) != null) {
                    if (!line.startsWith("key") && !line.startsWith("null")) {
                        metaDataLines.add(line.split(","));
                    }
                }
                Log.d(TAG, "metaDataLines: " + Arrays.deepToString(metaDataLines.toArray()));
            } catch (IOException e) {
                handleIOException(e);
            }

            ridesArr = new String[metaDataLines.size()];
            for (int i = 0; i < metaDataLines.size(); i++) {
                String[] metaDataLine = metaDataLines.get(i);
                if (metaDataLine.length > 2 && !metaDataLine[0].equals("key")) {
                    ridesArr[(metaDataLines.size() - i) - 1] = listToTextShape(metaDataLine);
                }
            }

            List<String> stringArrayList = new ArrayList<>(Arrays.asList(ridesArr));
            MyArrayAdapter myAdapter = new MyArrayAdapter(this, R.layout.row_icons, stringArrayList, metaDataLines);
            binding.listView.setAdapter(myAdapter);
        } else {
            handleNoMetaDataFile();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("de.tuberlin.mcc.simra.app.UPLOAD_COMPLETE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.registerReceiver(br, filter, RECEIVER_EXPORTED);
        } else {
            this.registerReceiver(br, filter);
        }

        refreshMyRides();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(br);
    }

    private String listToTextShape(String[] item) {
        String todo = getString(R.string.newRideInHistoryActivity);

        if ("1".equals(item[3])) {
            todo = getString(R.string.rideAnnotatedInHistoryActivity);
        } else if ("2".equals(item[3])) {
            todo = getString(R.string.rideUploadedInHistoryActivity);
        }

        // Ressources import

        long millis = Long.parseLong(item[2]) - Long.parseLong(item[1]);
        int minutes = Math.round((millis / 1000 / 60));
        Date dt = new Date(Long.parseLong(item[1]));
        Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
        localCalendar.setTime(dt);
        Locale locale = Resources.getSystem().getConfiguration().locale;
        SimpleDateFormat wholeDateFormat = new SimpleDateFormat(getString(R.string.datetime_format), locale);
        String datetime = wholeDateFormat.format(dt);

        if (item.length > 6) {
            return "#" + item[0] + ";" + datetime + ";" + todo + ";" + minutes + ";" + item[3] + ";" + item[6];
            // } else {
            //     return "#" + item[0]
        } else {
            return "#" + item[0] + ";" + datetime + ";" + todo + ";" + minutes + ";" + item[3] + ";0";
        }
    }

    public void fireDeletePrompt(int position, MyArrayAdapter arrayAdapter) {
        AlertDialog.Builder alert = new AlertDialog.Builder(HistoryActivity2.this);
        alert.setTitle(getString(R.string.warning));
        alert.setMessage(getString(R.string.delete_file_warning));
        alert.setPositiveButton(R.string.delete_ride_approve, (dialog, id) -> {
            deleteRideFiles(position);
            MetaData.deleteMetaDataEntryForRide(Integer.parseInt(getClickedRideId(position)), this);
            Toast.makeText(HistoryActivity2.this, R.string.ride_deleted, Toast.LENGTH_SHORT).show();
            refreshMyRides();
        });
        alert.setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        alert.show();
    }

    private void deleteRideFiles(int position) {
        File[] dirFiles = getFilesDir().listFiles();
        String clicked = (String) binding.listView.getItemAtPosition(position);
        clicked = clicked.replace("#", "").split(";")[0];

        if (dirFiles.length != 0) {
            for (File actualFile : dirFiles) {
                if (actualFile.getName().startsWith(clicked + "_") || actualFile.getName().startsWith("accEvents" + clicked)) {
                    Log.i(TAG, actualFile.getName() + " deleted: " + actualFile.delete());
                }
            }
        }
    }

    private String getClickedRideId(int position) {
        String clicked = (String) binding.listView.getItemAtPosition(position);
        return clicked.replace("#", "").split(";")[0];
    }

    public void fireUploadPrompt() {
        AlertDialog.Builder alert = new AlertDialog.Builder(HistoryActivity2.this);
        alert.setTitle(getString(R.string.warning));
        alert.setMessage(getString(R.string.upload_file_warning));
        alert.setPositiveButton(R.string.upload, (dialog, id) -> {
            handleUploadButtonClick();
        });
        alert.setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        alert.show();
    }

    private void handleUploadButtonClick() {
        if (Profile.loadProfile(null, HistoryActivity2.this).region == 0) {
            fireProfileRegionPrompt(SharedPref.App.Regions.getLastSeenRegionsID(HistoryActivity2.this), HistoryActivity2.this);
        } else {
            writeBooleanToSharedPrefs("uploadWarningShown", true, "simraPrefs", HistoryActivity2.this);
            Intent intent = new Intent(HistoryActivity2.this, UploadService.class);
            startService(intent);
            Toast.makeText(HistoryActivity2.this, getString(R.string.upload_started), Toast.LENGTH_SHORT).show();
            if (exitWhenDone) {
                HistoryActivity2.this.moveTaskToBack(true);
            }
        }
    }

    // ... (continue with the rest of the code)

    int longClickedRideID = -1;

    private final ActivityResultLauncher<Uri> exportRideToLocation =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            handleExportRideActivityResult(uri);
                        }
                    });


    /* handleExportRideActivityResult twice
    private void handleExportRideActivityResult(Uri uri) {
        boolean successfullyExportedGPSPart = copyTo(IOUtils.Files.getGPSLogFile(longClickedRideID, false, HistoryActivity2.this), uri, HistoryActivity2.this);
        boolean successfullyExportedIncidentPart = copyTo(IOUtils.Files.getIncidentLogFile(longClickedRideID, false, HistoryActivity2.this), uri, HistoryActivity2.this);

        if (successfullyExportedGPSPart && successfullyExportedIncidentPart) {
            Toast.makeText(HistoryActivity2.this, R.string.exportRideSuccessToast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(HistoryActivity2.this, R.string.exportRideFailToast, Toast.LENGTH_SHORT).show();
        }
    }


     */
    // ... (continue with the rest of the code)

    public class MyArrayAdapter extends ArrayAdapter<String> {
        String TAG = "MyArrayAdapter_LOG";
        Context context;
        int layoutResourceId;
        List<String> stringList;
        List<String[]> metaDataLines;

        public MyArrayAdapter(Context context, int layoutResourceId, List<String> stringList, List<String[]> metaDataLines) {
            super(context, layoutResourceId, stringList);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.stringList = stringList;
            this.metaDataLines = metaDataLines;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new Holder();
                holder.rideDate = row.findViewById(R.id.row_icons_ride_date);
                holder.rideTime = row.findViewById(R.id.row_ride_time);
                holder.duration = row.findViewById(R.id.row_duration);
                holder.distance = row.findViewById(R.id.row_distance);
                holder.distanceUnit = row.findViewById(R.id.row_distanceKM);
                holder.status = row.findViewById(R.id.statusBtn);
                holder.btnDelete = row.findViewById(R.id.deleteBtn);
                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            String[] itemComponents = stringList.get(position).split(";");
            holder.rideDate.setText(itemComponents[1].split(",")[0]);
            holder.rideTime.setText(itemComponents[1].split(",")[1]);

            if (itemComponents[2].contains(getString(R.string.rideAnnotatedInHistoryActivity))) {
                holder.status.setBackground(getDrawable(R.drawable.ic_phone_android_black_24dp));
            } else if (itemComponents[2].contains(getString(R.string.rideUploadedInHistoryActivity))) {
                holder.status.setBackground(getDrawable(R.drawable.ic_cloud_done_black_24dp));
            } else {
                holder.status.setBackground(null);
            }

            holder.duration.setText(itemComponents[3]);

            if (SharedPref.Settings.DisplayUnit.isImperial(HistoryActivity2.this)) {
                holder.distance.setText(String.valueOf(Math.round(((Double.parseDouble(itemComponents[5]) / 1600) * 100.0)) / 100.0));
                holder.distanceUnit.setText("mi");
            } else {
                holder.distance.setText(String.valueOf(Math.round(((Double.parseDouble(itemComponents[5]) / 1000) * 100.0)) / 100.0));
                holder.distanceUnit.setText("km");
            }

            if (!itemComponents[4].equals("2")) {
                holder.btnDelete.setVisibility(View.VISIBLE);
            } else {
                holder.btnDelete.setVisibility(View.INVISIBLE);
            }

            row.setOnClickListener(v -> {
                File[] dirFiles = new File(IOUtils.Directories.getBaseFolderPath(context)).listFiles();
                String clicked = (String) binding.listView.getItemAtPosition(position);
                clicked = clicked.replace("#", "").split(";")[0];

                if (dirFiles.length != 0) {
                    for (File dirFile : dirFiles) {
                        String fileOutput = dirFile.getName();
                        if (fileOutput.startsWith(clicked + "_")) {
                            ShowRouteActivity.startShowRouteActivity(Integer.parseInt(fileOutput.split("_", -1)[0]),
                                    Integer.parseInt(metaDataLines.get(metaDataLines.size() - position - 1)[3]), true, HistoryActivity2.this);
                        }
                    }
                }
            });

            row.setOnLongClickListener(view -> {
                String clicked = (String) binding.listView.getItemAtPosition(position);
                longClickedRideID = Integer.parseInt(clicked.split(";")[0].substring(1));
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity2.this).setTitle(R.string.exportRideTitle);
                builder.setMessage(R.string.exportRideButtonText);
                builder.setPositiveButton(R.string.continueText, (dialog, which) -> exportRideToLocation.launch(Uri.parse(DocumentsContract.PROVIDER_INTERFACE)));
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
                return true;
            });

            holder.btnDelete.setOnClickListener(v -> fireDeletePrompt(position, MyArrayAdapter.this));
            return row;
        }

        class Holder {
            TextView rideDate;
            TextView rideTime;
            TextView duration;
            TextView distance;
            TextView distanceUnit;
            ImageButton status;
            ImageButton btnDelete;
        }
    }

    private void handleIOException(IOException e) {
        Log.e(TAG, "Exception in refreshMyRides(): " + e.getMessage());
        Log.e(TAG, Arrays.toString(e.getStackTrace()));
        e.printStackTrace();
    }

    private void handleNoMetaDataFile() {
        Log.d(TAG, "metaData.csv doesn't exist");
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), (getString(R.string.noHistory)), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void handleExportRideButtonClick() {
        if (Profile.loadProfile(null, HistoryActivity2.this).region == 0) {
            // fireProfileRegion

            //  RegionPrompt(SharedPref.App.Regions.getLastSeenRegionsID(HistoryActivity2.this), HistoryActivity2.this);
        } else {
            writeBooleanToSharedPrefs("uploadWarningShown", true, "simraPrefs", HistoryActivity2.this);
            Intent intent = new Intent(HistoryActivity2.this, UploadService.class);
            startService(intent);
            Toast.makeText(HistoryActivity2.this, getString(R.string.upload_started), Toast.LENGTH_SHORT).show();
            if (exitWhenDone) {
                HistoryActivity2.this.moveTaskToBack(true);
            }
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleBroadcastIntent(context, intent);
        }
    }

    private void handleBroadcastIntent(Context context, Intent intent) {
        boolean uploadSuccessful = intent.getBooleanExtra("uploadSuccessful", false);
        boolean foundARideToUpload = intent.getBooleanExtra("foundARideToUpload", true);

        if (!foundARideToUpload) {
            Toast.makeText(context, R.string.nothing_to_upload, Toast.LENGTH_LONG).show();
        } else if (!uploadSuccessful) {
            Toast.makeText(context, R.string.upload_failed, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, R.string.upload_completed, Toast.LENGTH_LONG).show();
        }

        refreshMyRides();
    }

    // exportRideToLocation twice
    /*

    private final ActivityResultLauncher<Uri> exportRideToLocation =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            handleExportRideActivityResult(uri);
                        }
                    });
*/
    // URI import

    private void handleExportRideActivityResult(Uri uri) {
        boolean successfullyExportedGPSPart = copyTo(IOUtils.Files.getGPSLogFile(longClickedRideID, false, HistoryActivity2.this), uri, HistoryActivity2.this);
        boolean successfullyExportedIncidentPart = copyTo(IOUtils.Files.getIncidentLogFile(longClickedRideID, false, HistoryActivity2.this), uri, HistoryActivity2.this);

        if (successfullyExportedGPSPart && successfullyExportedIncidentPart) {
            Toast.makeText(HistoryActivity2.this, R.string.exportRideSuccessToast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(HistoryActivity2.this, R.string.exportRideFailToast, Toast.LENGTH_SHORT).show();
        }
    }
}
