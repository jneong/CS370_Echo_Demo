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

REPLACE_SCHEMA_NAME = getenv('REPLACE_SCHEMA_NAME', 'ssucalendar')


def ask_secrets():
    SecretField = namedtuple('SecretField', ['name', 'value'])
    Step = namedtuple('Step', ['name', 'default'])
    steps = (
        Step('host', None),
        Step('user', 'wolfpack'),
        Step('password', None),
        Step('dbname', 'ssunews'),
        Step('schema', None)
    )

    for step in steps:
        sys.stdout.write(step.name)
        if step.default is None:
            value = raw_input(": ")
            value = value.strip()
        else:
            value = raw_input(" [{:s}]: ".format(step.default))
            value = value.strip()
            if value == '':
                value = step.default
        yield SecretField(step.name, value)


def secrets_wizard(args=None):
    """
    Prompt the user for values to initialize a secrets file.
    """
    filename = args.path.rsplit('/', 1)[-1] if args else SECRETS_FILENAME
    path = args.path if args else SECRETS_PATH

    print("Creating {:s}...".format(filename))

    with open(path, 'w') as f:
        def write_secret(secret):
            secret_dict = secret._asdict()
            f.write('{name:s} = "{value:s}"'.format(**secret_dict))

        # Start a dictionary for credentials
        f.write("credentials = dict(\n")

        for secret in ask_secrets():
            if secret.name == 'schema':
                # Stop when we hit schema, it's not a credentials field
                break
            f.write('\t'); write_secret(secret); f.write(',\n')

        # Close the dictionary and write the schema as a separate variable
        f.write(')\n')
        write_secret(secret); f.write('\n')


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
        **secrets.credentials
    )
    return psycopg2.connect(**database_connect_args)


def get_schema_list(cursor):
    cursor.execute('SELECT schema_name FROM information_schema.schemata')
    return [result[0] for result in cursor.fetchall()]


def create_sql_template(filename):
    with open(filename) as f:
        return f.read().replace(REPLACE_SCHEMA_NAME, '%(schema)s')


def execute_schema_action(cursor, sql_file_path, schema):
    sql = create_sql_template(sql_file_path)
    cursor.execute(sql, dict(schema=AsIs(schema)))
    print("ok")


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


@with_cursor
def drop_schema(cursor, args):
    protected = ('pg_catalog', 'information_schema', 'ssucalendar')
    if schema in protected:
        print("dropping that schema is not allowed")
        return

    schema = args.schema
    if schema not in get_schema_list(cursor):
        print("schema does not exist")
        return

    execute_schema_action(cursor, OBLITERATE_PATH, schema)


if __name__ == "__main__":
    secrets = require_secrets()

    def register_handler(parser, handler):
        parser.set_defaults(func=partial(handler, secrets))

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
    secrets_parser.set_defaults(func=secrets_wizard)

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
    drop_parser.add_argument("schema", metavar="SCHEMA",
                             help="name of the schema to drop")
    register_handler(drop_parser, drop_schema)

    args = parser.parse_args() # exits on error
    args.func(args)
