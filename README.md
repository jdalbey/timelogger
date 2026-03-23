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

## Files

- `.tlg` - Time log files
- `~/.config/TimeLogger/` - Settings and profiles

## Usage

See USER_GUIDE.md

## AI Transparency

The code was developed by humans with exception of two small feature enhancements. The README and the USER_GUIDE were written by generative AI. 
