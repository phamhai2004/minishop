from PIL import Image

import open_clip
import torch


class EmbeddingService:

    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"

        self.model = None
        self.preprocess = None

    def _load_model(self):
        if self.model is not None and self.preprocess is not None:
            return

        self.model, _, self.preprocess = open_clip.create_model_and_transforms(
            "ViT-B-32",
            pretrained="laion2b_s34b_b79k",
        )

        self.model.to(self.device)
        self.model.eval()

    def generate_embedding(self, image: Image.Image):
        """
        Sinh embedding từ đối tượng PIL.Image
        """

        self._load_model()

        image = image.convert("RGB")

        image_tensor = (
            self.preprocess(image)
            .unsqueeze(0)
            .to(self.device)
        )

        with torch.no_grad():
            embedding = self.model.encode_image(image_tensor)

            embedding /= embedding.norm(dim=-1, keepdim=Tzrue)

        return embedding.squeeze().cpu().tolist()