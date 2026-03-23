# TimeLogger User Guide

**Version 1.29**

TimeLogger is a desktop time-tracking utility designed for software developers to record time spent on various development activities. It follows the Personal Software Process (PSP) methodology for tracking time by development phases.

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [The Main Window](#the-main-window)
3. [Tracking Time](#tracking-time)
4. [Managing Log Entries](#managing-log-entries)
5. [File Operations](#file-operations)
6. [Analysis and Reports](#analysis-and-reports)
7. [Customization](#customization)
8. [Keyboard Shortcuts](#keyboard-shortcuts)

---

## Getting Started

### System Requirements
- Java Runtime Environment (JRE) 8 or higher

### Installation
1. Extract the TimeLogger distribution to your preferred location
2. Run `java -jar TimeLogger.jar` or double-click the JAR file

### Starting TimeLogger
Launch the application from your terminal:
```bash
java -jar TimeLogger.jar
```

On first launch, a blank log is created. Your settings and profiles are stored in `~/.config/TimeLogger/`.

---

## The Main Window

The main window consists of three areas:

```
+------------------------------------------------------------------+
|  Menu Bar: File | Insert | Tools | Options | Help                |
+------------------------------------------------------------------+
|           |                                                      |
|  [Design] |  Start  Stop   Delta  Task   Comment                  |
|  [Code  ] |  --------------------------------------------------  |
|  [Compile] |  09:00 09:45   45    DESIGN  Initial requirements    |
|  [Test   ] |  09:45 10:30   45    CODE    Module implementation  |
|  [PSP   ] |                                                      |
|  [Review ] |  (Log entries appear here)                          |
|           |                                                      |
|  [Interrupt]|                                                    |
+------------------------------------------------------------------+
```

- **Left Panel**: Phase timer buttons
- **Center Panel**: List of logged time entries
- **Menu Bar**: File operations, inserting entries, tools, and settings

---

## Tracking Time

### Starting a Phase Timer

1. Click one of the phase buttons on the left panel (Design, Code, Compile, Test, PSP, Review)
2. A floating timer window appears showing elapsed time
3. The timer window displays:
   - Current elapsed time (HH:MM:SS)
   - Start time
   - Current phase name

### Stopping a Timer

1. Click anywhere on the timer window (or press any key)
2. The log entry is automatically added to the main list
3. The timer window closes

### Timer Window Options

- **Always-on-Top**: The timer stays above other windows (configurable in Settings)
- **Minimize**: When minimized, the timer displays elapsed time in the window title bar

### Recording Interruptions

When you are interrupted during a phase:

1. Finish and stop the current timer
2. Click the **Interrupt** button in the main window
3. Enter the number of minutes interrupted in the dialog
4. Click OK
5. The interruption is logged as a negative delta entry

---

## Managing Log Entries

### Viewing Entries

Log entries display in the center panel with columns:
- **Start**: Start time (HH:MM)
- **Stop**: End time (HH:MM)
- **Delta**: Duration in minutes
- **Task**: Phase name
- **Comment**: Optional description

### Adding a Manual Entry

1. Select **Insert > Add Log Entry...**
2. Enter the start time (HH:MM)
3. Enter the stop time (HH:MM)
4. Select the task from the dropdown
5. Optionally add a comment
6. Click OK

### Editing a Comment

1. Double-click on any log entry
2. Modify the comment in the dialog
3. Click OK to save

### Deleting an Entry

1. Right-click on the entry
2. Select Delete from the context menu
3. Confirm the deletion

### Clearing the Log

Select **File > Clear Log** to remove all entries from the current log.

---

## File Operations

### Creating a New Log

Select **File > New** or press `Ctrl+N`

**Note**: You will be prompted to save unsaved changes if the current log has modifications.

### Opening an Existing Log

Select **File > Open** or press `Ctrl+O`

TimeLogger uses `.tlg` file extension. Select your log file and click Open.

### Recent Files

Select **File > Recent Files** to quickly access previously opened logs.

### Saving a Log

- **Save** (`Ctrl+S`): Save to the current file
- **Save As** (`Ctrl+Shift+S`): Save to a new location with a new name

### Exporting to Text

Select **File > Export** or press `Ctrl+E`

This creates a `.txt` file containing:
- Header with your name, project, and date
- All log entries
- Time totals per phase

### Printing

Select **File > Print** or press `Ctrl+P`

Configure print settings via **Options > Settings > Print Setup**:
- **Name**: Your name
- **Project**: Project name
- **Date**: Date for the report

---

## Analysis and Reports

### Viewing Time Totals

Select **Tools > Total Times** or press `Ctrl+T`

A dialog displays:
- Total minutes for each phase
- Grand total of all time logged

---

## Customization

### Configuring Settings

Select **Options > Settings**

#### Print Setup Tab
- **Name**: Enter your name for printed reports
- **Project**: Enter the project name
- **Date**: Enter the report date

#### Task Names Tab
- Customize phase button names (up to 9 tasks)
- Manage profiles for different project configurations
- Save, update, or delete profiles

### Changing the Font

Select **Options > Font** to change the user interface font.

### Restoring Defaults

Select **Options > Restore Defaults** to reset all settings to their original values.

### Profile Management

Profiles save your custom task configurations:

1. Go to **Options > Settings > Task Names**
2. Enter custom names in the task fields
3. Click **Save** to save as a new profile
4. Use the profile dropdown to switch between configurations
5. Click **Update** to modify the selected profile
6. Click **Delete** to remove a profile

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl+N | New file |
| Ctrl+O | Open file |
| Ctrl+S | Save file |
| Ctrl+Shift+S | Save As |
| Ctrl+E | Export to text |
| Ctrl+P | Print |
| Ctrl+T | Show total times |
| Ctrl+X | Exit |
| Ctrl+Q | Quit |

---

## Known Limitations

- Totals are accurate only for entries with Delta up to 999 minutes
- Time duration does not wrap around midnight (entries must start and end on the same day)
- Minimum recorded delta is 1 minute (even if timer is stopped immediately)

---

## Getting Help

Select **Help > Quick Start Guide** for a brief overview of TimeLogger features.

For more information, select **Help > About** to view version details.
