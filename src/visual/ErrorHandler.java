/* 
 * Copyright (C) 2016 Serphentas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package visual;

import java.awt.TrayIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public final class ErrorHandler {

    public static void showError(Exception e) {
        setUIStyle();
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()
                + "\nPlease contact support.", "Error", TrayIcon.MessageType.ERROR.ordinal());

    }

    public static void showError(String message) {
        setUIStyle();
        JOptionPane.showMessageDialog(null, "Error: " + message,
                "Error", TrayIcon.MessageType.ERROR.ordinal());
    }

    private static void setUIStyle() {
        try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error initializing UI style: "
                    + e.getMessage() + "\nPlease contact support.", "Error",
                    TrayIcon.MessageType.ERROR.ordinal());
        }
    }
}