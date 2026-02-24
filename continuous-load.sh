#!/bin/bash

# 継続的な負荷をかけるスクリプト
# 使い方: ./continuous-load.sh [URL] [秒間リクエスト数]

URL="${1:-http://localhost:8080}"
RPS="${2:-5}"  # Requests Per Second
INTERVAL=$(echo "scale=2; 1/$RPS" | bc)

echo "==================================="
echo "継続的負荷テスト開始"
echo "==================================="
echo "URL: $URL"
echo "秒間リクエスト数: $RPS"
echo "停止するには Ctrl+C を押してください"
echo "==================================="

counter=0
start_time=$(date +%s)

trap 'echo ""; echo "総リクエスト数: $counter"; echo "実行時間: $(($(date +%s) - start_time))秒"; exit' INT

while true; do
    response=$(curl -s -o /dev/null -w "%{http_code}" "$URL")
    counter=$((counter + 1))
    
    if [ $((counter % 10)) -eq 0 ]; then
        echo "$(date '+%H:%M:%S') - リクエスト数: $counter - ステータス: $response"
    fi
    
    sleep $INTERVAL
done
