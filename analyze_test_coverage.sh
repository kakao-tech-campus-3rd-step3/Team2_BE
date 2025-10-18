CSV=build/reports/jacoco/test/jacocoTestReport.csv
DELIM=$(head -1 "$CSV" | grep -q ';' && echo ';' || echo ',')

awk -F"$DELIM" '
  NR==1 { for(i=1;i<=NF;i++) h[$i]=i; next }
  ($h["CLASS"]!="") {
    key = $h["PACKAGE"] "." $h["CLASS"]
    im[key] += ($h["INSTRUCTION_MISSED"]+0)
    bm[key] += ($h["BRANCH_MISSED"]+0)
    lm[key] += ($h["LINE_MISSED"]+0)
  }
  END {
    for (k in lm) {
      total = im[k]+bm[k]+lm[k]
      printf "%08d  %s  |  LINE_MISSED=%d, BRANCH_MISSED=%d, INSTR_MISSED=%d\n", total, k, lm[k], bm[k], im[k]
    }
  }
' "$CSV" | sort -nr | sed 's/^[0-9]\{8\}  //' | head -50
