#!/usr/bin/env python3
"""Snapshot public/protected JVM signatures without compiler implementation noise."""

import argparse
import re
import struct
import subprocess
from pathlib import Path


def class_access_offset(data):
    """Locate access_flags after the JVM constant pool (including two-slot entries)."""
    if data[:4] != b"\xca\xfe\xba\xbe":
        raise ValueError("Invalid JVM class file")
    count = struct.unpack_from(">H", data, 8)[0]
    offset, index = 10, 1
    sizes = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
             11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
    while index < count:
        tag = data[offset]
        offset += 1
        if tag == 1:
            length = struct.unpack_from(">H", data, offset)[0]
            offset += 2 + length
        elif tag in sizes:
            offset += sizes[tag]
        else:
            raise ValueError(f"Unknown JVM constant pool tag: {tag}")
        index += 2 if tag in (5, 6) else 1
    return offset


def class_names(directories):
    names = set()
    for directory in directories:
        for path in Path(directory).rglob("*.class"):
            data = path.read_bytes()
            flags = struct.unpack_from(">H", data, class_access_offset(data))[0]
            if not flags & 0x0001 or flags & 0x1000:  # ACC_PUBLIC / ACC_SYNTHETIC
                continue
            name = ".".join(path.relative_to(directory).with_suffix("").parts)
            simple = name.rsplit(".", 1)[-1]
            if simple in ("R", "BuildConfig") or simple.startswith("R$"):
                continue
            if simple.startswith("ComposableSingletons$"):
                continue
            if re.search(r"\$\d+(?:\$|$)", simple) or "$$inlined$" in simple:
                continue
            names.add(name)
    return sorted(names)


def snapshot(classpath, directories):
    names = class_names(directories)
    if not names:
        raise ValueError("No public release classes found")
    result = subprocess.run(
        ["javap", "-protected", "-classpath", classpath, *names],
        check=True, capture_output=True, text=True,
    )
    # Keep $default and public named nested classes: callers can depend on them.
    # access$ methods bridge private implementation code for generated lambdas.
    lines = [line for line in result.stdout.splitlines()
             if not line.startswith('Compiled from "')
             and not re.search(r"\baccess\$", line)]
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--classpath", required=True)
    parser.add_argument("--out", required=True, type=Path)
    parser.add_argument("directories", nargs="+", type=Path)
    args = parser.parse_args()
    result = snapshot(args.classpath, args.directories)
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(result)


if __name__ == "__main__":
    main()
