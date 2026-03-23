package timelogger;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.*;
import timeselector.TimeSelectionField;

/**
 * A User Interface for the TimeLogger using the Java swing library.
 *
 * The length of this implementation over the previous one is due to using
 * individual ActionListener's for every operation rather than one extremely
 * non-cohesive method to do everything. It might be improved if you can find a
 * faster or less space-consuming way to make all of the individual action
 * listeners.
 *
 * @author David Wheelwright
 * @author Evin Thompson
 * @author jdalbey
 *
 */
public class TimeLoggerJavaUI implements Observer
{
    private static final int kDeltaTimeFieldWidth = 5;
    private static final int kTaskFieldWidth = 12;
    // The following five fields represent Swing objects used to make the
    // UI window.
    private JFrame loggerFrame;
    private JList loggerList;
    private JScrollPane scrollPane;
    private JLabel columnLabel;
    private JPanel textPanel;  // contains column labels and log entries
    private JPanel buttons;
    private boolean alwaysOnTop = false;
    private TimeLoggerJavaUI TL = this;
    private String saveName = "";
    private String projName = "";
    private String currDate = "";
    private String recentFile = "";
    // The default font used to display the TimeLogger log entries
    private final Font defaultFont = new Font("Monospaced", Font.PLAIN,
            12);
    private Font currentFont = defaultFont;
    // The buttonItems contains the list of PSP phase options on the left
    // side of the TimeLogger UI.  This field might be removable.
    private Vector<String> buttonItems;
    // the UI's copy of the TimeLogger
    private TimeLogger myModel;
    // Profile management
    private String activeProfile = "Custom";
    private ProfileManager profileManager;

