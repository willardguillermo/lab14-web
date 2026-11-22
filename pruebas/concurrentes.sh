#!/usr/bin/env bash
# Simula "abrir varias pestañas": lanza N peticiones simultáneas y muestra cuánto tardó cada una.
# Uso: ./pruebas/concurrentes.sh http://localhost:8083/api/productos 5
URL=${1:-http://localhost:8083/api/productos}
N=${2:-5}
for i in $(seq 1 "$N"); do
  (
    inicio=$(date +%s.%N)
    cuerpo=$(curl -s "$URL")
    fin=$(date +%s.%N)
    resumen=$(echo "$cuerpo" | grep -o '"nombre":"[^"]*temporal no disponible"' | sort | uniq -c | tr -s ' ')
    printf "Pestaña %d -> %5.1fs  %s\n" "$i" "$(echo "$fin - $inicio" | bc)" "${resumen:-respuesta normal}"
  ) &
  sleep 0.2
done
wait
