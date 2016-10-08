# Scraper

## Overview

The scraper parses iCalendar format calendar files for event information
and uses it to update the database.

## Testing

To test the scraper, set up a PostgreSQL server using the schema from
`../Database/schema.sql`.  Amazon RDS makes it easy to create a server in
their public cloud, or you can run your own PostgreSQL server locally.

*DO NOT TEST ON THE MAIN DATABASE*

Using a tool like pgAdmin4, DataGrip, pgModeler, or even LibreOffice or
MS Access can be helpful for viewing the contents of the database in a
graphical environment, if that suits you.

## secrets.py

You must copy secrets.py-sample to create secrets.py, filling in the values
to connect to your database.  This file will not be tracked by git.
