package timelogger;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;

/**
 * A simple utility class that lets you very simply print an arbitrary
 * component. Just pass the component to the PrintUtilities.printComponent. The
 * component you want to print doesn't need a print method and doesn't have to
 * implement any interface or do anything special at all.
 * <P>
 * If you are going to be printing many times, it is marginally more efficient
 * to first do the following:
 * <PRE>
 *    PrintUtilities printHelper = new PrintUtilities(theComponent);
 * </PRE> then later do printHelper.print(). But this is a very tiny difference,
 * so in most cases just do the simpler
 * PrintUtilities.printComponent(componentToBePrinted).
 *
 * 7/99 Marty Hall, http://www.apl.jhu.edu/~hall/java/ May be freely used or
 * adapted. 3/2010 Modified by JD to include Linux CUPS error message.
 */
public class PrintUtilities implements Printable
{ // instance field
    private Component componentToBePrinted;
    private String kDialogError = "Error displaying Print Dialog. This is a known bug 156191.\n"
            + "On Linux, if you're using CUPS, try specifying an Orientation for the printer. \n"
            + "From the System -> Administration menu select Printing\n"
            + "Right-click on a printer and select Properties -> Job Options and set Orientation to Portrait\n"
            + "instead of Automatic Rotation";

    public static void printComponent(Component c)
    {
        new PrintUtilities(c).print();
    }

    public PrintUtilities(Component componentToBePrinted)
    {
        this.componentToBePrinted = componentToBePrinted;
    }

    public void print()
    {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        boolean dlgOK = false;
        try
        {
            dlgOK = printJob.printDialog();
        } catch (java.lang.NullPointerException ex)
        {
            JOptionPane.showMessageDialog(componentToBePrinted,
                    kDialogError, "Printing Error", JOptionPane.ERROR_MESSAGE);
        }
        if (dlgOK)
        {
            try
            {
                printJob.print();
            } catch (PrinterException pe)
            {
                JOptionPane.showMessageDialog(componentToBePrinted,
                        "Java Printing Error " + pe, "Printing Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex)
    {
        if (pageIndex > 0)
        {
            return (NO_SUCH_PAGE);
        }
        else
        {
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            disableDoubleBuffering(componentToBePrinted);
            componentToBePrinted.paint(g2d);
            enableDoubleBuffering(componentToBePrinted);
            return (PAGE_EXISTS);
        }
    }

    /**
     * The speed and quality of printing suffers dramatically if any of the
     * containers have double buffering turned on. So this turns if off
     * globally.
     *
     * @see #enableDoubleBuffering(Component c)
     */
    public static void disableDoubleBuffering(Component c)
    {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(false);
    }

    /**
     * Re-enables double buffering globally.
     */
    public static void enableDoubleBuffering(Component c)
    {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(true);
    }
}
