while true; do
  echo "Starting program..."
  java -jar build/libs/pogo-assistance-1.0-SNAPSHOT-all.jar -feed -responder -listener RELAY_POKEX_TO_PDEX100 -listener RELAY_POKEX_TO_POGONICE >> log-nov-3-1-11-pm.txt &
  last_pid=$!
  echo "Program started at $(date) (PID: $last_pid)"

  echo "Waiting for program to finish..."
  wait $last_pid
  echo "Program finished at $(date)"
  sleep 5
done
