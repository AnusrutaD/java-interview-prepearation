#!/usr/bin/env bash
# Runs every Streams question file and shows a scoreboard. Works on Java 8+ (compiles first).
#   ./run-streams.sh            -> your answers   (streams/questions)
#   ./run-streams.sh solutions  -> reference answers
#   ./run-streams.sh 07         -> just one question (ST07), with full output
cd "$(dirname "$0")"
command -v javac >/dev/null 2>&1 || { echo "javac not found. Install a JDK (not only a JRE) or set JAVA_HOME, e.g.: export JAVA_HOME=\$(/usr/libexec/java_home -v 1.8)"; exit 1; }
dir="streams/questions"; [ "$1" = "solutions" ] && { dir="streams/solutions"; shift; }
pkg="${dir//\//.}"
pattern="${1:-}"
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
# Enforce Java 8 rules even on a newer JDK (javac --release 8); real JDK 8 has no --release flag
if javac --release 8 -version >/dev/null 2>&1; then JAVAC_FLAGS="--release 8 -Xlint:-options"; else JAVAC_FLAGS=""; fi
done_count=0; total=0
for f in "$dir"/ST${pattern}*.java; do
  total=$((total+1))
  cls="$pkg.$(basename "$f" .java)"
  if out=$(javac $JAVAC_FLAGS -d "$tmp" "$f" 2>&1 | grep -v JAVA_TOOL_OPTIONS; exit ${PIPESTATUS[0]}); then
    out=$(java -cp "$tmp" "$cls" 2>&1 | grep -v JAVA_TOOL_OPTIONS)
    if echo "$out" | grep -q " 0 failed"; then status="DONE"; done_count=$((done_count+1));
    elif echo "$out" | grep -q "Exception"; then status="RUNTIME ERROR";
    else status="todo"; fi
  else
    status="COMPILE ERROR"
  fi
  [ -n "$pattern" ] && echo "$out"
  printf "%-45s %s\n" "$(basename "$f" .java)" "$status"
done
echo "-------------------------------------------------"
echo "$done_count / $total solved"
