# this script is called when the judge wants our compiler to compile a source file.
# print the compiled source, i.e. asm code, directly to stdout.
# don't print anything other to stdout.
# if you would like to print some debug information, please go to stderr.
# $1 is the path to the source file.
# $2 is the path to the target file.

set -e
cd "$(dirname "$0")"
export CCHK="java -classpath ./lib/antlr-4.6-complete.jar:./bin com.mercy.Main"
cat > testcase/test.c   # save everything in stdin to program.txt
$CCHK
