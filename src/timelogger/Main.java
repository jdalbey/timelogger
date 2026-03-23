package timelogger;

import javax.swing.*;

/**
 * Main executable for the TimeLogger tool.
 *
 * @author David Wheelwright
 */
public class Main
{
    /**
     * Main execution method for the TimeLogger program. Construct an instance
     * of both the model and view classes, and let them know about each other.
     *
     * @param args String name of a .tlg file to be opened.
     */
    public static void main(String[] args)
    {
        // Get the native look and feel class name
        String nativeLF = UIManager.getSystemLookAndFeelClassName();

        // Install the look and feel
        try
        {
            UIManager.setLookAndFeel(nativeLF);
        } catch (InstantiationException ex)
        {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex)
        {
            ex.printStackTrace();
        } catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
        }
        TimeLogger model = new TimeLogger(args);
        TimeLoggerJavaUI view = new TimeLoggerJavaUI(model);
        model.addObserver(view);
    }
}
