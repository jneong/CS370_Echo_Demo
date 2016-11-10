#!/bin/sh
############################################
# Build a deployment package for AWS Lambda
#
# Author: Ryan Moeller <moellerr@sonoma.edu>
# Date: 2016-10-24

if [ "$(whoami)" = "root" ]
then
	echo "This script should not be run as root."
	exit 1
fi

if ! ( grep -q "Amazon Linux" /etc/system-release && [ "$(uname -m)" = "x86_64" ] )
then
	echo "This script must be run on a 64-bit Amazon Linux EC2 instance."
	exit 1
fi

skip_build=0
skip_dependencies=0
skip_extract=0
update_lambda_code=0

while true
do
    case "${1}" in
        "--skip-build") skip_build=1; shift;;
        "--skip-dependencies") skip_dependencies=1; shift;;
        "--skip-extract") skip_extract=1; shift;;
        "--update-lambda-code") update_lambda_code=1; shift;;
        *) break;;
    esac
done

if [ ! -d "${1}" ]
then
    echo "usage: ${0} [--skip-build] [--skip-dependencies] [--skip-extract] [--update-lambda-code] PATH"
    exit 1
else
    WORK_DIR=$(cd "${1}" && pwd) || exit 1
    shift
fi

if [ ${skip_dependencies} -eq 0 ]
then
    sudo yum update
    PACKAGES="openssl-devel git python27-devel python27-pip"
    sudo yum install ${PACKAGES}
fi

cd "${WORK_DIR}"

[ ${skip_dependencies} -eq 0 ] && virtualenv .
. bin/activate

if [ ${skip_dependencies} -eq 0 ]
then
    DEPENDENCIES="requests vobject"
    pip install ${DEPENDENCIES}
fi

# We need to compile psycopg2 and postgresql95 from source, because Lambda
# doesn't have the PostgreSQL libs and none of the packages in pip seem to
# work as advertised.  We compile PostgreSQL as a static lib for psycopg2.


SRC_DIR="${WORK_DIR}/tmp/src"

PG_SRC_URL="https://ftp.postgresql.org/pub/source/v9.5.4/postgresql-9.5.4.tar.bz2"
PG_SRC_ARCHIVE=$(basename "${PG_SRC_URL}")
PG_SRC_DIRNAME=$(basename -s .tar.bz2 "${PG_SRC_URL}")
PG_SRC_DIR="${SRC_DIR}/${PG_SRC_DIRNAME}"
PG_PREFIX_DIR="${WORK_DIR}/tmp/prefix"

PS_SRC_URL="http://initd.org/psycopg/tarballs/PSYCOPG-2-6/psycopg2-2.6.2.tar.gz"
PS_SRC_ARCHIVE=$(basename "${PS_SRC_URL}")
PS_SRC_DIRNAME=$(basename -s .tar.gz "${PS_SRC_ARCHIVE}")
PS_SRC_DIR="${SRC_DIR}/${PS_SRC_DIRNAME}"
PS_BUILD_DIR="${PS_SRC_DIR}/build/lib.linux-x86_64-2.7"


#
# Download/extract sources
#

if [ ${skip_extract} -eq 0 ]
then
    mkdir -p "${SRC_DIR}"
    cd "${SRC_DIR}"

    # PostgreSQL
    rm -rf "${PG_SRC_DIR}"
    [ ! -f "${PG_SRC_ARCHIVE}" ] && wget "${PG_SRC_URL}"
    tar xf "${PG_SRC_ARCHIVE}"

    # Psycopg2
    rm -rf "${PS_SRC_DIR}"
    [ ! -f "${PS_SRC_ARCHIVE}" ] && wget "${PS_SRC_URL}"
    tar xf "${PS_SRC_ARCHIVE}"
fi


#
# Compile sources
#

if [ ${skip_build} -eq 0 ]
then
    # PostgreSQL
    cd "${PG_SRC_DIR}"

    rm -rf "${PG_PREFIX_DIR}"
    mkdir -p "${PG_PREFIX_DIR}"

    ./configure \
        --prefix "${PG_PREFIX_DIR}" \
        --without-readline \
        --without-zlib \
        --with-openssl
    make
    make install

    # Psycopg2
    cd "${PS_SRC_DIR}"

    # Configure psycopg2 to use SSL and static libpq, point it to our prefix
    cat > setup.cfg <<EOF
[build_ext]
define =
use_pydatetime = 1
have_ssl = 1
pg_config = ${PG_PREFIX_DIR}/bin/pg_config
static_libpq = 1

[egg_info]
tag_build = lambda-static
tag_date = 0
tag_svn_revision = 0
EOF

    # Patch setup.py to link to the ssl and crypto libs
    # (shouldn't `have_ssl = 1` do this?)
    ed setup.py <<EOF
/def finalize_linux(/+1a
        self.libraries.append('ssl')
        self.libraries.append('crypto')
.
wq
EOF

    python setup.py build
    python setup.py install
fi


#
# Create the deployment package
#

SCRAPER_ZIP="${WORK_DIR}/Scraper.zip"

cd "${WORK_DIR}"
rm -f "${SCRAPER_ZIP}"
zip -9 "${SCRAPER_ZIP}"

cd "${PS_BUILD_DIR}" && zip -gr9 "${SCRAPER_ZIP}" * && cd -
cd lib/python2.7/site-packages && zip -gr9 "${SCRAPER_ZIP}" * && cd -
cd lib64/python2.7/site-packages && zip -gr9 "${SCRAPER_ZIP}" * && cd -
#cd lib64/python2.7/site-packages/psycopg2-2.6.2lambda_static-py2.7-linux-x86_64.egg/ \
#    && zip -gr9 "${SCRAPER_ZIP}" psycopg2 && cd -

zip -g "${SCRAPER_ZIP}" root.crt
zip -g "${SCRAPER_ZIP}" scraper.py
zip -g "${SCRAPER_ZIP}" secrets.py

if [ ${update_lambda_code} -eq 1 ]
then
    aws s3 cp "${SCRAPER_ZIP}" s3://lambduhs/
    aws lambda update-function-code \
        --function-name ssuCalendarScraper \
        --s3-bucket lambduhs \
        --s3-key Scraper.zip
fi

echo "ok"
