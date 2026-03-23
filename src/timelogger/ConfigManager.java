package timelogger;

import java.io.*;

/**
 * Manages configuration file locations for TimeLogger.
 * Implements XDG Base Directory specification for Linux.
 *
 * Config files are stored in: ~/.config/TimeLogger/
 * - timelogger.cfg (main settings)
 * - profiles.cfg (task profiles)
 *
 * @author Claude Code
 */
public class ConfigManager
{
    private static final String CONFIG_DIR_NAME = "TimeLogger";
    private static final String MAIN_CONFIG_FILE = "timelogger.cfg";
    private static final String PROFILES_CONFIG_FILE = "profiles.cfg";

    private static File configDirectory = null;

    /**
     * Returns the configuration directory path.
     * Creates directory if it doesn't exist.
     *
     * @return File object representing config directory (~/.config/TimeLogger/)
     */
    public static File getConfigDirectory()
    {
        if (configDirectory != null)
        {
            return configDirectory;
        }

        // Get XDG standard location: ~/.config/TimeLogger/
        String userHome = System.getProperty("user.home");
        if (userHome == null)
        {
            throw new RuntimeException("Unable to determine user home directory");
        }

        File xdgConfigDir = new File(userHome, ".config");
        File appConfigDir = new File(xdgConfigDir, CONFIG_DIR_NAME);

        // Create directory if it doesn't exist
        if (!appConfigDir.exists())
        {
            if (!appConfigDir.mkdirs())
            {
                throw new RuntimeException("Unable to create config directory: " + appConfigDir);
            }
        }

        configDirectory = appConfigDir;
        return configDirectory;
    }

    /**
     * Returns the main configuration file path.
     *
     * @return File object for ~/.config/TimeLogger/timelogger.cfg
     */
    public static File getMainConfigFile()
    {
        File dir = getConfigDirectory();
        return new File(dir, MAIN_CONFIG_FILE);
    }

    /**
     * Returns the profiles configuration file path.
     *
     * @return File object for ~/.config/TimeLogger/profiles.cfg
     */
    public static File getProfilesConfigFile()
    {
        File dir = getConfigDirectory();
        return new File(dir, PROFILES_CONFIG_FILE);
    }
}
