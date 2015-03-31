/*
 * Copyright 2015 POSEIDON Project
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

package org.poseidon_project.context.utility;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Utility Class to hold static file operations
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class FileOperations {

    private static final String LOGTAG  = "FileOperations";

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();

            for(File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }

    public static boolean copyFile(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;

        return true;
    }

    public static boolean copyFile(InputStream in, String outputPath) throws IOException {

        File output = new File(outputPath);

        ensureTargetDirectoryExists(output.getParentFile());

        OutputStream out = new FileOutputStream(output);

        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;

        return true;
    }

    public static String convertStreamToString(final InputStream input) {
        if (input == null) return null;

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        final StringBuilder sBuf = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sBuf.append(line);
            }
        } catch (IOException e) {
            Log.e(LOGTAG, e.getMessage());
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                Log.e(LOGTAG, e.getMessage());
            }
        }
        return sBuf.toString();
    }

    public static void ensureTargetDirectoryExists(File aTargetDir){
        if(!aTargetDir.exists()){
            aTargetDir.mkdirs();
        }
    }
}
