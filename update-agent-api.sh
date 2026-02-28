#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/fun-ai-agent-api}"
SERVICE_NAME="${SERVICE_NAME:-fun-ai-agent-api}"
GIT_REMOTE="${GIT_REMOTE:-origin}"
GIT_BRANCH="${GIT_BRANCH:-main}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/v1/health}"
HEALTH_RETRIES="${HEALTH_RETRIES:-20}"
HEALTH_WAIT_SECONDS="${HEALTH_WAIT_SECONDS:-2}"
MVN_ARGS="${MVN_ARGS:--DskipTests clean package}"
MVN_CMD="${MVN_CMD:-mvn}"
MIN_MAVEN_VERSION="${MIN_MAVEN_VERSION:-3.6.3}"

if [[ ! -d "${APP_DIR}/.git" ]]; then
  echo "ERROR: APP_DIR is invalid: ${APP_DIR}"
  exit 1
fi

if ! command -v "${MVN_CMD}" >/dev/null 2>&1; then
  echo "ERROR: mvn not found (MVN_CMD=${MVN_CMD})"
  exit 1
fi

if ! command -v systemctl >/dev/null 2>&1; then
  echo "ERROR: systemctl not found"
  exit 1
fi

cd "${APP_DIR}"

maven_version="$("${MVN_CMD}" -v | awk '/Apache Maven/{print $3; exit}')"
if [[ -z "${maven_version}" ]]; then
  echo "ERROR: cannot detect Maven version"
  exit 1
fi

if [[ "$(printf '%s\n%s\n' "${MIN_MAVEN_VERSION}" "${maven_version}" | sort -V | head -n1)" != "${MIN_MAVEN_VERSION}" ]]; then
  echo "ERROR: Maven ${maven_version} is too old, require >= ${MIN_MAVEN_VERSION}"
  echo "HINT: install Maven 3.9.x and rerun script"
  exit 1
fi

echo "[1/5] Pull latest code from ${GIT_REMOTE}/${GIT_BRANCH}"
git fetch "${GIT_REMOTE}" "${GIT_BRANCH}"
git checkout "${GIT_BRANCH}"
git pull --ff-only "${GIT_REMOTE}" "${GIT_BRANCH}"

echo "[2/5] Build jar"
"${MVN_CMD}" ${MVN_ARGS}

JAR_PATH="$(find target -maxdepth 1 -type f -name 'fun-ai-agent-api-*.jar' ! -name 'original-*.jar' | head -n 1)"
if [[ -z "${JAR_PATH}" ]]; then
  echo "ERROR: built jar not found in target/"
  exit 1
fi

echo "[3/5] Update app.jar"
cp "${JAR_PATH}" app.jar

echo "[4/5] Restart service ${SERVICE_NAME}"
systemctl restart "${SERVICE_NAME}"

echo "[5/5] Health check ${HEALTH_URL}"
for ((i=1; i<=HEALTH_RETRIES; i++)); do
  if curl -fsS "${HEALTH_URL}" >/dev/null 2>&1; then
    echo "SUCCESS: ${SERVICE_NAME} is healthy"
    systemctl --no-pager --full status "${SERVICE_NAME}" | head -n 20
    exit 0
  fi
  sleep "${HEALTH_WAIT_SECONDS}"
done

echo "ERROR: health check failed after ${HEALTH_RETRIES} retries"
systemctl --no-pager --full status "${SERVICE_NAME}" || true
journalctl -u "${SERVICE_NAME}" -n 100 --no-pager || true
exit 1
