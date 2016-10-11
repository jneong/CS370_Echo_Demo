--
-- Roles
--

CREATE ROLE ssuadmin WITH ROLE wolfpack;

-- Remember to set a password (but never commit it)
CREATE ROLE scraper WITH
  CONNECTION LIMIT 1
  LOGIN
  PASSWORD '';

-- Remember to set a password (but never commit it)
CREATE ROLE alexaskill WITH
  LOGIN
  PASSWORD '';


--
-- Permissions
--

GRANT CONNECT
  ON DATABASE ssunews
  TO scraper,alexaskill;

GRANT CREATE,CONNECT
  ON DATABASE ssunews
  TO ssuadmin;
