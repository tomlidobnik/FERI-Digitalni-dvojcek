Naredi direktorij checkpoints in naloži ta (model)[https://huggingface.co/tomlidobnik/human-detection/blob/main/human-detection-epoch%3D05-val_loss%3D0.63.ckpt]
Naloži model preko (spleta)[https://huggingface.co/tomlidobnik/human-detection/tree/main].
**Run app with:** uv run uvicorn app:app --reload

Za učenje je bil uporabljen ta (dataset)[https://www.kaggle.com/datasets/fareselmenshawii/human-dataset]

To run:

```
uv run python -m uvicorn app:app --host 0.0.0.0 --port 8000
```

Seed sample events:

```
python3 backend/scripts/seed_events.py --count 25 --public-ratio 0.6 --with-locations --seed 42
```
