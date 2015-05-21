package ru.ifmo.ctddev.slyusarenko.copier;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;

public class UIFileCopy {

    private static volatile boolean copyForever = false;
    private static volatile boolean notCopyForever = false;
    private static File toCopy;
    private static File copied;
    private static JProgressBar progressBar;
    private static JLabel speed;
    private static JLabel timeToDownload;
    private static JLabel averageSpeed;
    private static JLabel timeFromBeginning;
    private static long space;
    private static long copiedNow = 0;
    private static JFrame frame;
    private static int pastTime = 0;
    private static int measurements = 0;
    private static double sumSpeeds = 1.0d;
    private static boolean interrupted = false;

    private static boolean checkNeedToCopy(File file) {
        if (copyForever) {
            return true;
        } else if (notCopyForever) {
            return false;
        }
        return showDialog(file);
    }

    private static long getDirectorySize(File dir) {
        long size = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir.getPath()))) {
            for (Path path: stream) {
                File file = new File(path.toString());
                if (file.isDirectory()) {
                    size += getDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        } catch (IOException ignored) {
        }
        return size;
    }

    private static void copyFile(File file, File copyTo) {
        File checkExist = new File(copyTo.toString() + File.separator + file.getName());
        if (checkExist.exists()) {
            boolean checkNeedToCopy = checkNeedToCopy(new File(copyTo.toString() + File.separator + file.getName()));
            if (!checkNeedToCopy) {
                copiedNow += file.length();
                return;
            }
        }
        try (FileInputStream is = new FileInputStream(file);
             FileOutputStream os = new FileOutputStream(copyTo.toString() + File.separator + file.getName())) {
            byte[] buf = new byte[4096];
            int length;
            long startTimeMillis = System.currentTimeMillis();
            long copiedInSomeSeconds = 0;
            while ((length = is.read(buf)) > 0) {
                if (!interrupted) {
                    os.write(buf, 0, length);
                } else {
                    return;
                }
                copiedNow += length;
                int percents = (int) (copiedNow * 100 / space);
                SwingUtilities.invokeLater(() -> progressBar.setValue(percents));
                copiedInSomeSeconds += length;
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - startTimeMillis > 1000) {
                    int seconds = Math.round(currentTimeMillis - startTimeMillis) % 1000;
                    double currentSpeed = copiedInSomeSeconds / 1024 / 1024 / seconds;
                    sumSpeeds += currentSpeed;
                    measurements++;
                    double averSpeed = sumSpeeds / (double) measurements;
                    long remaining = (space - copiedNow) / 1000 / Math.round(averSpeed);
                    pastTime += seconds;
                    long time = pastTime;
                    SwingUtilities.invokeLater(() -> {
                        speed.setText("       Current speed: " + Math.round(currentSpeed) + " MB/second");
                        timeToDownload.setText("       Remaining time: " + remaining / 1000 + " seconds");
                        averageSpeed.setText("       Average speed: " + Math.round(averSpeed) + " MB/second");
                        timeFromBeginning.setText("       Past time: " + time + " seconds");
                    });
                    startTimeMillis = System.currentTimeMillis();
                    copiedInSomeSeconds = 0;
                }
                if (interrupted) {
                    return;
                }
            }
        } catch (OutOfMemoryError e) {
            JOptionPane.showMessageDialog(frame, "Not enough memory to copy file or directory");
        } catch (IOException ignored) {

        }
    }

    private static void copyDirectory(File dir, File copyTo) {
        copyTo = new File(copyTo.toString() + File.separator + dir.getName());
        boolean made;
        if (!interrupted) {
            made = copyTo.mkdirs();
        } else {
            return;
        }
        if (!made && !copyTo.exists()) {
            final File finalCopyTo = copyTo;
            try {
                SwingUtilities.invokeAndWait(() -> JOptionPane.showMessageDialog(frame, "Can't create directory " + finalCopyTo.toString()));
            } catch (InterruptedException | InvocationTargetException ignored) {
            }
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir.getPath()))) {
            for (Path path: stream) {
                if (interrupted) {
                    return;
                }
                File file = new File(path.toString());
                if (file.isDirectory()) {
                    copyDirectory(file, copyTo);
                } else {
                    copyFile(file, copyTo);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static boolean showDialog(File toCopy) {
        final boolean[] answer = new boolean[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                String[] options = {"Yes", "No", "Yes forever", "No forever"};
                int value = JOptionPane.showOptionDialog(null, " File " + toCopy.toString() + " already exists. Do you want to rewrite it?  ", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (value == 0) {
                    answer[0] = true;
                } else if (value == 1) {
                    answer[0] = false;
                } else  if (value == 2) {
                    answer[0] = true;
                    copyForever = true;
                } else {
                    answer[0] = false;
                    notCopyForever = true;
                }
            });
        } catch (InterruptedException | InvocationTargetException ignored) {
        }
        return answer[0];
    }

    public static void main(String[] args) throws InterruptedException {
        if (args == null || args[0] == null || args[1] == null) {
            System.err.println("Incorrect arguments");
            return;
        }
        toCopy = new File(args[0]);
        copied = new File(args[1]);
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Copier");
            frame.setSize(new Dimension((int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3), (int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 3)));
            frame.setMinimumSize(new Dimension((int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3), (int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 3)));
            frame.setLocation(((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - frame.getWidth()) / 2, ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - frame.getHeight()) / 2);
            if (!toCopy.exists()) {
                JOptionPane.showMessageDialog(frame, "File " + args[0] + " doesn't exist");
                frame.dispose();
                return;
            }
            if (!copied.exists()) {
                int dialogResult = JOptionPane.showConfirmDialog(frame, "Directory  " + args[1] + " doesn't exist. Do you want to create it?");
                if (dialogResult != JOptionPane.YES_OPTION) {
                    frame.dispose();
                    return;
                }
            }
            if (!copied.isDirectory()) {
                JOptionPane.showMessageDialog(frame, args[1] + " is not a directory");
                frame.dispose();
                return;
            }
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JButton cancel = new JButton("Cancel");
            progressBar = new JProgressBar();
            progressBar.setStringPainted(true);
            speed = new JLabel();
            timeToDownload = new JLabel();
            averageSpeed = new JLabel();
            timeFromBeginning = new JLabel();
            panel.add(progressBar, BorderLayout.PAGE_START);
            JPanel labels = new JPanel(new GridLayout(3, 1));
            labels.add(Box.createVerticalGlue());
            JPanel labels1 = new JPanel();
            labels1.setLayout(new BoxLayout(labels1, BoxLayout.Y_AXIS));
            labels1.add(timeFromBeginning);
            labels1.add(averageSpeed);
            labels1.add(speed);
            labels1.add(timeToDownload);
            labels.add(labels1);
            labels.add(Box.createVerticalGlue());
            panel.add(labels, BorderLayout.LINE_START);
            JPanel button = new JPanel(new FlowLayout(FlowLayout.TRAILING));
            button.add(cancel);
            panel.add(Box.createVerticalGlue());
            panel.add(button, BorderLayout.PAGE_END);
            frame.add(panel);
            frame.setVisible(true);
            cancel.addActionListener(e -> interrupted = true);
            timeFromBeginning.setText("       Past time: 0 seconds");
            averageSpeed.setText("       Average speed: Calculating...");
            speed.setText("       Current speed: Calculating...");
            timeToDownload.setText("       Remaining time: Calculating...");
        });
        if (toCopy.isFile()) {
            space = toCopy.length();
        } else {
            space = getDirectorySize(toCopy);
        }
        if (toCopy.isDirectory()) {
            copyDirectory(toCopy, copied);
        } else {
            copyFile(toCopy, copied);
        }
        frame.dispose();
    }

}
