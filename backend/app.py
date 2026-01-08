import base64
import io
from typing import Dict, List

import matplotlib.patches as patches
import matplotlib.pyplot as plt
import numpy as np
import torch
import uvicorn
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, JSONResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles
from PIL import Image, ImageDraw, ImageFont

from model import LitObjectDetector

app = FastAPI(
    title="Human Detection API",
    description="Upload an image to detect humans with masks and bounding boxes",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

model = None
device = None

CHECKPOINT_PATH = "checkpoints/human-detection-epoch=05-val_loss=0.63.ckpt"
CONFIDENCE_THRESHOLD = 0.88


def load_model():
    global model, device
    device = torch.device("cpu")
    print(f"Loading model on device: {device}")
    model = LitObjectDetector.load_from_checkpoint(
        CHECKPOINT_PATH, num_classes=1, map_location=device
    )
    model = model.to(device)
    model.eval()
    print("Model loaded successfully!")


@app.on_event("startup")
async def startup_event():
    load_model()


@app.get("/")
async def root():
    return {
        "message": "Human Detection API",
        "version": "1.0.0",
        "endpoints": {
            "/detect": "POST - Upload an image for human detection",
            "/health": "GET - Check API health status",
        },
    }


@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "model_loaded": model is not None,
        "device": str(device),
    }


@app.post("/detect")
async def detect_humans(
    file: UploadFile = File(...), confidence: float = CONFIDENCE_THRESHOLD
):
    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    if not 0.0 <= confidence <= 1.0:
        raise HTTPException(
            status_code=400, detail="Confidence must be between 0.0 and 1.0"
        )

    if file.content_type and not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
        image_size = image.size

        predictions = model.predict(image, confidence_threshold=confidence)

        detections = []
        for i in range(predictions["num_people"]):
            box = predictions["boxes"][i]
            score = predictions["scores"][i]
            mask = predictions["masks"][i]

            mask_bytes = io.BytesIO()
            mask_image = Image.fromarray(mask * 255)
            mask_image.save(mask_bytes, format="PNG")
            mask_base64 = base64.b64encode(mask_bytes.getvalue()).decode("utf-8")

            detection = {
                "id": i + 1,
                "confidence": float(score),
                "bounding_box": {
                    "x1": float(box[0]),
                    "y1": float(box[1]),
                    "x2": float(box[2]),
                    "y2": float(box[3]),
                    "width": float(box[2] - box[0]),
                    "height": float(box[3] - box[1]),
                },
                "mask_base64": mask_base64,
            }
            detections.append(detection)

        return JSONResponse(
            {
                "success": True,
                "num_people": predictions["num_people"],
                "detections": detections,
                "image_size": {"width": image_size[0], "height": image_size[1]},
                "confidence_threshold": confidence,
            }
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing image: {str(e)}")


@app.post("/detect/simple")
async def detect_humans_simple(
    file: UploadFile = File(...), confidence: float = CONFIDENCE_THRESHOLD
):
    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    if not 0.0 <= confidence <= 1.0:
        raise HTTPException(
            status_code=400, detail="Confidence must be between 0.0 and 1.0"
        )

    if file.content_type and not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")

        predictions = model.predict(image, confidence_threshold=confidence)

        detections = []
        for i in range(predictions["num_people"]):
            box = predictions["boxes"][i]
            score = predictions["scores"][i]

            detections.append(
                {
                    "id": i + 1,
                    "confidence": float(score),
                    "bounding_box": {
                        "x1": float(box[0]),
                        "y1": float(box[1]),
                        "x2": float(box[2]),
                        "y2": float(box[3]),
                    },
                }
            )

        return JSONResponse(
            {
                "success": True,
                "num_people": predictions["num_people"],
                "detections": detections,
                "confidence_threshold": confidence,
            }
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing image: {str(e)}")


@app.post("/detect/visualize")
async def detect_and_visualize(
    file: UploadFile = File(...), confidence: float = CONFIDENCE_THRESHOLD
):
    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    if not 0.0 <= confidence <= 1.0:
        raise HTTPException(
            status_code=400, detail="Confidence must be between 0.0 and 1.0"
        )

    if file.content_type and not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")

        predictions = model.predict(image, confidence_threshold=confidence)

        fig, ax = plt.subplots(1, figsize=(12, 8))
        ax.imshow(image)

        num_people = predictions["num_people"]

        colors = plt.cm.rainbow(np.linspace(0, 1, max(num_people, 1)))

        for i, (mask, box, score) in enumerate(
            zip(predictions["masks"], predictions["boxes"], predictions["scores"])
        ):
            color = colors[i]

            mask_overlay = np.zeros((*mask.shape, 4))
            mask_overlay[mask > 0] = [*color[:3], 0.5]
            ax.imshow(mask_overlay)

            x1, y1, x2, y2 = box
            rect = patches.Rectangle(
                (x1, y1),
                x2 - x1,
                y2 - y1,
                linewidth=3,
                edgecolor=color,
                facecolor="none",
            )
            ax.add_patch(rect)

            ax.text(
                x1,
                y1 - 10,
                f"Person {i+1}: {score:.2f}",
                bbox=dict(facecolor=color, alpha=0.8, edgecolor="white", linewidth=2),
                fontsize=12,
                color="white",
                fontweight="bold",
            )

        ax.axis("off")
        plt.tight_layout()

        buf = io.BytesIO()
        plt.savefig(buf, format="png", bbox_inches="tight", dpi=150)
        plt.close(fig)
        buf.seek(0)

        return StreamingResponse(buf, media_type="image/png")

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing image: {str(e)}")


@app.get("/app")
async def web_app():
    return FileResponse("static/index.html")


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
