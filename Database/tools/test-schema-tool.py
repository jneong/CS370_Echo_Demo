#!/usr/bin/env python2.7

import argparse
from collections import namedtuple
from functools import partial, wraps
from importlib import import_module
import os, sys

import psycopg2
from psycopg2.extensions import AsIs


DESCRIPTION = "Manipulate database schema"
EPILOG = """
examples:
  Generate a secrets file for the scraper:
    %(prog)s secrets ../../Scraper/secrets.py

  List the schema on the configured database:
    %(prog)s list

  Create a schema named "foo_testing":
    %(prog)s create foo_testing

  Drop the schema named "foo_testing":
    %(prog)s drop foo_testing

hints:
  This tool uses schema.sql from the parent directory.  Modifications to
  that file can be tested by creating a new schema on the testing database
  with this tool.

  A different schema file can be specified by setting SCHEMA_PATH in the
  environment when running this script.  See the top of the source for this
  script for additional environment variables that can be used to control
  additional settings.
"""


#
# The settings below can be overriden by passing them in the environment.
#

def getenv(var, default):
    return os.environ.get(var, default)

SECRETS_FILENAME = getenv('SECRETS_FILENAME', 'secrets.py')
SECRETS_DIR = getenv('SECRETS_DIR', './')
SECRETS_PATH = getenv('SECRETS_PATH', SECRETS_DIR + SECRETS_FILENAME)

SSL_ROOT_CERT_PATH = getenv('SSL_ROOT_CERT_PATH', '../../Scraper/root.crt')
SSL_CRL_PATH = getenv('SSL_CRL_PATH', '')

SCHEMA_PATH = getenv('SCHEMA_PATH', '../schema.sql')
OBLITERATE_PATH = getenv('OBLITERATE_PATH', '../obliterate.sql')
CALENDAR_URLS_PATH = getenv('CALENDAR_URLS_PATH', '../calendar_urls.sql')

REPLACE_SCHEMA_NAME = getenv('REPLACE_SCHEMA_NAME', 'ssucalendar')


def ask_secret(secret):
    """
    Prompts the user for a secret with an optional default value, and
    returns their answer or the default if it exists and the received value
    is empty.

    """
    sys.stdout.write(secret.name)

    if secret.default is None:
        value = raw_input(": ")
    else:
        value = raw_input(" [{:s}]: ".format(secret.default))
        if value == '':
            value = secret.default

    return value


def secrets_wizard(path=SECRETS_PATH):
    """
    Prompt the user for values to initialize a secrets file.  The path to
    the file may optionally be specified in args.path, otherwise a default
    value is used.
    """
    # Secrets have a name and an optional default value or None if there is
    # no default.
    Secret = namedtuple('Secret', ['name', 'default'])
    credentials = (
        Secret('host', None),
        Secret('user', 'wolfpack'),
        Secret('password', None),
        Secret('dbname', 'ssunews'),
    )
    schema = Secret('schema', None)

    print("Creating {:s}...".format(path))

    with open(path, 'w') as f:
        def write_secret(secret):
            """
            Prompt the user for a secret and write it to f.

            For example, writes 'foo = "bar"' when the secret name is "foo"
            and the value entered by the user when prompted is "bar".
            """
            f.write(
                '{name:s} = "{value:s}"'.format(
                    name = secret.name,
                    value = ask_secret(secret)
                )
            )

        # Start a dictionary for credentials
        f.write("credentials = dict(\n")

        # Write each secret as a named parameter
        for secret in credentials:
            f.write('\t'); write_secret(secret); f.write(',\n')

        # Close the dictionary and write the schema as a separate variable
        f.write(')\n')
        write_secret(schema); f.write('\n')


def require_secrets():
    """
    Import and return the secrets module.  If the file does not exist, the
    user is prompted for the values to create it.
    """
    if not SECRETS_FILENAME in os.listdir(SECRETS_DIR):
        print("{:s} does not exist".format(SECRETS_PATH))
        secrets_wizard()

    return import_module(SECRETS_FILENAME.rsplit('.', 1)[0])


def get_database_connection(secrets):
    database_connect_args = dict(
        sslmode = 'verify-full',
        sslrootcert = SSL_ROOT_CERT_PATH,
        sslcrl = SSL_CRL_PATH,
        options = '-c search_path={}'.format(secrets.schema),
        **secrets.credentials
    )
    return psycopg2.connect(**database_connect_args)


