/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.sternkn.djvu.gui.view_model.MainViewModel.APP_NAME;

public final class LogUtils {
    private static final String OS = System.getProperty("os.name", "").toLowerCase();
    private static final String USER_HOME = System.getProperty("user.home");

    private LogUtils() {
    }

    public static void init() {
        final Path logDir = getLogDir();

        try {
            Files.createDirectories(logDir);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create log directory: " + logDir, e);
        }

        System.setProperty("djvu.viewer.log.dir", logDir.toString());
    }

    private static Path getLogDir() {
        if (OS.contains("mac")) {
            return Paths.get(USER_HOME, "Library", "Logs", APP_NAME);
        }

        if (OS.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (StringUtils.isBlank(localAppData)) {
                return Paths.get(USER_HOME, "AppData", "Local", APP_NAME, "Logs");
            }
            else {
                return Paths.get(localAppData, APP_NAME, "Logs");
            }
        }

        String xdgStateHome = System.getenv("XDG_STATE_HOME");
        if (StringUtils.isBlank(xdgStateHome)) {
            return Paths.get(USER_HOME, ".local", "state", APP_NAME, "logs");
        }
        else {
            return Paths.get(xdgStateHome, APP_NAME, "logs");
        }
    }
}
