# Scraper

## Overview

The scraper parses iCalendar format calendar files for event information
and uses it to update the database.

## Testing

To test the scraper, create a schema for testing using
`../Database/tools/test-schema-tool.py`, and configure `secrets.py` in this
directory to use the test schema you created.

*ONLY USE THE TEST DATABASE. DO NOT TEST ON THE MAIN DATABASE*

Using a tool like pgAdmin4, DataGrip, pgModeler, or even LibreOffice or
MS Access can be helpful for viewing the contents of the database in a
graphical environment, if that suits you.

## secrets.py

You must copy secrets.py-sample to create secrets.py, filling in the values
to connect to your database.  Alternatively, `test-schema-tool.py` can be
used to generate a `secrets.py` file for you.  This file will not be
tracked by git.
