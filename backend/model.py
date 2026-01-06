import pytorch_lightning as pl
import torch
import torchvision
from torchvision.models.detection import MaskRCNN_ResNet50_FPN_Weights
from torchvision.models.detection.faster_rcnn import FastRCNNPredictor
from torchvision.models.detection.mask_rcnn import MaskRCNNPredictor

class LitObjectDetector(pl.LightningModule):
    def __init__(self, num_classes: int = 1, *, weights: MaskRCNN_ResNet50_FPN_Weights | None = MaskRCNN_ResNet50_FPN_Weights.DEFAULT):
        super().__init__()
        self.model = torchvision.models.detection.maskrcnn_resnet50_fpn(weights=weights)

        in_features = self.model.roi_heads.box_predictor.cls_score.in_features
        self.model.roi_heads.box_predictor = FastRCNNPredictor(in_features, num_classes + 1)

        in_features_mask = self.model.roi_heads.mask_predictor.conv5_mask.in_channels
        hidden_layer = 256
        self.model.roi_heads.mask_predictor = MaskRCNNPredictor(in_features_mask, hidden_layer, num_classes + 1)

    def training_step(self, batch, batch_idx):
        images, targets = batch
        targets = [{k: v for k, v in t.items()} for t in targets]

        loss_dict = self.model(images, targets)
        losses = sum(loss for loss in loss_dict.values())

        self.log('train_loss', losses, on_step=True, on_epoch=True, prog_bar=True, logger=True)
        return losses

    def validation_step(self, batch, batch_idx):
        images, targets = batch
        targets = [{k: v for k, v in t.items()} for t in targets]

        self.model.train()
        loss_dict = self.model(images, targets)
        losses = sum(loss for loss in loss_dict.values())

        self.log('val_loss', losses, on_step=True, on_epoch=True, prog_bar=True, logger=True)
        return losses

    def configure_optimizers(self):
        optimizer = torch.optim.SGD(self.parameters(), lr=0.005, momentum=0.9, weight_decay=0.0005)
        return optimizer

    def predict(self, image, confidence_threshold=0.5):
        self.model.eval()

        if not isinstance(image, torch.Tensor):
            from torchvision import transforms
            transform = transforms.ToTensor()
            image = transform(image)

        image = image.unsqueeze(0).to(self.device)

        with torch.no_grad():
            predictions = self.model(image)[0]

        mask = (predictions['scores'] > confidence_threshold) & (predictions['labels'] == 1)

        num_people = mask.sum().item()
        masks = []
        boxes = []
        scores = []

        if num_people > 0:
            pred_masks = predictions['masks'][mask].squeeze(1).cpu().numpy()
            masks = [(m > 0.5).astype('uint8') for m in pred_masks]

            boxes = predictions['boxes'][mask].cpu().numpy().tolist()
            scores = predictions['scores'][mask].cpu().numpy().tolist()

        return {
            'num_people': num_people,
            'masks': masks,
            'boxes': boxes,
            'scores': scores
        }

