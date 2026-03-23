package timelogger;

import java.io.*;
import java.util.*;

/**
 * Manages task name profiles for TimeLogger.
 * Profiles are stored in ~/.config/TimeLogger/profiles.cfg using INI-style format.
 *
 * @author Claude Code
 */
public class ProfileManager
{
    private Map<String, Vector<String>> profiles;

    /**
     * Constructs a ProfileManager and loads all profiles from disk.
     */
    public ProfileManager()
    {
        profiles = new LinkedHashMap<>();  // Preserve insertion order
        loadAllProfiles();
    }

    /**
     * Returns a list of all profile names.
     *
     * @return List of profile names in the order they were created
     */
    public List<String> listProfiles()
    {
        return new ArrayList<>(profiles.keySet());
    }

    /**
     * Loads task names for a specific profile.
     *
     * @param profileName the name of the profile to load
     * @return Vector of task names, or null if profile doesn't exist
     */
    public Vector<String> loadProfile(String profileName)
    {
        Vector<String> tasks = profiles.get(profileName);
        if (tasks == null)
        {
            return null;
        }
        // Return a copy to prevent external modification
        return new Vector<>(tasks);
    }

    /**
     * Saves or updates a profile with the given task names.
     *
     * @param profileName the name of the profile to save
     * @param taskNames the task names to store in this profile
     */
    public void saveProfile(String profileName, Vector<String> taskNames)
    {
        profiles.put(profileName, new Vector<>(taskNames));
        saveAllProfiles();
    }

    /**
     * Deletes a profile from disk.
     *
     * @param profileName the name of the profile to delete
     */
    public void deleteProfile(String profileName)
    {
        profiles.remove(profileName);
        saveAllProfiles();
    }

    /**
     * Checks if a profile exists.
     *
     * @param profileName the name of the profile to check
     * @return true if the profile exists, false otherwise
     */
    public boolean profileExists(String profileName)
    {
        return profiles.containsKey(profileName);
    }

    /**
     * Loads all profiles from the TimeLogger_Profiles.cfg file.
     * File format:
     * [ProfileName]
     * TASK1
     * TASK2
     *
     * [AnotherProfile]
     * TASKX
     * TASKY
     */
    private void loadAllProfiles()
    {
        File file = ConfigManager.getProfilesConfigFile();
        if (!file.exists())
        {
            return;  // No profiles yet
        }

        try
        {
            FileReader inputStream = new FileReader(file);
            BufferedReader reader = new BufferedReader(inputStream);
            String line;
            String currentProfile = null;
            Vector<String> currentTasks = null;

            while ((line = reader.readLine()) != null)
            {
                line = line.trim();

                if (line.isEmpty())
                {
                    continue;  // Skip blank lines
                }

                if (line.startsWith("[") && line.endsWith("]"))
                {
                    // Save previous profile if any
                    if (currentProfile != null && currentTasks != null)
                    {
                        profiles.put(currentProfile, currentTasks);
                    }
                    // Start new profile
                    currentProfile = line.substring(1, line.length() - 1);
                    currentTasks = new Vector<>();
                }
                else if (currentProfile != null)
                {
                    // Add task to current profile
                    currentTasks.add(line);
                }
            }

            // Save last profile
            if (currentProfile != null && currentTasks != null)
            {
                profiles.put(currentProfile, currentTasks);
            }

            reader.close();
            inputStream.close();
        }
        catch (IOException ex)
        {
            System.err.println("Error loading profiles: " + ex);
        }
    }

    /**
     * Saves all profiles to the TimeLogger_Profiles.cfg file.
     */
    private void saveAllProfiles()
    {
        try
        {
            FileWriter outputStream = new FileWriter(ConfigManager.getProfilesConfigFile());
            PrintWriter writer = new PrintWriter(outputStream);

            for (Map.Entry<String, Vector<String>> entry : profiles.entrySet())
            {
                writer.println("[" + entry.getKey() + "]");
                for (String task : entry.getValue())
                {
                    writer.println(task);
                }
                writer.println();  // Blank line between profiles
            }

            writer.close();
            outputStream.close();
        }
        catch (IOException ex)
        {
            System.err.println("Error saving profiles: " + ex);
        }
    }
}
