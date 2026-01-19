import pytorch_lightning as pl
from pytorch_lightning.callbacks import ModelCheckpoint, LearningRateMonitor
from model import LitObjectDetector
import torch
from torch.utils.data import DataLoader, Dataset
from torchvision import transforms
from PIL import Image
import os
import yaml


def collate_fn(batch):
    return tuple(zip(*batch))


class HumanDataset(Dataset):
    def __init__(self, image_dir, label_dir, transform=None):
        self.image_dir = image_dir
        self.label_dir = label_dir
        self.transform = transform
        self.image_filenames = [f for f in os.listdir(image_dir) if f.endswith('.jpg')]

    def __len__(self):
        return len(self.image_filenames)

    def __getitem__(self, idx):
        img_name = self.image_filenames[idx]
        img_path = os.path.join(self.image_dir, img_name)
        image = Image.open(img_path).convert("RGB")

        original_width, original_height = image.size

        label_name = img_name.replace('.jpg', '.txt')
        label_path = os.path.join(self.label_dir, label_name)

        boxes = []
        labels_list = []
        if os.path.exists(label_path):
            with open(label_path, 'r') as f:
                for line in f.readlines():
                    parts = list(map(float, line.strip().split()))
                    class_id = int(parts[0])
                    x_center, y_center, width, height = parts[1:]

                    x_center *= original_width
                    y_center *= original_height
                    width *= original_width
                    height *= original_height

                    x_min = x_center - width / 2
                    y_min = y_center - height / 2
                    x_max = x_center + width / 2
                    y_max = y_center + height / 2

                    boxes.append([x_min, y_min, x_max, y_max])
                    labels_list.append(class_id + 1)

        if self.transform:
            image = self.transform(image)

        if image.dim() == 3:
            new_height, new_width = image.shape[1], image.shape[2]
        else:
            new_height, new_width = original_height, original_width

        if boxes:
            scale_x = new_width / original_width
            scale_y = new_height / original_height
            scaled_boxes = []
            masks = []
            for box in boxes:
                x_min, y_min, x_max, y_max = box
                scaled_x_min = x_min * scale_x
                scaled_y_min = y_min * scale_y
                scaled_x_max = x_max * scale_x
                scaled_y_max = y_max * scale_y
                scaled_boxes.append([scaled_x_min, scaled_y_min, scaled_x_max, scaled_y_max])

                mask = torch.zeros((new_height, new_width), dtype=torch.uint8)
                x1 = int(max(0, scaled_x_min))
                y1 = int(max(0, scaled_y_min))
                x2 = int(min(new_width, scaled_x_max))
                y2 = int(min(new_height, scaled_y_max))
                mask[y1:y2, x1:x2] = 1
                masks.append(mask)

            target = {
                "boxes": torch.tensor(scaled_boxes, dtype=torch.float32),
                "labels": torch.tensor(labels_list, dtype=torch.int64),
                "masks": torch.stack(masks)
            }
        else:
            target = {
                "boxes": torch.zeros((0, 4), dtype=torch.float32),
                "labels": torch.zeros((0,), dtype=torch.int64),
                "masks": torch.zeros((0, new_height, new_width), dtype=torch.uint8)
            }

        return image, target


class HumanDataModule(pl.LightningDataModule):
    def __init__(self, data_dir: str = "./", batch_size: int = 32, num_workers: int = 11):
        super().__init__()
        self.data_dir = data_dir
        self.batch_size = batch_size
        self.num_workers = num_workers
        self.transform = transforms.Compose([
            transforms.Resize((640, 640)),
            transforms.ToTensor(),
        ])

        with open(os.path.join(data_dir, 'data.yaml'), 'r') as f:
            self.data_config = yaml.safe_load(f)

    def setup(self, stage=None):
        if stage == "fit" or stage is None:
            self.train_dataset = HumanDataset(
                image_dir=os.path.join(self.data_dir, self.data_config['train']),
                label_dir=os.path.join(self.data_dir, self.data_config['labels']['train']),
                transform=self.transform
            )
            self.val_dataset = HumanDataset(
                image_dir=os.path.join(self.data_dir, self.data_config['val']),
                label_dir=os.path.join(self.data_dir, self.data_config['labels']['val']),
                transform=self.transform
            )

    def train_dataloader(self):
        return DataLoader(
            self.train_dataset,
            batch_size=self.batch_size,
            shuffle=True,
            num_workers=self.num_workers,
            collate_fn=collate_fn,
            persistent_workers=self.num_workers > 0,
            pin_memory=False,
        )

    def val_dataloader(self):
        return DataLoader(
            self.val_dataset,
            batch_size=self.batch_size,
            shuffle=False,
            num_workers=self.num_workers,
            collate_fn=collate_fn,
            persistent_workers=self.num_workers > 0,
            pin_memory=False,
        )


def main():
    data_module = HumanDataModule(
        data_dir="human-dataset",
        batch_size=4,
        num_workers=11
    )

    model = LitObjectDetector(num_classes=1)

    checkpoint_callback = ModelCheckpoint(
        dirpath='checkpoints',
        filename='human-detection-{epoch:02d}-{val_loss:.2f}',
        save_top_k=-1,
        every_n_epochs=1,
    )

    lr_monitor = LearningRateMonitor(logging_interval='epoch')

    trainer = pl.Trainer(
        max_epochs=10,
        accelerator='cpu',
        devices=1,
        callbacks=[checkpoint_callback, lr_monitor],
        log_every_n_steps=10,
        logger=pl.loggers.TensorBoardLogger("lightning_logs/", name="human_detection"),
    )

    trainer.fit(model, data_module)

    print("\nTraining complete!")
    print(f"Best checkpoint: {checkpoint_callback.best_model_path}")


if __name__ == '__main__':
    main()
