from fastapi import FastAPI, Request, HTTPException, Header
from slowapi import Limiter
from slowapi.util import get_remote_address
from slowapi.middleware import SlowAPIMiddleware
from dotenv import load_dotenv
import hmac
import hashlib
import os
import logging
import subprocess

load_dotenv()

app = FastAPI()

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
)

limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter
app.add_middleware(SlowAPIMiddleware)

WEBHOOK_SECRET = os.getenv("GITHUB_WEBHOOK_SECRET")

COMMANDS = """
if [ ! -d "/home/pi/FERI-Digitalni-dvojcek" ]; then
    git clone git@github.com:tomlidobnik/FERI-Digitalni-dvojcek.git /home/pi/FERI-Digitalni-dvojcek
fi

cd /home/pi/FERI-Digitalni-dvojcek
git switch develop
git fetch origin
git reset --hard origin/develop

cd ./backend
mkcert -install
mkcert -key-file key.pem -cert-file cert.pem tom.thehomeserver.net
cd ..

docker pull tomlidobnik/backend:prod
docker pull tomlidobnik/frontend:prod

docker-compose down
docker-compose up -d --build db backend-prod frontend-prod
"""


def run_deployment(commands: str):
    try:
        logging.info("🚀 Starting deployment process...")
        result = subprocess.run(
            commands, shell=True, check=True, text=True, capture_output=True
        )
        logging.info(f"✅ Deployment successful:\n{result.stdout}")
    except subprocess.CalledProcessError as e:
        logging.error(f"❌ Deployment failed:\n{e.stderr}")
        raise


@app.post("/secretkey")
@limiter.limit("5/minute")
async def update(request: Request):
    headers = request.headers
    x_hub_signature_256 = headers.get(
        "X-Hub-Signature-256", headers.get("x-hub-signature-256")
    )
    logging.info(f"🔑 Secret loaded: {WEBHOOK_SECRET}")
    logging.info(f"🔏 Received signature: {x_hub_signature_256}")

    if not x_hub_signature_256 or not WEBHOOK_SECRET:
        raise HTTPException(403, "Missing GitHub signature or secret")

    if not hmac.compare_digest(x_hub_signature_256, WEBHOOK_SECRET):
        raise HTTPException(403, "Invalid GitHub signature")

    try:
        run_deployment(COMMANDS)

        return {"message": "Webhook processed successfully."}

    except Exception as e:
        logging.error(f"Failed to process webhook: {e}")
        raise HTTPException(status_code=500, detail="Internal Server Error")
