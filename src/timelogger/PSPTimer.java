package timelogger;

import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Timer;

/**
 * The timer that keeps track of the elapsed time. This was originally all
 * static fields kept by his program, so I refactored it into a separate class.
 * After looking at what this class actually does, it would have been much
 * simpler to use a regular javax.swing.timer instead.
 *
 * @author David Wheelwright
 */
public class PSPTimer extends Observable
{
    private TimeLogger myOwner;
    private int hours10;
    private int hours1;
    private int minutes10;
    private int minutes1;
    private int seconds10;
    private int seconds1;
    private javax.swing.Timer timer;
    private String time;

    public PSPTimer(Observer myView)
    {
        addObserver(myView);
        hours1 = 0;
        hours10 = 0;
        minutes10 = 0;
        minutes1 = 0;
        seconds10 = 0;
        seconds1 = 0;
        timer = new Timer(998, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                seconds1++;
                if (seconds1 == 10)
                {
                    seconds1 = 0;
                    seconds10++;
                }
                if (seconds10 == 6)
                {
                    seconds10 = 0;
                    minutes1++;
                }
                if (minutes1 == 10)
                {
                    minutes1 = 0;
                    minutes10++;
                }
                if (minutes10 == 6)
                {
                    minutes10 = 0;
                    hours1++;
                }
                if (hours1 == 10)
                {
                    hours1 = 0;
                    hours10++;
                }
                if (hours10 == 10)
                {
                    hours10 = 0;
                }
                time = Integer.toString(hours10) + Integer.toString(hours1) + ":"
                        + Integer.toString(minutes10) + Integer.toString(minutes1) + ":"
                        + Integer.toString(seconds10) + Integer.toString(seconds1);
                setChanged();
                notifyObservers();
                clearChanged();
            }
        });
        timer.setInitialDelay(0);
        timer.setCoalesce(true);
        timer.start();
    }

    public String getTime()
    {
        return time;
    }

    public int getMinutes()
    {
        return (hours10 * 600) + (hours1 * 60) + (minutes10 * 10) + minutes1;
    }

    public TimeLogger getOwner()
    {
        return myOwner;
    }

    public void setOwner(TimeLogger owner)
    {
        myOwner = owner;
    }
}
