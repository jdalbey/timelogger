package timelogger;

import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;
import javax.swing.*;

/**
 * The specialized window that shows the timer running and listens for input to
 * signal that the current phase is complete.
 *
 * @author David Wheelwright
 */
public class PSPTimerView implements Observer
{
    private PSPTimer myTimer;
    private JPanel panel;
    private JFrame counter;
    private JLabel numbers;
    private final String currentPhase;
    private String DATE_FORMAT = "MM-dd-yy HH:mm";
    private String TIME_FORMAT = "HH:mm";
    private String toVector;
    private Calendar calendar;
    private java.text.SimpleDateFormat timeFormat;

    /**
     * Construct a new PSPTimer to watch over the running timer.
     *
     * @param loggerFrame The frame for this UI to use.
     * @param currentPhase the name of the software phase being timed.
     * @param alwaysOnTop boolean value determining if timer should always be on
     * top.
     */
    public PSPTimerView(final JFrame loggerFrame, final String currentPhase, boolean alwaysOnTop)
    {
        this.currentPhase = currentPhase;

        String dateTime;
        int hour;
        String AMPM;

        numbers = new JLabel();
        numbers.setFont(new Font("TBLcd", Font.BOLD, 30));
        numbers.setForeground(Color.BLUE);
        numbers.setText("00:00:00");

        AMPM = "AM";
        toVector = "";
        panel = new JPanel();
        calendar = Calendar.getInstance(TimeZone.getDefault());
        java.text.SimpleDateFormat dateFormat =
                new java.text.SimpleDateFormat(DATE_FORMAT);
        timeFormat = new java.text.SimpleDateFormat(TIME_FORMAT);

        JLabel task = new JLabel(currentPhase);
        task.setFont(new Font("Tahoma", Font.BOLD, 20));
        panel.setLayout(new BorderLayout());
        panel.add(BorderLayout.NORTH, numbers);
        panel.add(BorderLayout.SOUTH, task);

        dateTime = timeFormat.format(calendar.getTime());
        hour = Integer.valueOf(dateTime.substring(0, 2)).intValue();

        if (hour == 12)
        {
            AMPM = "PM";
        }
        else if (hour > 12)
        {
            AMPM = "PM";
            hour = hour - 12;

            if (hour < 10)
            {
                dateTime = ("0" + Integer.toString(hour) + ":" + dateTime.substring(3, 5));
            }
            else
            {
                dateTime = (Integer.toString(hour) + ":" + dateTime.substring(3, 5));
            }
        }

        JLabel startTime = new JLabel("Started: " + dateTime + " " + AMPM + "         ");
        startTime.setFont(new Font("Tahoma", Font.PLAIN, 14));
        panel.add(BorderLayout.CENTER, startTime);

        dateFormat.setTimeZone(TimeZone.getDefault());
        toVector += timeFormat.format(calendar.getTime());



        counter = new JFrame("Timer");
        counter.getContentPane().add(BorderLayout.CENTER, panel);
        counter.toFront();
        // I don't think these work if you pack()
        //counter.setPreferredMinimumSize(new Dimension(180,120));
        //counter.setSize(180,120);
        counter.setResizable(false);
        counter.setAlwaysOnTop(alwaysOnTop);
        counter.pack();
        counter.setVisible(true);
        // If the window get clicked on, it is time to stop the timer
        counter.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                timerDone();
                loggerFrame.setVisible(true);
            }
        });
        counter.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                timerDone();
                loggerFrame.setVisible(true);
            }
        });
    }

    /*public void setAlwaysOnTop(boolean alwaysOnTop) {
     loggerFrame.setAlwaysOnTop(alwaysOnTop);
     }*/
    /**
     * Every time the PSPTimer ticks, update the timer text.
     */
    public void update(Observable o, Object arg)
    {
        PSPTimer timer = (PSPTimer) o;
        numbers.setText(timer.getTime());
        if (counter.getState() == Frame.ICONIFIED)
        {
            counter.setTitle(timer.getTime());
        }
        else
        {
            counter.setTitle("Timer");
        }
    }

    /**
     * Assigns this view a PSPTimer to watch.
     *
     * @param timer the PSPTimer to watch.
     */
    public void setModel(PSPTimer timer)
    {
        myTimer = timer;
    }

    /**
     * Calculates the time elapsed, formats the results as a String
     * representation of a Log Entry, and adds it to the log in TimeLogger.
     */
    public void timerDone()
    {
        calendar = Calendar.getInstance(TimeZone.getDefault());  // get stop time
        int delta = myTimer.getMinutes();

        // Make smallest delta 1 minute.
        if (delta == 0)
        {
            delta = 1;
        }
        toVector += (" " + timeFormat.format(calendar.getTime()) + "   " + delta
                + "    " + currentPhase.toUpperCase() + "   ");
        counter.dispose();

        myTimer.getOwner().addLogEntry(toVector);
    }
}
