package de.tuberlin.mcc.simra.app.entities;

import android.util.Log;

import androidx.annotation.Nullable;

public class DataLogEntry {
    @Nullable
    public final double latitude;
    @Nullable
    public final double longitude;
    public final double accelerometerX;
    public final double accelerometerY;
    public final double accelerometerZ;
    public final long timestamp;
    @Nullable
    public final double GPSAccuracy;
    @Nullable
    public final double gyroscopeA;
    @Nullable
    public final double gyroscopeB;
    @Nullable
    public final double gyroscopeC;
    @Nullable
    public final long RadmesserTimeStamp;
    @Nullable
    public final double RadmesserDistanceLeft1;
    @Nullable
    public final double RadmesserDistanceLeft2;
    @Nullable
    public final double RadmesserDistanceRight1;
    @Nullable
    public final double RadmesserDistanceRight2;

    private DataLogEntry(double latitude, double longitude, double accelerometerX, double accelerometerY, double accelerometerZ, long timestamp, double GPSAccuracy, double gyroscopeA, double gyroscopeB, double gyroscopeC, long radmesserTimeStamp, Double radmesserDistanceLeft1, @Nullable Double radmesserDistanceLeft2, @Nullable Double radmesserDistanceRight1, @Nullable Double radmesserDistanceRight2) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accelerometerX = accelerometerX;
        this.accelerometerY = accelerometerY;
        this.accelerometerZ = accelerometerZ;
        this.timestamp = timestamp;
        this.GPSAccuracy = GPSAccuracy;
        this.gyroscopeA = gyroscopeA;
        this.gyroscopeB = gyroscopeB;
        this.gyroscopeC = gyroscopeC;
        RadmesserTimeStamp = radmesserTimeStamp;
        RadmesserDistanceLeft1 = radmesserDistanceLeft1;
        RadmesserDistanceLeft2 = radmesserDistanceLeft2;
        RadmesserDistanceRight1 = radmesserDistanceRight1;
        RadmesserDistanceRight2 = radmesserDistanceRight2;
    }

    private DataLogEntry(Builder builder) {
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.accelerometerX = builder.accelerometerX;
        this.accelerometerY = builder.accelerometerY;
        this.accelerometerZ = builder.accelerometerZ;
        this.timestamp = builder.timestamp;
        this.GPSAccuracy = builder.GPSAccuracy;
        this.gyroscopeA = builder.gyroscopeA;
        this.gyroscopeB = builder.gyroscopeB;
        this.gyroscopeC = builder.gyroscopeC;
        RadmesserTimeStamp = builder.radmesserTimeStamp;
        RadmesserDistanceLeft1 = builder.radmesserDistanceLeft1;
        RadmesserDistanceLeft2 = builder.radmesserDistanceLeft2;
        RadmesserDistanceRight1 = builder.radmesserDistanceRight1;
        RadmesserDistanceRight2 = builder.radmesserDistanceRight2;
    }

    public static DataLogEntry parseDataLogEntryFromLine(String string) {
        String[] dataLogLine = string.split(",", -1);
        // No Radmesser Data appended, but GPS
        Builder dataLogEntry = DataLogEntry.newBuilder().withBaseValues(
                Double.parseDouble(dataLogLine[2]),
                Double.parseDouble(dataLogLine[3]),
                Double.parseDouble(dataLogLine[4]),
                Long.parseLong(dataLogLine[5])
        );

        // GPS Signal
        if (!dataLogLine[0].isEmpty() && !dataLogLine[1].isEmpty() && !dataLogLine[6].isEmpty()) {
            dataLogEntry.withGPS(
                    Double.parseDouble(dataLogLine[0]),
                    Double.parseDouble(dataLogLine[1]),
                    Double.parseDouble(dataLogLine[6])
            );
        }

        // Gyroscope Values
        if (!dataLogLine[7].isEmpty() && !dataLogLine[8].isEmpty() && !dataLogLine[9].isEmpty()) {
            dataLogEntry.withGyroscope(
                    Double.parseDouble(dataLogLine[7]),
                    Double.parseDouble(dataLogLine[8]),
                    Double.parseDouble(dataLogLine[9])
            );
        }

        // Radmesser Values
        dataLogEntry.withRadmesser(
                dataLogLine.length > 11 ? (!dataLogLine[11].isEmpty() ? Long.parseLong(dataLogLine[11]) : null) : null,
                dataLogLine.length > 10 ? (!dataLogLine[10].isEmpty() ? Double.parseDouble(dataLogLine[10]) : null) : null,
                dataLogLine.length > 12 ? (!dataLogLine[12].isEmpty()  ? Double.parseDouble(dataLogLine[12]) : null) : null,
                dataLogLine.length > 13 ? (!dataLogLine[13].isEmpty()  ? Double.parseDouble(dataLogLine[13]) : null) : null,
                dataLogLine.length > 14 ? (!dataLogLine[14].isEmpty()  ? Double.parseDouble(dataLogLine[14]) : null) : null
        );

        return dataLogEntry.build();

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static final class Builder {

        private double latitude;
        private double longitude;
        private double accelerometerX;
        private double accelerometerY;
        private double accelerometerZ;
        private long timestamp;
        private double GPSAccuracy;
        private double gyroscopeA;
        private double gyroscopeB;
        private double gyroscopeC;
        private long radmesserTimeStamp;
        private double radmesserDistanceLeft1;
        private double radmesserDistanceLeft2;
        private double radmesserDistanceRight1;
        private double radmesserDistanceRight2;

        private Builder() {
        }

        public Builder withBaseValues(double vAccelerometerX, double vAccelerometerY, double vAccelerometerZ, long vTimestamp) {
            accelerometerX = vAccelerometerX;
            accelerometerY = vAccelerometerY;
            accelerometerZ = vAccelerometerZ;
            timestamp = vTimestamp;
            return this;
        }

        public Builder withGPS(double vLatitude, double vLongitude, double vGPSAccuracy) {
            latitude = vLatitude;
            longitude = vLongitude;
            GPSAccuracy = vGPSAccuracy;
            return this;
        }

        public Builder withGyroscope(double vGyroscopeA, double vGyroscopeB, double vGyroscopeC) {
            gyroscopeA = vGyroscopeA;
            gyroscopeB = vGyroscopeB;
            gyroscopeC = vGyroscopeC;
            return this;
        }

        public Builder withRadmesser(long vRadmesserTimeStamp, Double vRadmesserDistanceLeft1, Double vRadmesserDistanceLeft2, Double vRadmesserDistanceRight1, Double vRadmesserDistanceRight2) {
            radmesserTimeStamp = vRadmesserTimeStamp;
            radmesserDistanceLeft1 = vRadmesserDistanceLeft1;
            if (vRadmesserDistanceLeft2 != null) {
                radmesserDistanceLeft2 = vRadmesserDistanceLeft2;
            }
            if (vRadmesserDistanceRight1 != null) {
                radmesserDistanceRight1 = vRadmesserDistanceRight1;
            }
            if (vRadmesserDistanceRight2 != null) {
                radmesserDistanceRight2 = vRadmesserDistanceRight2;
            }
            return this;
        }

        public DataLogEntry build() {
            return new DataLogEntry(this);
        }

    }
}
