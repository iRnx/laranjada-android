from pathlib import Path

ROOT = Path(".").resolve()
OUTPUT_FILE = ROOT / "estrutura-android-atual.txt"

IGNORE_DIRS = {
    ".git",
    ".gradle",
    ".idea",
    ".vscode",
    "build",
    "captures",
    ".kotlin",
    "out",
    "generated",
    "__pycache__",
}

IGNORE_FILES = {
    ".DS_Store",
    "estrutura-android-atual.txt",
    "arquivos-android-api-colecao.txt",
}

def should_ignore(path: Path) -> bool:
    name = path.name

    if name in IGNORE_FILES:
        return True

    if path.is_dir() and name in IGNORE_DIRS:
        return True

    return any(part in IGNORE_DIRS for part in path.parts)

def build_tree(directory: Path, prefix: str = "") -> list[str]:
    lines = []

    try:
        items = [
            item for item in directory.iterdir()
            if not should_ignore(item)
        ]
    except PermissionError:
        return lines

    items.sort(key=lambda item: (not item.is_dir(), item.name.lower()))

    for index, item in enumerate(items):
        is_last = index == len(items) - 1
        connector = "└── " if is_last else "├── "

        lines.append(f"{prefix}{connector}{item.name}")

        if item.is_dir():
            extension = "    " if is_last else "│   "
            lines.extend(build_tree(item, prefix + extension))

    return lines

lines = [ROOT.name]
lines.extend(build_tree(ROOT))

OUTPUT_FILE.write_text("\n".join(lines), encoding="utf-8")

print(f"Arquivo gerado: {OUTPUT_FILE}")
