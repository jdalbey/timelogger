package timelogger;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * The data model for a TimeLogger program. Provides methods for working with
 * and altering the contents of the TimeLogger without a specific UI
 * implementation.
 *
 * The internal representation of the log entries is currently a String. It
 * would be much better if there was a "Log Entry" object with fields for each
 * of the different parts of a log entry. In order to do this, you would only
 * need to change the Vector "listData" from type String to the type you want.
 * You would also need to change the "addLogEntry" method and the timerDone()
 * method (which creates the actual entries) in class PSPTimerView accordingly.
 *
 * @author David Wheelwright
 * @author Evin Thompson
 * @author Nathaniel Lehrer
 *
 */
public class TimeLogger extends Observable
{
    /**
     * listData holds the log entries that make up the time logger.
     */
    private static Vector<String> listData = new Vector<String>();
    /**
     * The currentFile is the name of the currently opened file.
     */
    private static File currentFile = new File("Untitled.tlg");
    /**
     * The number of the current file. Used to differentiate newly created
     * files.
     */
    private static int fileNum = 1;
    /**
     * A boolean indicator of whether the file has been modified since it was
     * last saved
     */
    private boolean needsSaving = false;
    /**
     * An int that signifies exactly how many buttons there are, default 6
     */
    private int numButtons = 6;
    /**
     * A list of strings that contains all the names of the buttons
     */
    private LinkedList<String> nameList = new LinkedList<String>();

    /**
     * Construct this time logger with an optional log file to open.
     *
     * @param args the first item in the array is a filename to be opened.
     */
    public TimeLogger(String[] args)
    {
        processArgs(args);
        nameList.add("Design");
        nameList.add("Code");
        nameList.add("Compile");
        nameList.add("Test");
        nameList.add("PSP");
    }

    private void processArgs(String[] args)
    {
        if (args.length == 0)
        {
            return;
        }

        String fullname = args[0];
        File logFile;
        logFile = new File(fullname);
//        try
//{
//            logFile = new File(fullname);
//        }
//        catch (java.io.FileNotFoundException ex)
//{
//            JOptionPane.showMessageDialog(null, "alert", "Unable to open " + fullname, JOptionPane.ERROR_MESSAGE);
//        }
        doOpen(logFile);
    }

    /**
     * Returns the list of log entries for the current file.
     *
     * @return the Vector filled with the log entries for the current file.
     */
    public Vector<String> getListData()
    {
        return listData;
    }

    /**
     * Returns the name of the current file, as a String.
     *
     * @return the name of the current file.
     */
    public String getCurrentFile()
    {
        return currentFile.getName();
    }

