#!/usr/bin/env python3
"""Checks app/src/main/assets/players.json: valid JSON, 500+ unique names, known tags."""
import json
import pathlib
import sys

PATH = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/assets/players.json"
TAGS = {"current", "premier_league_legend", "world_legend", "england", "difficult"}

data = json.loads(PATH.read_text(encoding="utf-8"))
players = data["players"]
names = [p["name"].strip() for p in players]
errors = []
if len(set(n.casefold() for n in names)) != len(names):
    errors.append("duplicate names found")
if len(names) < 500:
    errors.append(f"only {len(names)} players, need at least 500")
for p in players:
    unknown = set(p.get("tags", [])) - TAGS
    if unknown:
        errors.append(f"{p['name']}: unknown tags {sorted(unknown)}")
for tag in sorted(TAGS):
    count = sum(tag in p.get("tags", []) for p in players)
    print(f"{tag:22} {count}")
    if count < 50:
        errors.append(f"category {tag} has only {count} players")
print(f"{'total':22} {len(names)}")
if errors:
    print("\n".join(errors), file=sys.stderr)
    sys.exit(1)
