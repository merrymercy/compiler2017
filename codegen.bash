set -e
cd "$(dirname "$0")"
export CCHK="java -classpath ./lib/antlr-4.6-complete.jar:./bin com.mercy.Main"
cat > testcase/test.c   # save everything in stdin to program.txt
$CCHK
cat out.asm
