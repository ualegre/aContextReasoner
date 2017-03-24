/*
 * Copyright 2017 aContextReasoner Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.deansserver.contextserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * MySQL Database for the context service
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class MySQLDatabase implements Database{

    private static final String DB_URL = "jdbc:mysql://localhost:3306/poseidon_log?autoReconnect=true";
    private static final String DB_USER = "";
    private static final String DB_PASS = "";
    private Connection mConnection;
    private boolean mStarted = false;

    private static final String LOG_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS `log` (\n" +
                    "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                    "  `user_id` smallint(6) NOT NULL,\n" +
                    "  `event_origin` tinyint(4) NOT NULL,\n" +
                    "  `event_location` varchar(25) NOT NULL,\n" +
                    "  `event_date` datetime NOT NULL,\n" +
                    "  `event_text` varchar(200) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  KEY `user` (`user_id`)\n" +
                    ")";

    private static final String USER_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS `user` (\n" +
                    "  `id` smallint(6) NOT NULL AUTO_INCREMENT,\n" +
                    "  `user_id` varchar(40) NOT NULL,\n" +
                    "  `device_id` varchar(50) NOT NULL,\n" +
                    "  `learning` int(1) NOT NULL DEFAULT '1',\n" +
                    "  `last_sync` datetime NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ")";

    private static final String ADD_EVENT_STRING =
            "INSERT INTO log (user_id, event_origin, event_location, event_date, event_text)" +
                    "VALUES (?,?,?,?,?);";

    private static final String UPDATE_USER_STRING =
            "UPDATE user SET last_sync= ? WHERE id = ?;";

    private static final String UPDATE_USER_INFO_STRING =
            "UPDATE user SET user_id=? WHERE id=?;";

    private static final String NEW_USER_STRING =
            "INSERT INTO user (user_id, device_id, last_sync) VALUES (?,?,?);";

    private static final String UPDATE_USER_LEARNING_STRING =
            "UPDATE user SET learning=? WHERE id=?;";

    private static final String GET_USER_ID_STRING =
            "SELECT id from user WHERE device_id=?;";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MySQLDatabase() {
        getConnection();
    }

    private Connection getConnection() {
        try {

            if (mConnection == null || ! mConnection.isValid(1)) {
                mConnection = (Connection) DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

                if (! mStarted) {
                    mConnection.createStatement().execute(LOG_TABLE_CREATE);
                    mConnection.createStatement().execute(USER_TABLE_CREATE);
                    mStarted = true;
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            mConnection = null;
        }

        return mConnection;
    }

    public void close() throws SQLException {
        if (mConnection == null || ! mConnection.isValid(1)) {
            mConnection.close();
        }
    }

    @Override
    public int logEvents(int user, int rows, List<Integer> origins, List<String> locations,
                         List<Long> dates, List<String> texts) throws SQLException {

        PreparedStatement addEvent = null;
        PreparedStatement updateUserSync = null;

        Connection con = getConnection();

        if (con == null) {
            System.err.println("Cannot get connection");
            return -1000;
        }

        try {
            con.setAutoCommit(false);
            addEvent = con.prepareStatement(ADD_EVENT_STRING);

            for (int i = 0; i < rows; i++) {

                addEvent.setInt(1, user);
                addEvent.setInt(2, origins.get(i));
                addEvent.setString(3, locations.get(i));
                addEvent.setString(4, sdf.format(new Date(dates.get(i) * 1000)));
                addEvent.setString(5, texts.get(i));
                addEvent.addBatch();
            }

            addEvent.executeBatch();

            updateUserSync = updateSyncTime(con, user);

            con.commit();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            con.rollback();
        } finally {

            if (addEvent != null) {
                addEvent.close();
            }

            if (updateUserSync != null) {
                updateUserSync.close();
            }
        }

        return 1;
    }

    private PreparedStatement updateSyncTime(Connection con, int user) throws SQLException {

        PreparedStatement updateUserSync = con.prepareStatement(UPDATE_USER_STRING);

        updateUserSync.setString(1, sdf.format(new Date()));
        updateUserSync.setInt(2, user);

        updateUserSync.execute();

        return updateUserSync;
    }

    @Override
    public int registerUser(int user, String username, String device) throws SQLException {

        PreparedStatement updateUser = null;
        PreparedStatement newUser = null;
        PreparedStatement updateUserSync = null;

        Connection con = getConnection();

        if (con == null) {
            System.err.println("Cannot get connection");
            return -1000;
        }

        try {

            con.setAutoCommit(false);

            if (user == -1) {
                user = getUserId(device);
                if(user == -1) {
                    newUser = con.prepareStatement(NEW_USER_STRING);
                    newUser.setString(1, username);
                    newUser.setString(2, device);
                    newUser.setString(3, sdf.format(new Date()));
                    newUser.execute();
                }
            } else {
                updateUser = con.prepareStatement(UPDATE_USER_INFO_STRING);
                updateUser.setString(1, username);
                updateUser.setInt(2, user);
                updateUser.execute();

                updateUserSync = updateSyncTime(con, user);
            }

            con.commit();

            if (user == -1) {
                user = getUserId(device);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            con.rollback();

        } finally {
            if (updateUser != null) {
                updateUser.close();
            }

            if (newUser != null) {
                newUser.close();
            }

            if (updateUserSync != null) {
                updateUserSync.close();
            }
        }

        return user;
    }

    @Override
    public int setLearning(int user, boolean mode) throws SQLException {

        int dbresponse = -1;

        PreparedStatement updateUserLearning = null;
        PreparedStatement updateUserSync = null;

        Connection con = getConnection();

        if (con == null) {
            System.err.println("Cannot get connection");
            return -1000;
        }

        try {
            updateUserLearning = con
                    .prepareStatement(UPDATE_USER_LEARNING_STRING);

            if (mode) {
                updateUserLearning.setInt(1, 1);
            } else {
                updateUserLearning.setInt(1, 0);
            }

            updateUserLearning.setInt(2, user);

            if (updateUserLearning.executeUpdate() == 1) {
                dbresponse = 1;
                updateUserSync = updateSyncTime(con, user);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            if (updateUserLearning != null) {
                updateUserLearning.close();
            }

            if (updateUserSync != null) {
                updateUserSync.close();
            }
        }

        return dbresponse;
    }

    @Override
    public int getUserId(String device) throws SQLException {

        Connection con = getConnection();

        if (con == null) {
            System.err.println("Cannot get connection");
            return -1000;
        }

        PreparedStatement selectUser = con.prepareStatement(GET_USER_ID_STRING);
        selectUser.setString(1, device);
        ResultSet rs = selectUser.executeQuery();
        int user = -1;

        while (rs.next()) {
            user = rs.getInt(1);
        }

        return user;
    }
}