    /**
     * Constructs a new UI based on the given TimeLogger model.
     *
     * @param newModel the TimeLogger data for this program.
     */
    public TimeLoggerJavaUI(TimeLogger newModel)
    {
        super();


        myModel = newModel;
        loggerFrame = new JFrame("TimeLogger - " + myModel.getCurrentFile());
        loggerList = new JList(myModel.getListData());
        addListener();
        scrollPane = new JScrollPane();

        buttons = new JPanel();
        buttonItems = new Vector<String>();
        profileManager = new ProfileManager();
        // Ensure config directory exists
        ConfigManager.getConfigDirectory();
        resetDefaultButtons();
        buttons = getButtons();

        loggerFrame.setJMenuBar(getMenuBar());
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));

        columnLabel = new JLabel(
                "Start Stop    "
                + "\u0394      Task   Comment");
        columnLabel.setFont(defaultFont);
        textPanel.add(columnLabel);
        columnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = loggerFrame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        //contentPane.add(columnLabel, BorderLayout.NORTH);
        contentPane.add(buttons, BorderLayout.WEST);
        //loggerList = fillList();
        loggerList.setFont(defaultFont);
        loggerList.setVisibleRowCount(15);
        scrollPane = new JScrollPane(loggerList);
        scrollPane.setPreferredSize(new Dimension(450, 220));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(scrollPane);
        contentPane.add(textPanel, BorderLayout.CENTER);
        loggerFrame.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                JFrame tmp = (JFrame) e.getSource();
                if ((tmp.getWidth() < 200) || (tmp.getHeight() < 242))
                {
                    tmp.setSize(200, 242);
                }
            }
        });

        loggerFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        loggerFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                saveSettings();
            }
        });

        // loggerFrame.setPreferredSize(new Dimension(500,242));  // JDK1.5
        loggerFrame.setSize(500, 242); // JDK1.4
        loggerFrame.pack();
        loggerFrame.setSize(500, 325); // JDK1.4
        loggerFrame.setVisible(true);
    }

    /**
     * Creates or updates TimeLogger.cfg.
     */
    private void saveSettings()
    {
        //save settings to configuration file for next time opened
        try
        {
            FileWriter outputStream = new FileWriter(ConfigManager.getMainConfigFile());
            PrintWriter objOutputStream = new PrintWriter(outputStream);

            objOutputStream.println(saveName);
            objOutputStream.println(projName);
            objOutputStream.println(currDate);
            objOutputStream.println(myModel.getCurrentPath());
            if (alwaysOnTop)
            {
                objOutputStream.println("true");
            }
            else
            {
                objOutputStream.println("false");
            }
            objOutputStream.println(activeProfile);
            for (int itr = 0; itr < buttonItems.size(); itr++)
            {
                objOutputStream.println((String) buttonItems.get(itr));
            }
            outputStream.close();
        } catch (Exception ex)
        {
            System.err.println("Save failed\n" + ex);
        }

        if (checkNeedsSaving())
        {
            myModel.doExit();
        }
    }

    /**
     * Fills each of the major software design phase buttons into the list of
     * design phase buttons. If these buttons need to be altered or more buttons
     * need to be added, this is the place to do it.
     *
     * Also will upload the button names, number of buttons, and name
     * information from configuration file.
     */
    private void resetDefaultButtons()
    {
        File defaults = ConfigManager.getMainConfigFile();
        buttonItems.clear();
        try
        {
            FileReader inputStream = new FileReader(defaults.getCanonicalPath());
            BufferedReader objInputStream = new BufferedReader(inputStream);
            String line = objInputStream.readLine();

            // while there are more elements, read them into the log
            for (int i = 0; i < 5; i++)
            {
                if (line == null)
                {
                    System.out.println("Error: settings file Empty! reseting to default");
                    buttonItems.add("DESIGN");
                    buttonItems.add("CODE");
                    buttonItems.add("COMPILE");
                    buttonItems.add("TEST");
                    buttonItems.add("PSP");
                    buttonItems.add("REVIEW");
                    return;
                }
                else
                {
                    if (i == 0)
                    {
                        saveName = line;
                        line = objInputStream.readLine();
                    }
                    else if (i == 1)
                    {
                        projName = line;
                        line = objInputStream.readLine();
                    }
                    else if (i == 2)
                    {
                        currDate = line;
                        line = objInputStream.readLine();
                    }
                    else if (i == 3)
                    {
                        recentFile = line;
                        line = objInputStream.readLine();
                    }
                    else
                    {
                        alwaysOnTop = line.equals("true");
                        line = objInputStream.readLine();
                    }
                }
            }

            // Read line 6: activeProfile (new in version 1.28)
            if (line != null)
            {
                activeProfile = line;
                line = objInputStream.readLine();
            }
            else
            {
                activeProfile = "Custom";
            }

            // Load tasks from profile or from file (lines 7+)
            if (!activeProfile.equals("Custom") && profileManager.profileExists(activeProfile))
            {
                // Load from profile file
                Vector<String> profileTasks = profileManager.loadProfile(activeProfile);
                if (profileTasks != null)
                {
                    buttonItems.addAll(profileTasks);
                }
            }
            else
            {
                // Load from config file (backwards compatibility)
                if (line == null)
                {
                    System.out.println("Error: settings file Empty! reseting to default");
                    buttonItems.add("DESIGN");
                    buttonItems.add("CODE");
                    buttonItems.add("COMPILE");
                    buttonItems.add("TEST");
                    buttonItems.add("PSP");
                    buttonItems.add("REVIEW");
                    activeProfile = "Custom";
                    return;
                }
                while (line != null)
                {
                    buttonItems.add(line);
                    line = objInputStream.readLine();
                }
            }

            inputStream.close();
        } catch (Exception ex)
        {
            //System.err.println("Open failed\n" + ex);
            buttonItems.add("DESIGN");
            buttonItems.add("CODE");
            buttonItems.add("COMPILE");
            buttonItems.add("TEST");
            buttonItems.add("PSP");
            buttonItems.add("REVIEW");
            activeProfile = "Custom";
        }
    }
    /*
     *  creates the JList that is used to fill loggerList
     *  adding mouse click listeners to the list to add comments.
     *
     *  @return the JList created
     */

    public void addListener()
    {
        loggerList.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent ev)
            {
                // Is it a right mouse click?
                if (SwingUtilities.isRightMouseButton(ev))
                {
                    int row = loggerList.locationToIndex(new Point(ev.getX(), ev.getY()));
                    int reply = JOptionPane.showConfirmDialog(loggerFrame,
                            "Delete this item?\n"
                            + loggerList.getModel().getElementAt(row).toString() + "  ",
                            "Delete log entry", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.OK_OPTION)
                    {
                        myModel.deleteLogEntry(row);
                    }
                }
                // handle double-click on a log entry (modify comment)
                if (ev.getClickCount() == 2)
                {
                    int selected = loggerList.getSelectedIndex();
                    StringTokenizer parser;
                    String entry = "";
                    Vector<String> vect = myModel.getListData();
                    String temp1;
                    String temp2 = "";
                    String deltaTime;
                    parser = new StringTokenizer(loggerList.getModel().getElementAt(selected).toString());
                    entry += parser.nextToken() + " ";
                    entry += parser.nextToken() + "  ";
                    deltaTime = parser.nextToken();
                    // Do we need to add a space for non-negative times?
                    if (deltaTime.charAt(0) != '-')
                    {
                        entry += " ";
                    }
                    entry += padr(deltaTime.substring(0), kDeltaTimeFieldWidth);
                    entry += padr(parser.nextToken(), kTaskFieldWidth);
                    // get existing comment words
                    while (parser.hasMoreTokens())
                    {
                        temp2 += parser.nextToken() + " ";
                    }
                    temp1 = (String) JOptionPane.showInputDialog(loggerFrame, "Comment.", "Add Comment.",
                            JOptionPane.PLAIN_MESSAGE, null, null, temp2);
                    if (temp1 != null)
                    {
                        entry += temp1;
                    }
                    else
                    {
                        entry += temp2;
                    }

                    vect.set(selected, entry);
                    myModel.markModified();
                    //logglist.setListData(vect);
                }
            }
        });

    }

    /**
     * Refreshes the UI's representation of TimeLogger every time the TimeLogger
     * signals that it has changed. Implements the Observer interface.
     *
     * @param o the Timelogger, not used here since we already have a copy.
     * @param arg any extra arguments, not used.
     *
     */
    public void update(Observable o, Object arg)
    {
        updateWindowTitle();

        //loggerList.setFixedCellWidth(450);

        // TODO: setListData() shouldn't be necessary if JList model is
        // handled properly.
        loggerList.setListData(myModel.getListData());
        // force scroll bar to bottom
        int lastIndex = loggerList.getModel().getSize() - 1;
        loggerList.setSelectedIndex(lastIndex);
        if (lastIndex >= 0)
        {
            loggerList.ensureIndexIsVisible(lastIndex);
        }
        loggerFrame.setVisible(true);
    }

    /**
     * Creates and returns the entire menu bar used by the UI.
     *
     * @return a JMenuBar containing the menu bar for the TimeLoggerUI.
     */
    public JMenuBar getMenuBar()
    {
        JMenu fileMenu = new JMenu();
        JMenu helpMenu = new JMenu();
        JMenu optionsMenu = new JMenu();
        JMenuBar loggerMenu = new JMenuBar();
        fileMenu = new JMenu("File");
        JMenu insertMenu = new JMenu("Insert");
        JMenu toolsMenu = new JMenu("Tools");
        optionsMenu = new JMenu("Options");
        helpMenu = new JMenu("Help");
        fileMenu = fillFileMenu(fileMenu);
        insertMenu = fillInsertMenu(insertMenu);
        toolsMenu = fillToolsMenu(toolsMenu);
        optionsMenu = fillOptionsMenu(optionsMenu);
        helpMenu = fillHelpMenu(helpMenu);
        loggerMenu.add(fileMenu);
        loggerMenu.add(insertMenu);
        loggerMenu.add(toolsMenu);
        loggerMenu.add(optionsMenu);
        loggerMenu.add(helpMenu);
        return loggerMenu;
    }

    /**
     * Returns the filled-in File Menu for the TimeLoggerUI's menu bar.
     *
     * @param fileMenu the JMenu to be filled in.
     * @return the filled File menu.
     */
    private JMenu fillFileMenu(JMenu fileMenu)
    {
        JMenuItem item;
        item = new JMenuItem("New");
        item.setAccelerator(KeyStroke.getKeyStroke('N', Event.CTRL_MASK, false));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (checkNeedsSaving())
                {
                    myModel.doNew();
                }
            }
        });

        fileMenu.add(item);
        item = new JMenuItem("Open...");
        item.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK, false));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (checkNeedsSaving())
                {
                    JFileChooser chooser = new JFileChooser(".");
                    int approved = chooser.showOpenDialog(chooser);
                    if (approved == JFileChooser.APPROVE_OPTION)
                    {
                        updateWindowTitle();

                        myModel.doOpen(chooser.getSelectedFile());
                    }
                }
            }
        });
        fileMenu.add(item);
        // Create a submenu of the most recently opened document
        // Note: resetDefaultButtons() must have been called before this,
        // so that the variable 'recentFile' has a value.
        JMenu recentMenu = new JMenu("Recent");
        item = new JMenuItem(recentFile);
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                myModel.doOpen(new File(recentFile));
            }
        });
        if (recentFile.length() > 0)
        {
            recentMenu.add(item);
        }
        fileMenu.add(recentMenu);

        item = new JMenuItem("Save");
        item.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK, false));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (myModel.getCurrentFile().startsWith("Untitled"))
                {
                    showSaveDialog();
                    updateWindowTitle();
                }
                else
                {
                    myModel.doSave();
                }
            }
        });

        fileMenu.add(item);
        item = new JMenuItem("Save As...");
        item.setAccelerator(KeyStroke.getKeyStroke('S', 3, false));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                showSaveDialog();
                updateWindowTitle();
            }
        });

        fileMenu.add(item);
        item = new JMenuItem("Export ");
        item.setAccelerator(KeyStroke.getKeyStroke('E', Event.CTRL_MASK, false));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // make a defensive copy of list data
                Vector<String> vect = new Vector(myModel.getListData());

                int index;
                int reference;
                int count = 0;
                vect.add(0, saveName);
                vect.add(1, projName);
                vect.add(2, currDate);
                vect.add(3, " ");
                vect.add(" ");

                JFileChooser chooser = new JFileChooser(".");
                chooser.setFileFilter(new TXTFileFilter());
                int choice = chooser.showDialog(null, "Export");
                if (choice == JFileChooser.APPROVE_OPTION)
                {
                    try
                    {
                        FileWriter fw = new FileWriter(chooser.getSelectedFile());
                        //
                        for (String item : vect)
                        {
                            fw.write(item + "\n");
                        }
                        fw.write(myModel.getTotalTimes());
                        fw.close();
                    } catch (java.io.IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        });

        fileMenu.add(item);
        item = new JMenuItem("Print");
        item.setAccelerator(KeyStroke.getKeyStroke('P', Event.CTRL_MASK, false));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // make a defensive copy of list data
                Vector<String> vect = new Vector(myModel.getListData());
                Vector<String> totalTimes = myModel.getTotalTimesAsVector();
                int index;
                int reference;
                int count = 0;
                vect.add(0, saveName);
                vect.add(1, projName);
                vect.add(2, currDate);
                for (String time : totalTimes)
                {
                    vect.add(time);
                }
                loggerList.setListData(vect);
                JList totalList = new JList(vect);
                //int size = vect.size();
                PrintUtilities.printComponent(loggerList);
                //PrintUtilities.printComponent(totalList);
                vect.remove(saveName);
                vect.remove(projName);
                vect.remove(currDate);
                for (String time : totalTimes)
                {
                    vect.remove(time);
                }
                loggerList.setListData(vect);
            }
        });

        fileMenu.add(item);
        item = new JMenuItem("Exit");
        item.setAccelerator(KeyStroke.getKeyStroke('X', Event.CTRL_MASK, false));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                saveSettings();
            }
        });

        fileMenu.add(item);
        fileMenu.insertSeparator(4);
        fileMenu.insertSeparator(6);
        return fileMenu;
    }

    /**
     * Fills in and returns the Insert Menu for the TimeLoggerUI.
     *
     * @param insertMenu the JMenu to be filled in.
     * @return the filled-in JMenu.
     */
    private JMenu fillInsertMenu(JMenu insertMenu)
    {
        JMenuItem item;
        item = new JMenuItem("Add Log Entry...");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //  create and show UI for the adding entry dialog
                final AddEntry entry = new AddEntry(loggerFrame);
                javax.swing.SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        entry.createAndShowGUI();
                    }
                });

            }
        });
        insertMenu.add(item);

        return insertMenu;
    }

    /**
     * Fills in and returns the Tools menu for the TimeLoggerUI.
     *
     * @param toolsMenu the JMenu to be filled in.
     * @return the filled-in JMenu.
     */
    private JMenu fillToolsMenu(JMenu toolsMenu)
    {
        JMenuItem item;
        item = new JMenuItem("Total Times");
        item.setAccelerator(KeyStroke.getKeyStroke('T', Event.CTRL_MASK, false));
        item.addActionListener(new ActionListener()
        {
            // display total times
            public void actionPerformed(ActionEvent e)
            {
                // Save the current optionpane font
                Font original = UIManager.getFont("OptionPane.font");
                // Set font to monospaced for totals display
                UIManager.put(
                        "OptionPane.messageFont",
                        new FontUIResource(new Font("Monospaced", Font.PLAIN, 12)));

                JOptionPane.showMessageDialog(loggerFrame,
                        myModel.getTotalTimes(), "Task Times",
                        JOptionPane.DEFAULT_OPTION);

                // restore original font
                UIManager.put("OptionPane.messageFont", new FontUIResource(original));
            }
        });
        toolsMenu.add(item);
        return toolsMenu;
    }

    /**
     * Fills in and returns the Options Menu for the TimeLoggerUI.
     *
     * @param optionsMenu the JMenu to be filled in.
     * @return the filled-in JMenu.
     */
    private JMenu fillOptionsMenu(JMenu optionsMenu)
    {
        JMenuItem item;
        item = new JMenuItem("Font");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Font font = currentFont;
                JFontChooser chooser = new JFontChooser(font);
                int result = chooser.showDialog(loggerFrame, "Choose a font...");
                if (result == JFontChooser.ACCEPT_OPTION)
                {
                    font = chooser.getSelectedFont();
                }
                //FontChooser chooser = new FontChooser(loggerFrame);
                //chooser.setVisible(true);
                //font = chooser.getNewFont();
                loggerList.setFont(font);
                columnLabel.setFont(font);
                currentFont = font;
            }
        });

        optionsMenu.add(item);
        item = new JMenuItem("Settings");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                //  create and show the UI for the settings window.
                final Settings sett = new Settings(loggerFrame);
                javax.swing.SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        sett.createAndShowGUI();
                    }
                });

                myModel.doSettings();
            }
        });

        optionsMenu.add(item);
        item = new JMenuItem("Restore Defaults");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                alwaysOnTop = false;
                resetDefaultButtons();
                loggerList.setFont(defaultFont);
                columnLabel.setFont(defaultFont);
                currentFont = defaultFont;
                loggerFrame.remove(buttons);
                buttons = getButtons();
                loggerFrame.add(buttons, BorderLayout.WEST);
                ((JComponent) loggerFrame.getContentPane()).revalidate();
            }
        });

        optionsMenu.add(item);
        return optionsMenu;
    }

    /**
     * Fills in and returns the Help Menu for the TimeLoggerUI.
     *
     * @param helpMenu the JMenu to be filled in.
     * @return the filled-in JMenu.
     */
    private JMenu fillHelpMenu(JMenu helpMenu)
    {
        JMenuItem item;

        item = new JMenuItem("Quick Start");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(loggerFrame, "<html>" + myModel.getInstructions(),
                        "Quick Start", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        helpMenu.add(item);

        item = new JMenuItem("About TimeLogger");
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(loggerFrame,
                        "TimeLogger Version " + getAppVersion() + "\n"
                        + "revision: " + getAppRevision() + "\n"
                        + "build:    " + getAppBuild() + "\n"
                        + "http://wiki.csc.calpoly.edu/TimeLogger/",
                        "TimeLogger", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(item);

        return helpMenu;
    }

    /**
     * Read the version number from the application properties. The file
     * 'application.properties' is generated by build.xml.
     *
     * @return a version string obtained from application.properties file, or
     * "Version: unknown" if an IOerror prevents us from reading the file.
     */
    private String getAppVersion()
    {
        String propfilename = "/timelogger/application.properties";
        String propKey = "Application.version";
        String version = "unknown";
        try
        {
            Properties props = new Properties();
            props.load(this.getClass().getResourceAsStream(propfilename));
            version = (String) props.get(propKey);
        } catch (IOException ex)
        {
            Logger.getLogger("timelogger").log(Level.SEVERE,
                    "getAppVersion()",
                    "IOError reading " + propfilename);
        }
        return version;
    }

    /**
     * Read the version number from the application properties. The file
     * 'application.properties' is generated by build.xml.
     *
     * @return a version string obtained from application.properties file, or
     * "Version: unknown" if an IOerror prevents us from reading the file.
     */
    private String getAppRevision()
    {
        String propfilename = "/timelogger/application.properties";
        String propKey = "Application.revision";
        String version = "unknown";
        try
        {
            Properties props = new Properties();
            props.load(this.getClass().getResourceAsStream(propfilename));
            version = (String) props.get(propKey);
        } catch (IOException ex)
        {
            Logger.getLogger("timelogger").log(Level.SEVERE,
                    "getAppVersion()",
                    "IOError reading " + propfilename);
        }
        return "revision: " + version;
    }
    /**
     * Read the build number from the application properties. The file
     * 'application.properties' is generated by build.xml.
     *
     * @return a build string obtained from application.properties file, or
     * "0" if an IOerror prevents us from reading the file.
     */
    private String getAppBuild()
    {
        String propfilename = "/timelogger/application.properties";
        String propKey = "Application.buildnumber";
        String build = "0";
        try
        {
            Properties props = new Properties();
            props.load(this.getClass().getResourceAsStream(propfilename));
            build = (String) props.get(propKey);
        } catch (IOException ex)
        {
            Logger.getLogger("timelogger").log(Level.SEVERE,
                    "getAppBuild()",
                    "IOError reading " + propfilename);
        }
        return build;
    }

    /**
     * Creates the list of design phase buttons plus the interrupt button.
     *
     * This function is largely unchanged from the previous version. I modified
     * the six design buttons to start up the timer window, and the interrupt
     * button to bring up its own window.
     *
     * @return a JPanel containing all of the design phase buttons.
     */
    public JPanel getButtons()
    {
        JPanel pane = new JPanel(new GridBagLayout());
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        pane.setLayout(gridBag);
        Dimension size = new Dimension(95, 25);
        JButton button;

        // for each of the major software process phase buttons,
        // make a button that will start a timer for that process
        for (int itr = 0; itr < buttonItems.size(); itr++)
        {
            // this variable must be created locally and redefined each time,
            // because each ActionListener requires its own final String.
            final String buttonName = buttonItems.elementAt(itr);
            button = new JButton(buttonName);
            constraints.gridx = 0;
            constraints.gridy = itr + 1;
            button.setPreferredSize(size);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // hide the main window while the timer runs
                    loggerFrame.setVisible(false);
                    // create the window to watch the timer
                    PSPTimerView view = new PSPTimerView(loggerFrame,
                            buttonName, alwaysOnTop);
                    // tell the model to start a timer for the button's phase
                    PSPTimer timer = myModel.startTimer(buttonName, view);
                    timer.addObserver(view);
                    view.setModel(timer);
                }
            });
            gridBag.setConstraints(button, constraints);
            pane.add(button);
        }
        button = new JButton("Interrupt");
        constraints.gridx = 0;
        constraints.gridy = 10;
        constraints.anchor = GridBagConstraints.SOUTHWEST;
        button.setPreferredSize(size);
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // bring up the interrupt dialog
                showInterruptDialog();
            }
        });
        gridBag.setConstraints(button, constraints);
        pane.add(button);
        return pane;
    }

    /**
     * Shows the dialog for choosing a file to save the TimeLogger data to.
     *
     * @return true if save succeeded, false if user canceled.
     */
    private boolean showSaveDialog()
    {
        boolean saved = false;
        JFileChooser chooser = new JFileChooser(".");
        int choice = chooser.showSaveDialog(chooser);
        if (choice == JFileChooser.APPROVE_OPTION)
        {
            saved = myModel.doSaveAs(chooser.getSelectedFile());
        }
        return choice == JFileChooser.APPROVE_OPTION && saved;
    }

    /* File .TXT files */
    private class TXTFileFilter extends javax.swing.filechooser.FileFilter
    {
        private final String okFileExtension = ".txt";

        public boolean accept(File file)
        {
            return (file.getName().toLowerCase().endsWith(okFileExtension));
        }

        public String getDescription()
        {
            return ".TXT";
        }
    }

    /**
     * Looks at whether the current file needs saving. If the file doesn't need
     * saving, it returns true immediately. If the file needs saving, it will
     * show a dialog that will ask the user if they want to save the file before
     * continuing. It returns true if the user chooses to continue without
     * saving or if the user chooses to continue with saving, or false if the
     * user does not want to continue.
     *
     * @return true if the file doesn't need saving or the user chose to
     * continue without saving or the user chose to continue with saving, or
     * false if the user chose not to continue.
     */
    private boolean checkNeedsSaving()
    {
        if (myModel.needsSaving())
        {
            Object[] options =
            {
                "Yes", "No", "Cancel"
            };
            int result = JOptionPane.showOptionDialog(loggerFrame,
                    "Log has been modified, do you want to save changes?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]);

            // Did user select "Yes"?
            if (result == 0)
            {
                if (myModel.getCurrentFile().startsWith("Untitled"))
                {
                    return showSaveDialog();
                }
                else
                {
                    myModel.doSave();
                    return true;
                }
            }
            // Did the user select "No"?
            else if (result == 1)
            {
                return true;
            }
            // Did the user select "Cancel" or close the window?
            else // if (result == 2 || result == JOptionPane.CLOSED_OPTION)
            {
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    /**
     * Updates the window title to reflect the current file and dirty state.
     * Format: "TimeLogger - filename*" (asterisk shown if unsaved changes exist)
     */
    private void updateWindowTitle()
    {
        String filename = myModel.getCurrentFile();
        String asterisk = myModel.needsSaving() ? "*" : "";
        loggerFrame.setTitle("TimeLogger - " + filename + asterisk);
    }

    /**
     * Brings up the Interrupt prompt. This method is largely unchanged from the
     * previous version. I added some error checks to prevent it from popping
     * Exceptions when the log table is empty or invalid input is entered for
     * interrupt time.
     */
    private void showInterruptDialog()
    {
        Vector<String> listData = myModel.getListData();

        if (listData.isEmpty())
        {
            JOptionPane.showMessageDialog(loggerFrame,
                    "Invalid Command: Can not add interrupt "
                    + "information when there are no log entries.", "Error",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }
        StringTokenizer strToken = new StringTokenizer((String) listData.lastElement());
        strToken.nextToken();
        strToken.nextToken();
        int prevInt = Integer.parseInt(strToken.nextToken());
        JOptionPane inputPane = new JOptionPane();
        String userInt = "";
        int parsedInt;
        userInt = inputPane.showInputDialog(loggerFrame,
                "How long were you interrupted?", "Interrupt Dialog",
                JOptionPane.OK_CANCEL_OPTION);
        try
        {
            parsedInt = Integer.parseInt(userInt);
            if (parsedInt > prevInt)
            {
                JOptionPane.showMessageDialog(loggerFrame,
                        "Invalid Input: Interruption time can "
                        + "not be longer than the\ntime spent during the previous "
                        + "task. Input Ignored.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                myModel.addLogEntry("00:00 00:00  -" + padr("" + parsedInt, kDeltaTimeFieldWidth)
                        + padr("Interrupt", kTaskFieldWidth));
            }
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(loggerFrame,
                    "Invalid Input: Interruption time must "
                    + "be a valid integer.  Input Ignored.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    /*
     *   Creates the GUI for the Add Entry dialog
     */
    public class AddEntry extends JPanel
    {
        //JLabel label = new JLabel("Settings");
        JFrame frame;
        // Adjusted labels to fix defect #14
        JLabel startText = new JLabel("Start:");
        JLabel stopText = new JLabel("Stop:");
        JLabel taskText = new JLabel("Task:");
        DateFormat timeFormat;
        NumberFormat numFormat;
        JFormattedTextField inputStart;
        JFormattedTextField inputStop;
        TimeSelectionField startTime;
        TimeSelectionField stopTime;
        JComboBox taskCombo = new JComboBox(buttonItems.toArray());

        /**
         * Creates the GUI shown inside the frame's content pane.
         */
        public AddEntry(JFrame frame)
        {
            super(new BorderLayout());
            this.frame = frame;

            numFormat = NumberFormat.getNumberInstance();
            numFormat.setMaximumIntegerDigits(4);
            numFormat.setGroupingUsed(false);

            timeFormat = DateFormat.getTimeInstance();
            //timeFormat.setNumberFormat(numFormat);

            startTime = new TimeSelectionField();
            stopTime = new TimeSelectionField();

            JPanel box = new JPanel();
            JPanel input = new JPanel();
            JPanel text = new JPanel();

            JButton OkButton = new JButton("OK");
            JButton CancelButton = new JButton("Cancel");

            OkButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {

                    int parsedInt1;
                    int parsedInt2;
                    int startParsedMin;
                    int stopParsedMin;
                    int delta;
                    int timeSpent = 0;

                    String s = startTime.getText();
                    String p = stopTime.getText();
                    String t = (String) taskCombo.getSelectedItem().toString();

                    s = s.replaceFirst(":", "");
                    p = p.replaceFirst(":", "");
                    s = s.trim();
                    p = p.trim();
                    // Modified to use TimeUtils 4/29/2010 by JD
                    delta = TimeUtils.getDelta(s, p);
                    if (delta > 0)
                    {
                        // Since delta was computed successfully we know the time strings
                        // must be valid 4 characters.
                        // Insert the colon back in HH:MM so it looks pretty
                        String pretty_start = s.substring(0, 2) + ":" + s.substring(2, 4);
                        String pretty_stop = p.substring(0, 2) + ":" + p.substring(2, 4);
                        myModel.addLogEntry(pretty_start + " " + pretty_stop + "   " + padr(""
                                + delta, kDeltaTimeFieldWidth) + padr(t.toUpperCase(), kTaskFieldWidth));
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(loggerFrame,
                                "Invalid Input: Entered time must "
                                + "be a valid time (0000-2359).  Input Ignored.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    exitAddEntry();
                    return;
                }
            });
            CancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    exitAddEntry();
                    return;
                }
            });


            box.setLayout(new BoxLayout(box, BoxLayout.LINE_AXIS));
            box.add(Box.createHorizontalGlue());
            box.add(OkButton);
            box.add(CancelButton);
            box.setAlignmentX(LEFT_ALIGNMENT);

            text.setLayout(new BoxLayout(text, BoxLayout.X_AXIS));
            text.add(startText);
            text.add(stopText);
            text.add(taskText);
            text.setAlignmentX(LEFT_ALIGNMENT);

//            input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
            input.setLayout(new FlowLayout());
            input.add(startText);
            input.add(startTime);
            input.add(stopText);
            input.add(stopTime);
            input.add(taskText);
            input.add(taskCombo);
            input.setAlignmentX(LEFT_ALIGNMENT);

            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            JLabel notes = new JLabel("Manually create log entry.");
            notes.setAlignmentX(LEFT_ALIGNMENT);
            add(notes);
            //add(text);
            add(input);
            add(box);
            box.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 60));
        }

        //creates the MaskFormatter used for the input times
        private MaskFormatter createFormat(String s)
        {
            MaskFormatter formatter = null;
            try
            {
                formatter = new MaskFormatter(s);
            } catch (java.text.ParseException exc)
            {
                System.err.println("formatter is bad: " + exc.getMessage());
                System.exit(-1);
            }
            return formatter;


        }

        private void createAndShowGUI()
        {
            //Create and set up the window.
            JFrame frame = new JFrame("AddEntry");


            //Create and set up the content pane.
            AddEntry newContentPane = new AddEntry(frame);
            newContentPane.setOpaque(true); //content panes must be opaque
            frame.setContentPane(newContentPane);

            //Display the window.
            frame.pack();
            frame.setVisible(true);
        }

        private void exitAddEntry()
        {
            this.frame.dispose();
        }

        private void updateFrame()
        {
            //loggerFrame.setAlwaysOnTop(alwaysOnTop);
            loggerFrame.remove(buttons);
            buttons = getButtons();
            loggerFrame.add(buttons, BorderLayout.WEST);
            ((JComponent) loggerFrame.getContentPane()).revalidate();
        }
    }

    /**
     * Pads right-hand-side of a string with spaces. If totalWidth is less than
     * or equal to the length of str, the str is returned. Otherwise, toalWidth
     * - str.length number of spaces are appended to str and the result is
     * returned. Note: StringBuilder is not used so this method should not be
     * used when efficiency is required.
     *
     * @param str the string to format
     * @param totalWidth the total width desired for the resulting string
     * @return the original string with additional spaces on appended to his
     * right-hand-side
     */
    public static String padr(String str, int totalWidth)
    {
        if (totalWidth <= str.length())
        {
            return str;
        }

        int numSpacesToBeAdded = totalWidth - str.length();
        int numSpacesLeft = numSpacesToBeAdded;
        String formattedString = str;

        while (numSpacesLeft > 0)
        {
            formattedString += " ";
            numSpacesLeft--;
        }

        return formattedString;
    }

    /*
     *  creates the GUI for the settings frame.
     */
    public class Settings extends JPanel
    {
        JFrame frame;
        JTextField nameText = new JTextField(saveName);
        JTextField projText = new JTextField(projName);
//      JFormattedTextField dateText = new JFormattedTextField(createFormat("## ## ##"));  removed by JD
        JTextField dateText = new JTextField();
        JTextField[] text = new JTextField[9];
        JCheckBox timerDisp = new JCheckBox("Timer Display always on top.");
        JComboBox<String> profileCombo;
        JButton saveProfileBtn;
        JButton deleteProfileBtn;

        /**
         * Creates the GUI shown inside the frame's content pane.
         */
        public Settings(JFrame frame)
        {
            super(new BorderLayout());
            this.frame = frame;
            dateText.setText(currDate);

            timerDisp.setSelected(alwaysOnTop);

            JPanel box = new JPanel();

            for (int i = 0; i < 9; i++)
            {
                text[i] = new JTextField();
                text[i].setPreferredSize(new Dimension(120, 28));
            }



            JButton OkButton = new JButton("OK");
            JButton CancelButton = new JButton("Cancel");
            JButton ApplyButton = new JButton("Apply");

            //creates OK button's action.
            OkButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {

                    buttonItems.clear();
                    for (int i = 0; i < 9; i++)
                    {
                        if (text[i].getText() != null)
                        {
                            if (!text[i].getText().equals(""))
                            {
                                buttonItems.add(text[i].getText().trim().toUpperCase());
                            }
                        }

                    }
                    saveName = nameText.getText();
                    projName = projText.getText();
                    currDate = dateText.getText();
                    alwaysOnTop = timerDisp.isSelected();
                    activeProfile = (String) profileCombo.getSelectedItem();
                    updateFrame();
                    exitSettings();
                    return;
                }
            });

            //creates cancel button's action.
            CancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    exitSettings();
                    return;
                }
            });

            //creates apply button's action.
            ApplyButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {

                    buttonItems.clear();
                    for (int i = 0; i < 9; i++)
                    {
                        if (text[i].getText() != null)
                        {
                            if (!text[i].getText().equals(""))
                            {
                                buttonItems.add(text[i].getText().trim().toUpperCase());
                            }
                        }

                    }
                    saveName = nameText.getText();
                    projName = projText.getText();
                    currDate = dateText.getText();
                    alwaysOnTop = timerDisp.isSelected();
                    activeProfile = (String) profileCombo.getSelectedItem();
                    updateFrame();
                    return;
                }
            });

            //Create the components.
            JPanel PrintPanel = PrintPane();
            JPanel TaskPanel = TaskPane();

            //Lay them out.
            Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            PrintPanel.setBorder(padding);
            TaskPanel.setBorder(padding);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Print Setup", null,
                    PrintPanel,
                    null); //tooltip text
            tabbedPane.addTab("Task Names", null,
                    TaskPanel,
                    null); //tooltip text

            box.setLayout(new BoxLayout(box, BoxLayout.LINE_AXIS));
            box.add(OkButton);
            box.add(CancelButton);
            box.add(ApplyButton);

            add(tabbedPane, BorderLayout.CENTER);
            add(box, BorderLayout.PAGE_END);
            box.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }

        private void createAndShowGUI()
        {
            //Create and set up the window.
            JFrame frame = new JFrame("Settings");


            //Create and set up the content pane.
            Settings newContentPane = new Settings(frame);
            newContentPane.setOpaque(true); //content panes must be opaque
            frame.setContentPane(newContentPane);

            //Display the window.
            frame.pack();
            frame.setVisible(true);
        }


        /*  creates the MaskFormatter used for the input times
         *  @param s is the string determining masking for the date
         */
        private MaskFormatter createFormat(String s)
        {
            MaskFormatter formatter = null;
            try
            {
                formatter = new MaskFormatter(s);
            } catch (java.text.ParseException exc)
            {
                System.err.println("formatter is bad: " + exc.getMessage());
                System.exit(-1);
            }
            return formatter;


        }

        //  creates the pane that contains the printing information.
        private JPanel PrintPane()
        {
            nameText.setPreferredSize(new Dimension(75, 25));
            projText.setPreferredSize(new Dimension(75, 25));
            dateText.setPreferredSize(new Dimension(75, 25));


            JPanel box = new JPanel();
            JLabel label = new JLabel("Page Title");
            JLabel labelName = new JLabel("   Name:");
            JLabel labelProj = new JLabel("   Project");
            JLabel labelDate = new JLabel("   Date");

            box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
            box.add(labelName);
            box.add(nameText);
            box.add(labelProj);
            box.add(projText);
            box.add(labelDate);
            box.add(dateText);
            box.add(timerDisp);
            box.setBorder(BorderFactory.createEmptyBorder(10, 10, 75, 10));

            JPanel pane = new JPanel(new BorderLayout());
            pane.add(label, BorderLayout.PAGE_START);
            pane.add(box, BorderLayout.WEST);
            return pane;
        }

        //  creates the pane that contains the button altering information.
        private JPanel TaskPane()
        {
            // Add profile selector at top
            JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            profilePanel.add(new JLabel("Profile:"));

            profileCombo = new JComboBox<>();
            profileCombo.addItem("Custom");
            List<String> profiles = profileManager.listProfiles();
            for (String profile : profiles)
            {
                profileCombo.addItem(profile);
            }
            profileCombo.setSelectedItem(activeProfile);
            profileCombo.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    loadSelectedProfile();
                }
            });
            profilePanel.add(profileCombo);

            saveProfileBtn = new JButton("Save");
            saveProfileBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    showSaveProfileDialog();
                }
            });
            profilePanel.add(saveProfileBtn);

            deleteProfileBtn = new JButton("Delete");
            deleteProfileBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    deleteCurrentProfile();
                }
            });
            profilePanel.add(deleteProfileBtn);

            // Existing task name fields
            Iterator<String> itr = buttonItems.iterator();
            int i;

            JPanel box = new JPanel();
            JLabel label = new JLabel("Task Names");

            box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));

            for (i = 0; i < 9; i++)
            {
                box.add(text[i]);
            }
            i = 0;

            while (itr.hasNext())
            {
                text[i].setText((String) itr.next());
                i++;
            }

            JPanel pane = new JPanel(new BorderLayout());
            pane.add(profilePanel, BorderLayout.PAGE_START);
            pane.add(label, BorderLayout.NORTH);
            pane.add(box, BorderLayout.WEST);
            return pane;
        }

        private void exitSettings()
        {
            this.frame.dispose();
        }

        /**
         * Loads the selected profile's task names into the text fields.
         */
        private void loadSelectedProfile()
        {
            String selected = (String) profileCombo.getSelectedItem();
            if (selected.equals("Custom"))
            {
                return;
            }

            Vector<String> tasks = profileManager.loadProfile(selected);
            if (tasks == null)
            {
                return;
            }

            for (int i = 0; i < 9; i++)
            {
                if (i < tasks.size())
                {
                    text[i].setText(tasks.get(i));
                }
                else
                {
                    text[i].setText("");
                }
            }
            activeProfile = selected;
        }

        /**
         * Shows a dialog to save the current task configuration as a profile.
         */
        private void showSaveProfileDialog()
        {
            String currentProfile = (String) profileCombo.getSelectedItem();

            // Check if we're updating an existing profile
            if (!currentProfile.equals("Custom") && profileManager.profileExists(currentProfile))
            {
                Object[] options = {"Update", "Save As New", "Cancel"};
                int result = JOptionPane.showOptionDialog(frame,
                        "Update profile '" + currentProfile + "' or save as new?",
                        "Save Profile",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (result == 0)
                {
                    // Update existing profile
                    Vector<String> tasks = new Vector<>();
                    for (int i = 0; i < 9; i++)
                    {
                        if (!text[i].getText().trim().isEmpty())
                        {
                            tasks.add(text[i].getText().trim().toUpperCase());
                        }
                    }
                    profileManager.saveProfile(currentProfile, tasks);
                    activeProfile = currentProfile;
                    return;
                }
                else if (result == 2 || result == JOptionPane.CLOSED_OPTION)
                {
                    // Cancel
                    return;
                }
                // Otherwise fall through to "Save As New"
            }

            // Prompt for new profile name
            String name = JOptionPane.showInputDialog(frame,
                    "Enter profile name:", "Save Profile",
                    JOptionPane.PLAIN_MESSAGE);

            if (name != null && !name.trim().isEmpty())
            {
                name = name.trim();

                // Check if profile already exists
                if (profileManager.profileExists(name))
                {
                    JOptionPane.showMessageDialog(frame,
                            "Profile '" + name + "' already exists.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Vector<String> tasks = new Vector<>();
                for (int i = 0; i < 9; i++)
                {
                    if (!text[i].getText().trim().isEmpty())
                    {
                        tasks.add(text[i].getText().trim().toUpperCase());
                    }
                }
                profileManager.saveProfile(name, tasks);
                profileCombo.addItem(name);
                profileCombo.setSelectedItem(name);
                activeProfile = name;
            }
        }

        /**
         * Deletes the currently selected profile after confirmation.
         */
        private void deleteCurrentProfile()
        {
            String selected = (String) profileCombo.getSelectedItem();
            if (selected.equals("Custom"))
            {
                JOptionPane.showMessageDialog(frame,
                        "Cannot delete Custom profile.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int result = JOptionPane.showConfirmDialog(frame,
                    "Delete profile '" + selected + "'?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION)
            {
                profileManager.deleteProfile(selected);
                profileCombo.removeItem(selected);
                profileCombo.setSelectedItem("Custom");
                activeProfile = "Custom";
            }
        }

        private void updateFrame()
        {
            loggerFrame.remove(buttons);
            buttons = getButtons();
            loggerFrame.add(buttons, BorderLayout.WEST);
            ((JComponent) loggerFrame.getContentPane()).revalidate();
        }
    }
}
