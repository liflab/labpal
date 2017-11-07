#!/bin/bash
# Script:       sauber
# Object:       Cleans up your Linux file system after a 
#                   session with AppleTalk and Finder.
# Etymologie:   sauber means clean in German
# Author:       originally by Christian Imhorst [http://www.datenteiler.de/what-is-2eds_store/]
#                   modified by Gordon Davisson

# Test number of arguments here
if (( $# < 1 )) ; then
    echo >&2
    echo "We need an argument here." >&2
    echo "Usage:   ./sauber [Directory]" >&2
    echo "Example: ./sauber /home/christian"  >&2
    echo >&2
    exit 1
elif [[ ! -d "$1" ]] ; then
    echo "$1 is not a directory" >&2
    exit 1
fi

find "$1" \( -iname ':2eDS_Store' \
    -o -iname '.DS_Store' \
    -o -iname '.AppleDouble' \
    -o -iname 'Network Trash Folder' \
    -o -iname 'Temporary Items' \
    -o -iname ':2eTemporary Items' \
    -o -iname '.Temporary Items' \
    -o -iname ':2elocalized' \
    -o -iname '.localized' \
    -o -iname ':2e_*' \
    -o -iname '._*' \) -exec rm -rf {} \;