    public String getCurrentPath()
    {
        String result = "";
        try
        {
            result = currentFile.getCanonicalPath();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Tells whether the file has been modified since it was last saved.
     *
     * @return true if the file has been modified since it was last saved, false
     * otherwise.
     */
    public boolean needsSaving()
    {
        return needsSaving;
    }

    /**
     * Adds a new log entry to the the time log.
     *
     * @param entry a formatted String representing a new log entry.
     */
    public void addLogEntry(String entry)
    {
        listData.addElement(entry);
        needsSaving = true;
        updateUI();
    }

    void deleteLogEntry(int number)
    {
        listData.remove(number);
        needsSaving = true;
        updateUI();
    }

    /**
     * Marks the log as modified. Used when UI makes direct changes
     * to entries without calling model methods.
     */
    public void markModified()
    {
        needsSaving = true;
        updateUI();
    }

    /**
     * Starts a timer for the software development phase chosen.
     *
     * @param currentPhase a String containing the name of the software phase.
     * @param view an observer that will provide the UI for the timer.
     * @return the newly created PSPTimer object.
     */
    public PSPTimer startTimer(String currentPhase, Observer view)
    {
        PSPTimer timer = new PSPTimer(view);
        timer.setOwner(this);

        return timer;
    }

    /**
     * Represents the File->New operation. It clears the current file
     * information and signals the UI that it needs to update its status.
     */
    public void doNew()
    {
        needsSaving = false;
        currentFile = new File("Untitled_" + fileNum + ".tlg");
        listData.clear();
        fileNum++;
        updateUI();
    }

    /**
     * Represents the File->Open operation. Opens the given file for reading,
     * and replaces any current log entries with new ones read from the file.
     *
     * @param fileToOpen a File object for the file to be read from.
     */
    public void doOpen(File fileToOpen)
    {
        needsSaving = false;
        currentFile = fileToOpen;

        // In case the open operation fails in some way, check for an error
        try
        {
            FileReader inputStream = new FileReader(fileToOpen.getCanonicalPath());
            BufferedReader objInputStream = new BufferedReader(inputStream);
            String line = objInputStream.readLine();
            listData.clear();

            updateUI();

            // while there are more elements, read them into the log
            while (line != null)
            {
                listData.addElement(line);
                line = objInputStream.readLine();
            }

            updateUI();

            inputStream.close();
        } catch (Exception ex)
        {
            System.err.println("Open failed\n" + ex);
            System.err.println("Probable cause: missing extension or incorrect path.");
        }
    }

    /**
     * Represents the File->Save operation. Sends the current file to the
     * writeFile method for saving. It requires the file to have been saved at
     * least once before in order to prevent files with the name "Untitled" from
     * being created.
     *
     * @pre the current file must have been saved at least once before.
     */
    public void doSave()
    {
        writeFile(currentFile);
    }

    /**
     * Represents the File->Save As... operation. Sends the File given as a
     * parameter to the writeFile method for saving.
     *
     * @param fileToSave the file that will be written to.
     * @return true if saved, false if already exists and overwrite canceled.
     */
    public boolean doSaveAs(File fileToSave)
    {
        String filename = fileToSave.getAbsolutePath();
        if (fileToSave.exists())
        {
            Date dt = new Date(fileToSave.lastModified());
            DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            String lastModified = df1.format(dt);

            int reply = JOptionPane.showConfirmDialog(null,
                    "File already exists, overwrite?\n"
                    + fileToSave.getName() + "  "
                    + lastModified);
            if (reply != JOptionPane.OK_OPTION)
            {
                return false;
            }
        }
        else
        {
            if (!filename.endsWith(".tlg"))
            {
                filename += ".tlg";
            }
        }
        fileToSave = new File(filename);
        writeFile(fileToSave);
        currentFile = fileToSave;
        return true;

    }

    /**
     * Performs the actual writing of the log entries to a file.
     *
     * @param fileToWrite the name of the file to be written to, as a String.
     */
    private void writeFile(File fileToWrite)
    {
        needsSaving = false;

        // in case the save operation fails, check for errors
        try
        {
            FileWriter outputStream = new FileWriter(fileToWrite);
            PrintWriter objOutputStream = new PrintWriter(outputStream);

            for (int itr = 0; itr < listData.size(); itr++)
            {
                objOutputStream.println((String) listData.elementAt(itr));
            }

            outputStream.close();
        } catch (Exception ex)
        {
            System.err.println("Save failed\n" + ex);
        }
        updateUI();
    }

    /**
     * Exits the program.
     */
    public void doExit()
    {
        System.exit(0);
    }

    /**
     * Clears the log of all log entries and signals the UI that it needs to
     * update its information.
     */
    public void doClearLog()
    {
        listData.clear();
        updateUI();
    }

    /**
     * Returns the total times for each of the different categories, as a
     * specifically formatted String.
     *
     * A better implementation would provide an accessor for the total for each
     * of the different phases. Then it would be left up to the UI to determine
     * how to organize this information. This implementation is straight out of
     * the previous version.
     *
     * @return a formatted String containing the total times for each of the PSP
     * software phases.
     */
    public Vector<String> getTotalTimesAsVector()
    {
        int holder = 0;
        ListIterator<String> iterate;
        String current;
        Object[] intArray;
        int place;
        //to hold the ints of times recieved
        LinkedList<Integer> intList = new LinkedList<Integer>();
        //in case the names of the buttons have been edited, so it will still display task
        LinkedList<String> names = new LinkedList<String>();
        //to tell whether or not the name was found in list names
        boolean found = false;


        int totalTime = 0;
        String checker = "";
        StringTokenizer parser;
        Vector<String> times = new Vector<String>();

        // look at each entry in the log, add its time to its category's total
        for (int itr = 0; itr < listData.size(); itr++)
        {
            found = false;
            parser = new StringTokenizer((String) listData.get(itr));
            parser.nextToken();
            parser.nextToken();
            holder = Integer.parseInt(parser.nextToken());
            totalTime += holder;
            checker = parser.nextToken();

            place = 0;
            iterate = names.listIterator(0);
            while (iterate.hasNext())
            {
                current = iterate.next();
                if (checker.equals(current))
                {
                    intList.set(place, new Integer((int) intList.get(place) + holder));
                    found = true;
                }
                place++;
            }

            if (!found)
            {
                names.add(checker);
                intList.add(new Integer(holder));
            }


        }

        place = 0;
        iterate = names.listIterator(0);
        intArray = intList.toArray();
        while (iterate.hasNext())
        {
            current = iterate.next();
            String formattedTotal = String.format("%-15s : %4d\n", current, (Integer) intArray[place]);
            times.add(formattedTotal);
            place++;
        }

        if (totalTime > -1)
        {
            times.add("TOTAL = " + totalTime + " minutes");
        }

        return times;
    }

    /**
     * Identical to getTotalTimesAsVector but returns a string with each item in
     * the vector listed on a separate line.
     *
     * @return string version of getTotalTimesAsVector's result
     */
    public String getTotalTimes()
    {
        Vector<String> times = getTotalTimesAsVector();
        String output = "";

        for (String time : times)
        {
            output += time;
        }

        return output;
    }

    /**
     * Represents the Options->Font operation. Not currently implemented.
     */
    public void doFont()
    {
    }

    /**
     * Represents the Options->Settings operation. Not currently implemented.
     */
    public void doSettings()
    {
    }

    /**
     * Represents the Help->QuickStart operation.
     *
     * @return the quick start help instructions in html.
     */
    public String getInstructions()
    {
        /**
         * The quickstart guide text in html format
         */
        String msg = "<center>TimeLogger QuickStart </center>"
                + "<p>Time Logger is cross-platform  \"stopwatch\" utility. It was written in Java.</p> "
                + "<p>Click on one of the buttons on the left to start the timer. <br>"
                + "A small timer window appears which you click when you want to stop the timer. <br>"
                + "A new entry is created in the Log display showing Start and Stop times and the <br>"
                + "duration (\"delta\"). Double click on a line in the display if you want to add a comment. </p>"
                + "<p> Sometimes you may be interrupted but forget to stop the clock.  <br>"
                + "When you return, you can stop the time and then press the \"Interrupt\" button and you can enter the <br>"
                + "duration of interrupt time that you would like deducted from the previous phase. </p> "
                + "<p> To find the total time, select Tools -&gt; Total Times. </p>"
                + "<p> There are typical File operations (Open, Save) for storing the Time Log. </p>"
                + "<p> To manually create a log entry, select Insert -&gt; Add Log Entry. </p> "
                + "<p> The Settings menu lets you modify configuration options.  <br> "
                + "The Print Setup tab has three text fields you can provide that <br> "
                + "will appear on a log printout. </p> <p> The \"Timer Display always on top\" checkbox causes the <br>"
                + "timer display to remain on top of other windows. </p>"
                + "<p> When the clock is running, you can minimize the window and the the elapsed time <br>"
                + "is shown in the task bar. <p>"
                + "LIMITATIONS: Totals accurate only for Delta up to 999. "
                + "If you are working that long without a break, <br> you're not a human, you're a machine! <br>"
                + "Time duration does  not properly wrap around midnight. </p>";

        return msg;
    }

    /**
     * Represents the Help->HelpTopics operation. Not currently implemented.
     */
    public void doHelpTopics()
    {
    }

    /**
     * Represents the Help->SupportWebSite operation. Not currently implemented.
     */
    public void doSupportWebSite()
    {
    }

    /**
     * Represents the Help-c>About TimeLogger operation. Not currently
     * implemented.
     */
    public void doAboutTimeLogger()
    {
    }

    /**
     * Signals the UI Observer that it needs to update its representation of the
     * TimeLogger.
     */
    private void updateUI()
    {
        setChanged();
        notifyObservers();
        clearChanged();
    }
}
