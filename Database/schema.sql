--
-- Roles
--

CREATE ROLE scraper;
CREATE ROLE ssuadmin WITH ROLE wolfpack;
CREATE ROLE alexaskill;


--
-- Schema
--

CREATE SCHEMA ssucalendar;
ALTER SCHEMA ssucalendar OWNER TO ssuadmin;

SET search_path TO pg_catalog,public,ssucalendar;


--
-- Tables
--

-- `event_categories` stores a "many-to-many relationship" between events and
-- categories.  There are many categories, and each event can have more than one
-- category, so this table keeps track of which events have which categories.
CREATE TABLE ssucalendar.event_categories(
  event_id smallint NOT NULL,
  category_id smallint NOT NULL,
  CONSTRAINT event_categories_id PRIMARY KEY (event_id,category_id)
);
ALTER TABLE ssucalendar.event_categories OWNER TO ssuadmin;

-- The fields that are unique to every event are kept here, with a few minor
-- exceptions.
--
-- Booleans have only two possible values, so naturally values will be repeated
-- for many rows.  However, boolean values are so small that it would take more
-- space to store a table of the mappings to true/false than is needed to simply
-- store the value.  When there are multiple boolean columns in a table, the
-- values can be packed into the same byte, further saving space.
--
-- TODO: The timestamps really should be broken out into their own table.
--
-- The nullable fields are most likely to be null or different for the majority
-- of events.  They could be stored separately, but it might not be worth doing.
--
-- The summary text and description text in the calendar aren't necessarily in a
-- "speech-friendly" form, it is not practical to manually translate each event.
-- It may be desireable instead to design a translation function that can strip
-- unspeakable items (eg. HTML, URLs), and translate a few important words into
-- the proper SSML (eg. how to pronounce "Beaujolais").
CREATE TABLE ssucalendar.events(
  event_id integer NOT NULL,
  summary text NOT NULL, -- The summary is the "title" or "name" of the event
  description text NOT NULL, -- The description is long, may contain HTML
  location_id smallint NOT NULL,
  all_day_event boolean NOT NULL DEFAULT FALSE,
  start timestamp with time zone NOT NULL,
  "end" timestamp with time zone NOT NULL,
  event_type_id smallint NOT NULL,
  general_admission_fee text,
  student_admission_fee text,
  open_to_public boolean,
  website_url text,
  ticket_sales_url text,
  contact_id smallint,
  CONSTRAINT event_id PRIMARY KEY (event_id)
);
ALTER TABLE ssucalendar.events OWNER TO ssuadmin;

-- There are about 70 or so different locations encountered for >900 events, so
-- that merits a separate table.  We may want an additional column for adding an
-- "speech-friendly" version of the values.
CREATE TABLE ssucalendar.locations(
  location_id smallserial NOT NULL,
  name text NOT NULL, -- The values here can be a bit weird | see the calendar
  CONSTRAINT location_id PRIMARY KEY (location_id)
);
ALTER TABLE ssucalendar.locations OWNER TO ssuadmin;

-- Every event has an event type in addition to categories.  There are 15
-- different types so far.
CREATE TABLE ssucalendar.event_types(
  event_type_id smallserial NOT NULL,
  name text NOT NULL, -- Some contain / or ,
  CONSTRAINT event_type_id PRIMARY KEY (event_type_id)
);
ALTER TABLE ssucalendar.event_types OWNER TO ssuadmin;

-- The contacts table is very lazy.  Contact information is optional and all
-- fields are optional.  It's gross.
CREATE TABLE ssucalendar.contacts(
  contact_id smallserial NOT NULL,
  name text,
  phone text,
  email text,
  CONSTRAINT contact_id PRIMARY KEY (contact_id)
);
ALTER TABLE ssucalendar.contacts OWNER TO ssuadmin;

-- There are 9 main categories and 24 custom categories.  We just throw them all
-- together.  The category names can be very awkward, so there will likely need
-- to be an additional column for an "speech-friendly" version of these values.
CREATE TABLE ssucalendar.categories(
  category_id smallserial NOT NULL,
  name text NOT NULL, -- These can have really weird values
  CONSTRAINT category_id PRIMARY KEY (category_id)
);
ALTER TABLE ssucalendar.categories OWNER TO ssuadmin;


--
-- Foreign key constraints
--

-- TODO: figure out correct ON DELETE behaviors

ALTER TABLE ssucalendar.event_categories ADD CONSTRAINT event_id FOREIGN KEY (event_id)
  REFERENCES ssucalendar.events (event_id) MATCH FULL
  ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE ssucalendar.event_categories ADD CONSTRAINT category_id FOREIGN KEY (category_id)
  REFERENCES ssucalendar.categories (category_id) MATCH FULL
  ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE ssucalendar.events ADD CONSTRAINT location_id FOREIGN KEY (location_id)
  REFERENCES ssucalendar.locations (location_id) MATCH FULL
  ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE ssucalendar.events ADD CONSTRAINT event_type_id FOREIGN KEY (event_type_id)
  REFERENCES ssucalendar.event_types (event_type_id) MATCH FULL
  ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE ssucalendar.events ADD CONSTRAINT contact_id FOREIGN KEY (contact_id)
  REFERENCES ssucalendar.contacts (contact_id) MATCH FULL
  ON DELETE NO ACTION ON UPDATE NO ACTION;


--
-- Permissions
--

GRANT CONNECT
  ON DATABASE "SSUCalendar"
  TO scraper,alexaskill;

GRANT CREATE,CONNECT
  ON DATABASE "SSUCalendar"
  TO ssuadmin;

GRANT CREATE,USAGE
  ON SCHEMA ssucalendar
  TO ssuadmin;

GRANT USAGE
  ON SCHEMA ssucalendar
  TO alexaskill,scraper;

GRANT SELECT
  ON TABLE ssucalendar.events
  TO alexaskill;

GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES,TRIGGER
  ON TABLE ssucalendar.events
  TO scraper;
