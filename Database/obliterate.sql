-- This file will wipe out the database! BE CAREFUL! --

REVOKE ALL ON DATABASE "SSUCalendar" FROM alexaskill;
REVOKE ALL ON DATABASE "SSUCalendar" FROM scraper;
REVOKE ALL ON DATABASE "SSUCalendar" FROM ssuadmin;

DROP SCHEMA ssucalendar CASCADE;

DROP ROLE alexaskill;
DROP ROLE scraper;
DROP ROLE ssuadmin;
