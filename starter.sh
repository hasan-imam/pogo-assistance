while true; do
  echo "Starting program..."
  java -jar build/libs/pogo-assistance-1.0-SNAPSHOT-all.jar -feed -responder -listener RELAY_POKEX_TO_PDEX100 -listener RELAY_POKEX_TO_POGONICE >> log-nov-3-1-11-pm.txt &
  last_pid=$!
  echo "Program started at PID: $last_pid"

  echo "Sleeping for 6h 10m..."
  sleep 21610
  echo "Woke up"

  echo "Killing program at PID $last_pid..."
  kill -KILL $last_pid
  echo "Killed"
  sleep 5
done

