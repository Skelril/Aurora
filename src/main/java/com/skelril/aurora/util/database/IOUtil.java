/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.aurora.util.database;

import com.sk89q.commandbook.CommandBook;

import java.io.*;
import java.util.logging.Logger;

public class IOUtil {

    private static Logger log = CommandBook.logger();

    public static File getBinaryFile(File workingDir, String fileName) {

        return new File(workingDir.getPath() + "/" + fileName + ".dat");
    }

    public static void toBinaryFile(File workingDir, String fileName, Object object) {

        fileName += ".dat";

        File file = new File(workingDir.getPath() + "/" + fileName);

        if (file.exists()) {
            File oldFile = new File(workingDir.getPath() + "/old-" + fileName);
            if (!oldFile.exists() || oldFile.delete()) {
                if (!file.renameTo(oldFile)) {
                    log.warning("Failed to rename binary file: " + fileName + "!");
                    return;
                }
            }
        }

        try {
            if (!workingDir.exists() && !workingDir.mkdirs()) {
                log.warning("Failed to create binary file: " + fileName + "!");
                return;
            }

            file.createNewFile();
        } catch (IOException e) {
            log.warning("Failed to create binary file: " + fileName + "!");
            log.warning(e.getMessage());
            return;
        }


        try (
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oss = new ObjectOutputStream(fos)
        ) {
            oss.writeObject(object);
        } catch (FileNotFoundException e) {
            log.warning("Failed to find binary file: " + fileName + "!");
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning("Failed to write binary file: " + fileName + "!");
            log.warning(e.getMessage());
        }
    }

    public static Object readBinaryFile(File file) {

        Object object = null;
        try (
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            object = ois.readObject();
        } catch (FileNotFoundException e) {
            log.warning("Failed to find a binary file!");
            log.warning(e.getMessage());
        } catch (ClassNotFoundException e) {
            log.warning("Couldn't find a compatible class for the binary file!");
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning("Failed to read a binary file!");
            log.warning(e.getMessage());
        }

        return object;
    }
}
