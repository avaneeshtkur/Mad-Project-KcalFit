#!/usr/bin/env python3
"""Download and compact the public Kaggle MyFitnessPal dataset.

Usage:
    python3 scripts/download_food_dataset.py

If Kaggle asks for authentication, export KAGGLE_USERNAME and KAGGLE_KEY
before running this script. The output is written to app/src/main/assets/foods.csv.
"""

from __future__ import annotations

import csv
import io
import os
import sys
import tempfile
import zipfile
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen


DOWNLOAD_URL = "https://www.kaggle.com/api/v1/datasets/download/zvikinozadze/myfitnesspal-dataset"
OUTPUT_PATH = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "assets" / "foods.csv"
LIMIT = 5_000
CHUNK_SIZE = 1024 * 1024


def download_to_file() -> Path:
    headers = {"User-Agent": "KCalFit food dataset downloader"}
    username = os.getenv("KAGGLE_USERNAME")
    api_key = os.getenv("KAGGLE_KEY")
    if username and api_key:
        import base64
        token = base64.b64encode(f"{username}:{api_key}".encode()).decode()
        headers["Authorization"] = f"Basic {token}"

    request = Request(DOWNLOAD_URL, headers=headers)
    try:
        with urlopen(request) as response:
            temp = tempfile.NamedTemporaryFile(prefix="kcalfit-foods-", suffix=".zip", delete=False)
            temp_path = Path(temp.name)
            with temp:
                while True:
                    chunk = response.read(CHUNK_SIZE)
                    if not chunk:
                        break
                    temp.write(chunk)
            return temp_path
    except HTTPError as error:
        if error.code in (401, 403):
            raise RuntimeError(
                "Kaggle requires credentials. Export KAGGLE_USERNAME and KAGGLE_KEY, then retry."
            ) from error
        raise RuntimeError(f"Kaggle download failed with HTTP {error.code}") from error


def first_matching(row: dict[str, str], names: tuple[str, ...]) -> str:
    normalized = {key.strip().lower(): value for key, value in row.items() if key}
    for name in names:
        if name in normalized:
            return normalized[name].strip()
    return ""


def number(value: str) -> str:
    value = value.strip().replace(",", "")
    try:
        return str(round(float(value), 2))
    except (TypeError, ValueError):
        return "0"


def locate_csv(archive: zipfile.ZipFile) -> str:
    candidates = [name for name in archive.namelist() if name.lower().endswith(".csv")]
    if not candidates:
        raise RuntimeError("The Kaggle ZIP did not contain a CSV file.")
    return candidates[0]


def main() -> None:
    print(f"Downloading dataset from {DOWNLOAD_URL}")
    compressed = download_to_file()
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)

    try:
        with zipfile.ZipFile(compressed) as archive:
            csv_name = locate_csv(archive)
            print(f"Reading {csv_name}")
            with archive.open(csv_name, "r") as raw_file, OUTPUT_PATH.open("w", encoding="utf-8", newline="") as output_file:
                text_file = io.TextIOWrapper(raw_file, encoding="utf-8-sig", errors="replace", newline="")
                reader = csv.DictReader(text_file)
                writer = csv.DictWriter(
                    output_file,
                    fieldnames=["food_name", "calories", "protein", "carbs", "fat"],
                    quoting=csv.QUOTE_MINIMAL,
                )
                writer.writeheader()
                count = 0
                for source_row in reader:
                    food_name = first_matching(source_row, ("food_name", "food", "name", "food name"))
                    if not food_name:
                        continue
                    writer.writerow({
                        "food_name": food_name,
                        "calories": number(first_matching(source_row, ("calories", "calorie", "kcal"))),
                        "protein": number(first_matching(source_row, ("protein", "protein_g", "protein (g)"))),
                        "carbs": number(first_matching(source_row, ("carbs", "carbohydrates", "carbohydrate", "carbs (g)"))),
                        "fat": number(first_matching(source_row, ("fat", "fat_g", "fat (g)"))),
                    })
                    count += 1
                    if count >= LIMIT:
                        break
    finally:
        compressed.unlink(missing_ok=True)

    print(f"Saved {count:,} rows to {OUTPUT_PATH}")


if __name__ == "__main__":
    try:
        main()
    except (OSError, zipfile.BadZipFile, RuntimeError) as error:
        print(f"Error: {error}", file=sys.stderr)
        sys.exit(1)
