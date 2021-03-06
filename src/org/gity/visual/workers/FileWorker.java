/* 
 * Copyright (c) 2016, Serphentas
 * All rights reserved.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/ or send a letter
 * to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */
package org.gity.visual.workers;

import java.io.File;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.gity.internal.Settings;
import org.gity.internal.network.Control;
import org.gity.internal.network.IO;
import org.gity.visual.DefaultFrame;
import org.gity.visual.ErrorHandler;

/**
 * Processes GUI file I/O actions by sending them to low-level classes
 *
 * @author Serphentas
 */
public class FileWorker extends SwingWorker<Integer, String> {

    private static final String[] buttons = {"Yes", "Yes to all", "No", "Cancel"};

    private static JTable fileQueue;
    private static String srcFilePath,
            dstFilePath;
    private static File dstFile,
            srcFile;

    private static void failIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("FileWorker interrupted");
        }
    }

    /**
     * Creates a FileWorker with the given file queue as parameter
     *
     * @param fileQueue file queue to work with
     */
    public FileWorker(JTable fileQueue) {
        FileWorker.fileQueue = fileQueue;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        Settings.setIsWorking(true);
        int returnVal = -1;
        boolean yesForAll = false,
                isQueueEmpty = false,
                existsFile = false;

        while (!isQueueEmpty) {
            fileQueue.setValueAt("In progress", 0, 2);
            srcFilePath = (String) fileQueue.getValueAt(0, 0);
            dstFilePath = (String) fileQueue.getValueAt(0, 1);

            if (fileQueue.getValueAt(0, 3).equals("Upload")) {
                srcFile = new File(srcFilePath);

                if (srcFile.length() > 6e10) {
                    JOptionPane.showMessageDialog(null, "Error: could not upload the file " + srcFile.
                            getName() + ".\nAt the present time, uploads are capped at 60GB.", "Upload file",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    if (!yesForAll && Control.exists(dstFilePath)) {
                        returnVal = JOptionPane.showOptionDialog(null, "File "
                                + srcFile.getName() + " already exists. Overwrite ?",
                                "Overwrite", JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE, null, buttons,
                                buttons[2]);
                        yesForAll = returnVal == 1;
                        existsFile = true;
                    }

                    if (returnVal < 2) {
                        if (existsFile) {
                            Control.rm(dstFilePath);
                            existsFile = false;
                        }

                        String prefix = "could not upload " + (String) fileQueue.getValueAt(0, 0) + ".\n";
                        switch (IO.upload(srcFile, dstFilePath)) {
                            case 3:
                                ErrorHandler.showError(prefix + "Remote filename is too short.");
                                break;
                            case 1:
                                ErrorHandler.showError(prefix + "File already exists.");
                                break;
                            case -2:
                                ErrorHandler.showError(prefix + "An I/O problem occured.");
                                break;
                        }
                    }
                }
            } else if (fileQueue.getValueAt(0, 3).equals("Download")) {
                dstFile = new File(dstFilePath);

                if (!yesForAll && dstFile.isFile() && dstFile.exists()) {
                    returnVal = JOptionPane.showOptionDialog(null, "File "
                            + dstFile.getName() + " already exists. Overwrite ?",
                            "Overwrite", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, buttons,
                            buttons[2]);
                    yesForAll = returnVal == 1;
                }

                if (returnVal < 2) {
                    if (dstFile.exists()) {
                        dstFile.delete();
                    }

                    String prefix = "could not download " + (String) fileQueue.getValueAt(0, 0) + ".\n";
                    switch (IO.download(srcFilePath, dstFile)) {
                        case 3:
                            ErrorHandler.showError(prefix + "Remote filename is too short.");
                            break;
                        case 2:
                            ErrorHandler.showError(prefix + "File does not exist.");
                            break;
                        case -1:
                            ErrorHandler.showError(prefix + "I/O error occured.");
                            break;
                        case -2:
                            ErrorHandler.showError(prefix + "I/O error occured.");
                            break;
                    }
                }
            }

            DefaultTableModel dtm = (DefaultTableModel) fileQueue.getModel();
            dtm.removeRow(0);
            fileQueue.setModel(dtm);
            isQueueEmpty = fileQueue.getRowCount() == 0;
            DefaultFrame.updateFileTable();
        }

        Settings.setIsWorking(false);
        return 1;
    }

    @Override
    protected void process(List<String> chunks) {
        chunks.stream().forEach((s) -> {
            //log.append(s + "\n");
        });
    }
}
