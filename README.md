# TimeLogger

A desktop time-tracking utility for software developers following the Personal Software Process (PSP) methodology.

## Requirements

- Java 8 or higher

## Running

```bash
java -jar TimeLogger.jar
```

## Building

```bash
ant jar
ant package-jars
```

## Features

- Phase-based time tracking (Design, Code, Compile, Test, PSP, Review)
- Floating timer window with always-on-top option
- Log interruption tracking
- Save/export logs (.tlg files)
- Print reports with customizable headers
- Customizable phase names via profiles
- Keyboard shortcuts

## Screenshots

[![Screenshot-from-2026-03-23-07-01-37.png](https://i.postimg.cc/FKdz67Jd/Screenshot-from-2026-03-23-07-01-37.png)](https://postimg.cc/Y47tG2RM)

TimeLogger main screen showing a log with two entries.

[![Screenshot-from-2026-03-23-07-11-40.png](https://i.postimg.cc/6q99B3TL/Screenshot-from-2026-03-23-07-11-40.png)](https://postimg.cc/hz5FrKLf)

The floating timer window.

[![Screenshot-from-2026-03-23-07-02-15.png](https://i.postimg.cc/tgxR5wLm/Screenshot-from-2026-03-23-07-02-15.png)](https://postimg.cc/v1Gd8q8W)

The time summary dialog, showing total time in each phase.

[![Screenshot-from-2026-03-23-07-06-53.png](https://i.postimg.cc/G3qRkb43/Screenshot-from-2026-03-23-07-06-53.png)](https://postimg.cc/Rq62m512)

The Settings dialog, showing the tab with fields to enter task names.



## Files

- `.tlg` - Time log files
- `~/.config/TimeLogger/` - Settings and profiles

## Usage

See USER_GUIDE.md

## AI Transparency

The code was developed by humans with exception of two small feature enhancements. The README and the USER_GUIDE were written by generative AI. 
