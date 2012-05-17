#!/bin/bash
#
# 1. gets list of current java process ids that this user is running
# 2. filters out the process id supplied as a command line argument to the script ($1)
# 3. does a kill -9 on each of the non-filtered process ids
#
# Usage:
# ./kill_java_processes.sh 32423

if [ -z "$1" ]
then
    echo "ERROR: pass a pid to ignore"
else
    # TODO: someone with some shell-foo needs to do some refactoring.

    echo "Process list:"
    ps auwx | grep `whoami` | grep java | grep -v grep
    echo "Killing these pids:"
    ps auwx | grep `whoami` | grep java | grep -v grep | awk '{print $2}' | grep -v $1

    # Actually kill the pids:
    ps auwx | grep `whoami` | grep java | grep -v grep | awk '{print $2}' | grep -v $1 | xargs kill -9
fi
