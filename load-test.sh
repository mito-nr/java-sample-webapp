#!/bin/bash

# 負荷テストスクリプト
# 使い方: ./load-test.sh [URL] [同時接続数] [リクエスト総数]

URL="${1:-http://localhost:8080}"
CONCURRENT="${2:-10}"
REQUESTS="${3:-1000}"

echo "==================================="
echo "負荷テスト開始"
echo "==================================="
echo "URL: $URL"
echo "同時接続数: $CONCURRENT"
echo "リクエスト総数: $REQUESTS"
echo "==================================="

# curlを使った継続的な負荷テスト
counter=0
while [ $counter -lt $REQUESTS ]; do
    for i in $(seq 1 $CONCURRENT); do
        curl -s -o /dev/null -w "%{http_code} - %{time_total}s\n" "$URL" &
    done
    wait
    counter=$((counter + CONCURRENT))
    echo "完了: $counter / $REQUESTS リクエスト"
    sleep 0.1
done

echo "==================================="
echo "負荷テスト完了"
echo "==================================="
