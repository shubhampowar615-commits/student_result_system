package com.studentresult.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.RepaintManager;

/**
 * Utility to print any Java Swing component directly using the standard Java Printing API.
 * This satisfies the requirement for a printable student report card without requiring external PDF libraries.
 */
public class PrintUtil implements Printable {

    private final Component componentToBePrinted;

    public PrintUtil(Component componentToBePrinted) {
        this.componentToBePrinted = componentToBePrinted;
    }

    /**
     * Prints the specified Swing component by presenting standard Print Dialog to user.
     *
     * @param component Component (e.g. Report Card Panel) to print
     * @param jobName Title for the print job
     * @return true if printed successfully, false if cancelled or failed
     */
    public static boolean printComponent(Component component, String jobName) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setJobName(jobName != null ? jobName : "Student Report Card");
        printJob.setPrintable(new PrintUtil(component));

        if (printJob.printDialog()) {
            try {
                printJob.print();
                return true;
            } catch (PrinterException pe) {
                System.err.println("Printing error: " + pe.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        // Move to printable margin area
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // Calculate scaling factor to fit the component on the printed page
        Dimension dim = componentToBePrinted.getSize();
        if (dim.width == 0 || dim.height == 0) {
            dim = componentToBePrinted.getPreferredSize();
            componentToBePrinted.setSize(dim);
        }

        double pageWidth = pageFormat.getImageableWidth();
        double pageHeight = pageFormat.getImageableHeight();

        double scaleX = pageWidth / dim.width;
        double scaleY = pageHeight / dim.height;
        // Maintain aspect ratio, shrink if larger than page
        double scale = Math.min(1.0, Math.min(scaleX, scaleY));

        g2d.scale(scale, scale);

        // Disable double buffering during printing for sharp vector/text output
        RepaintManager currentManager = RepaintManager.currentManager(componentToBePrinted);
        currentManager.setDoubleBufferingEnabled(false);

        componentToBePrinted.paint(g2d);

        currentManager.setDoubleBufferingEnabled(true);

        return PAGE_EXISTS;
    }
}