def get_schema_list(cursor):
    cursor.execute('SELECT schema_name FROM information_schema.schemata')
    return [result[0] for result in cursor.fetchall()]


def create_sql_template(filename):
    """
    Returns the contents of the specified file, but with all occurrences of
    the schema name specified by REPLACE_SCHEMA_NAME replaced with a
    template string for the key "schema".

    The result can be used as a statement for executing a parameterized
    database query, where the schema is specified by the "schema" key.
    """
    with open(filename) as f:
        return f.read().replace(REPLACE_SCHEMA_NAME, '%(schema)s')


def execute_schema_action(cursor, sql_file_path, schema):
    """
    Using the given database cursor, this function executes the contents of
    the file specified by sql_file_path but with all occurrences of the
    schema name specified by REPLACE_SCHEMA_NAME replaced with the schema
    name specified by the schema parameter.

    If no exceptions occur, the message "ok" is printed.
    """
    sql = create_sql_template(sql_file_path)
    cursor.execute(sql, dict(schema=AsIs(schema)))


def with_cursor(func):
    """
    Decorator that wraps func in a database connection.
    The resulting function takes a secrets object and args.
    The wrapped function gets a cursor object and args.
    """
    @wraps(func)
    def inner(secrets, args):
        with get_database_connection(secrets) as connection:
            with connection.cursor() as cursor:
                return func(cursor, args)
    return inner


@with_cursor
def list_schema(cursor, args):
    internal_schema = ('pg_catalog', 'information_schema')
    for schema in get_schema_list(cursor):
        if schema not in internal_schema:
            print(schema)


@with_cursor
def create_schema(cursor, args):
    schema = args.schema
    if schema in get_schema_list(cursor):
        print("schema already exists (drop first to replace)")
        return

    execute_schema_action(cursor, SCHEMA_PATH, schema)
    execute_schema_action(cursor, CALENDAR_URLS_PATH, schema)

    print("ok")

    print("""
Hint:

First, make sure you set the correct schema in ../../Scraper/secrets.py
For example:

	schema = "{:s}"

Now to populate the tables, activate the virtualenv in ../../Scraper/ and
run scraper.py:

	cd ../../Scraper/
	. bin/activate
	./scraper.py
""".format(schema))

@with_cursor
def drop_schema(cursor, args):
    protected = ('pg_catalog', 'information_schema', 'ssucalendar')
    schema = args.schema
    if schema in protected:
        if getattr(args, 'force', False) == True:
            print("user forced dropping protected schema")
        else:
            print("dropping that schema is not allowed")
            return

    if schema not in get_schema_list(cursor):
        print("schema does not exist")
        return

    execute_schema_action(cursor, OBLITERATE_PATH, schema)

    print("ok")


if __name__ == "__main__":
    secrets = require_secrets()

    def register_handler(parser, handler):
        parser.set_defaults(func=partial(handler, secrets))

    def wizard(args):
        return secrets_wizard(**args)

    parser = argparse.ArgumentParser(
        formatter_class = argparse.RawDescriptionHelpFormatter,
        description = DESCRIPTION,
        epilog = EPILOG
    )
    subparsers = parser.add_subparsers(help="sub-command help")

    secrets_parser = subparsers.add_parser("secrets",
                                           help="configure secrets")
    secrets_parser.add_argument("path", metavar="PATH",
                                help="path to output file")
    secrets_parser.set_defaults(func=wizard)

    list_parser = subparsers.add_parser("list",
                                        help="list schema")
    register_handler(list_parser, list_schema)

    create_parser = subparsers.add_parser("create",
                                          help="create a test schema")
    create_parser.add_argument("schema", metavar="SCHEMA",
                               help="name to call the new schema")
    register_handler(create_parser, create_schema)

    drop_parser = subparsers.add_parser("drop",
                                        help="drop a test schema")
    drop_parser.add_argument("-f", "--force", action="store_true",
                             help="force removal of protected schema")
    drop_parser.add_argument("schema", metavar="SCHEMA",
                             help="name of the schema to drop")
    register_handler(drop_parser, drop_schema)

    args = parser.parse_args() # exits on error
    args.func(args)
