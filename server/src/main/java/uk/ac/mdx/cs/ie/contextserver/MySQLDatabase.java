/*
 * Copyright 2016 Middlesex University
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

package uk.ac.mdx.cs.ie.contextserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * MySQL Database for the context service
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class MySQLDatabase implements Database{

    private static final String DB_URL = "";
    private static final String DB_USER = "";
    private static final String DB_PASS = "";
    private Connection mConnection;

    private static final String ADD_EVENT_STRING =
            "INSERT INTO log (user_id, event_origin, event_location, event_date, event_text)" +
                    "VALUES (?,?,?,?,?);";

    private static final String UPDATE_USER_STRING =
            "UPDATE user SET last_sync= ? WHERE id = ?;";

    private static final String UPDATE_USER_INFO_STRING =
            "UPDATE user SET user_id=? WHERE id=? AND device_id=?;";

    private static final String NEW_USER_STRING =
            "INSERT INTO user (user_id, device_id) VALUES (?,?);";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Connection newConnection() {

        try {
            mConnection = (Connection) DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mConnection;
    }

    @Override
    public int logEvents(int user, int rows, List<Integer> origins, List<String> locations,
                         List<Long> dates, List<String> texts) throws SQLException {

        PreparedStatement addEvent = null;
        PreparedStatement updateUser = null;

        try {
            mConnection.setAutoCommit(false);
            addEvent = mConnection.prepareStatement(ADD_EVENT_STRING);

            for (int i = 0; i < rows; i++) {

                addEvent.setInt(1, user);
                addEvent.setInt(2, origins.get(i));
                addEvent.setString(3, locations.get(i));
                addEvent.setString(4, sdf.format(new Date(dates.get(i))));
                addEvent.setString(5, texts.get(i));
                addEvent.addBatch();
            }

            addEvent.executeBatch();

            updateUser = mConnection.prepareStatement(UPDATE_USER_STRING);

            updateUser.setString(1, sdf.format(new Date()));
            updateUser.setInt(2, user);

            updateUser.execute();

            mConnection.commit();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            mConnection.rollback();
        } finally {

            if (addEvent != null) {
                addEvent.close();
            }

            if (updateUser != null) {
                updateUser.close();
            }

            mConnection.close();
        }

        return 1;
    }

    @Override
    public int registerUser(int user, String username, String device) throws SQLException {

        PreparedStatement updateUser = null;
        PreparedStatement newUser = null;

        try {

            mConnection.setAutoCommit(false);

            if (user == -1) {
                newUser = mConnection.prepareStatement(NEW_USER_STRING);
                newUser.setString(1, username);
                newUser.setString(2, device);
                newUser.execute();
            } else {
                updateUser = mConnection.prepareStatement(UPDATE_USER_INFO_STRING);
                updateUser.setInt(1, user);
                updateUser.setString(2, username);
                updateUser.setString(3, device);
                updateUser.execute();
            }

            mConnection.commit();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            mConnection.rollback();

        } finally {
            if (updateUser != null) {
                updateUser.close();
            }

            if (newUser != null) {
                newUser.close();
            }
        }

        return 0;
    }

    @Override
    public int setLearning(int user, boolean mode) {
        return 0;
    }
}
