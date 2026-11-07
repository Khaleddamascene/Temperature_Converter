package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public final class TravelRepository implements AutoCloseable {
    private final Connection connection;
    public record Measurement(long id, String trip, double distance, double hours, double speed) { }

    public TravelRepository(String url, String user, String password) throws SQLException {
        this(DriverManager.getConnection(url, user, password));
    }

    public TravelRepository(Connection connection) {
        this.connection = connection;
    }

    public record UnitText(String name, String description) {}
    public java.util.Map<String, UnitText> units(String language) throws SQLException {
        var units = new java.util.HashMap<String, UnitText>();
        try (var query = connection.prepareStatement("""
                SELECT en.unit_id, COALESCE(t.name,en.name), COALESCE(t.description,en.description)
                FROM unit_translation en LEFT JOIN unit_translation t
                ON t.unit_id=en.unit_id AND t.lang=? WHERE en.lang='en'
                """)) {
            query.setString(1, language);
            try (var rows = query.executeQuery()) {
                while (rows.next()) units.put(rows.getString(1), new UnitText(rows.getString(2), rows.getString(3)));
            }
        }
        return units;
    }

    public record Conversion(double input, String source, String target, double result) {}
    public void saveConversion(Conversion conversion, String language) throws SQLException {
        try (var insert = connection.prepareStatement(
                "INSERT INTO conversion(input_value,source,target,result,lang) VALUES (?,?,?,?,?)")) {
            insert.setDouble(1, conversion.input());
            insert.setString(2, conversion.source());
            insert.setString(3, conversion.target());
            insert.setDouble(4, conversion.result());
            insert.setString(5, language);
            insert.executeUpdate();
        }
    }

    public List<Conversion> recentConversions() throws SQLException {
        var history = new ArrayList<Conversion>();
        try (var query = connection.createStatement(); var rows = query.executeQuery(
                "SELECT input_value,source,target,result FROM conversion ORDER BY id DESC LIMIT 5")) {
            while (rows.next()) history.add(new Conversion(rows.getDouble(1), rows.getString(2), rows.getString(3), rows.getDouble(4)));
        }
        return history;
    }
    public void save(String trip, double distance, double hours) throws SQLException {
        if (trip == null || trip.isBlank() || trip.trim().length() > 100)
            throw new IllegalArgumentException("Trip name required (1-100 characters)");
        double speed = TravelCalculator.speed(distance, hours);
        connection.setAutoCommit(false);
        try {
            long tripId;
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO trips(name) VALUES (?) ON DUPLICATE KEY UPDATE name=VALUES(name)")) {
                insert.setString(1, trip.trim()); insert.executeUpdate();
            }
            try (PreparedStatement select = connection.prepareStatement("SELECT id FROM trips WHERE name = ?")) {
                select.setString(1, trip.trim());
                try (ResultSet result = select.executeQuery()) { result.next(); tripId = result.getLong(1); }
            }
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO measurements(trip_id,distance_km,duration_hours,speed_kmh) VALUES (?,?,?,?)")) {
                insert.setLong(1, tripId); insert.setDouble(2, distance); insert.setDouble(3, hours); insert.setDouble(4, speed); insert.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) { connection.rollback(); throw e; }
        finally { connection.setAutoCommit(true); }
    }
    public List<Measurement> findAll() throws SQLException {
        List<Measurement> rows = new ArrayList<>();
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SELECT m.id,t.name,m.distance_km,m.duration_hours,m.speed_kmh FROM measurements m JOIN trips t ON t.id=m.trip_id ORDER BY m.id DESC")) {
            while (result.next()) rows.add(new Measurement(result.getLong(1), result.getString(2), result.getDouble(3), result.getDouble(4), result.getDouble(5)));
        }
        return rows;
    }
    @Override public void close() throws SQLException { connection.close(); }
}
