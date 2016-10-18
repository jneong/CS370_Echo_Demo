-- BE CAREFUL! --

REVOKE ALL ON DATABASE ssunews FROM alexaskill;
REVOKE ALL ON DATABASE ssunews FROM scraper;
REVOKE ALL ON DATABASE ssunews FROM ssuadmin;

DROP ROLE alexaskill;
DROP ROLE scraper;
DROP ROLE ssuadmin;
