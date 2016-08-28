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


import java.sql.SQLException;
import java.util.List;

/**
 * General DB Interface
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public interface Database {

    int logEvents(int user, int rows, List<Integer> origins, List<String> locations,
                         List<Long> dates, List<String> texts) throws SQLException;

    int registerUser(int user, String username, String device) throws SQLException;

    int setLearning(int user, boolean mode);

}
