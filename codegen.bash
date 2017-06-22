set -e
cd "$(dirname "$0")"
cat > testcase/test.c   # save everything in stdin to test.c
java -jar Malic.jar -in testcase/test.c -out out.asm
cat out.asm
