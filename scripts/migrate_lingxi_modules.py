#!/usr/bin/env python3
"""Migrate lingxi biz modules into cloud-mall (monolith, no Feign, controllers -> mall-web)."""
from __future__ import annotations

import os
import re
import shutil
from pathlib import Path

LINGXI = Path("/Users/xiaobai/Deveploer/workspace/company/lingxi/JeecgBoot/jeecg-boot")
MALL = Path("/Users/xiaobai/Deveploer/workspace/sass-mall/cloud-mall")

# module_key -> (src_biz_root, dest_module, also_copy_api_non_feign)
MODULES = {
    "media": (
        LINGXI / "jeecg-boot-module/jeecg-module-media/src/main/java",
        MALL / "mall-modules/mall-media/src/main/java",
        None,
    ),
    "user": (
        LINGXI / "jeecg-module-user/jeecg-module-user-biz/src/main/java",
        MALL / "mall-modules/mall-user/src/main/java",
        None,
    ),
    "cuisine": (
        LINGXI / "jeecg-module-cuisine/jeecg-module-cuisine-biz/src/main/java",
        MALL / "mall-modules/mall-cuisine/src/main/java",
        None,
    ),
    "search": (
        LINGXI / "jeecg-module-search/jeecg-module-search-biz/src/main/java",
        MALL / "mall-modules/mall-search/src/main/java",
        None,
    ),
    "ai": (
        LINGXI / "jeecg-module-ai/jeecg-module-ai-biz/src/main/java",
        MALL / "mall-modules/mall-ai/src/main/java",
        LINGXI / "jeecg-module-ai/jeecg-module-ai-api/src/main/java",
    ),
    "interaction": (
        LINGXI / "jeecg-module-interaction/jeecg-module-interaction-biz/src/main/java",
        MALL / "mall-modules/mall-interaction/src/main/java",
        LINGXI / "jeecg-module-interaction/jeecg-module-interaction-api/src/main/java",
    ),
    "member": (
        LINGXI / "jeecg-module-member/jeecg-module-member-biz/src/main/java",
        MALL / "mall-modules/mall-member/src/main/java",
        LINGXI / "jeecg-module-member/jeecg-module-member-api/src/main/java",
    ),
    "support": (
        LINGXI / "jeecg-module-support/jeecg-module-support-biz/src/main/java",
        MALL / "mall-modules/mall-support/src/main/java",
        LINGXI / "jeecg-module-support/jeecg-module-support-api/src/main/java",
    ),
}

WEB_JAVA = MALL / "mall-web/src/main/java"
RES_MAP = {
    "user": LINGXI / "jeecg-module-user/jeecg-module-user-biz/src/main/resources",
    "cuisine": LINGXI / "jeecg-module-cuisine/jeecg-module-cuisine-biz/src/main/resources",
    "ai": LINGXI / "jeecg-module-ai/jeecg-module-ai-biz/src/main/resources",
    "search": LINGXI / "jeecg-module-search/jeecg-module-search-biz/src/main/resources",
    "interaction": LINGXI / "jeecg-module-interaction/jeecg-module-interaction-biz/src/main/resources",
    "member": LINGXI / "jeecg-module-member/jeecg-module-member-biz/src/main/resources",
    "support": LINGXI / "jeecg-module-support/jeecg-module-support-biz/src/main/resources",
    "media": LINGXI / "jeecg-boot-module/jeecg-module-media/src/main/resources",
}

FEIGN_NAME_RE = re.compile(r"FeignClient|Feign")
CONTROLLER_RE = re.compile(r"Controller\.java$")
SKIP_FILE_RE = re.compile(
    r"(FeignClient|FeignAutoConfiguration|cloud\.openfeign)",
    re.I,
)


def is_controller(path: Path) -> bool:
    return "controller" in path.parts and path.name.endswith("Controller.java")


def is_feign_file(path: Path) -> bool:
    name = path.name
    if "FeignClient" in name or name.endswith("Feign.java"):
        return True
    if path.suffix != ".java":
        return False
    try:
        text = path.read_text(encoding="utf-8", errors="ignore")
    except Exception:
        return False
    return "@FeignClient" in text or "spring.cloud.openfeign" in text


def is_feign_auth_config(path: Path) -> bool:
    """Auth configs that only wire Feign — skip, we rewrite in mall-web."""
    skip_names = {
        "AiUserAuthConfiguration.java",
        "InteractionAuthAutoConfiguration.java",
        "MemberAuthAutoConfiguration.java",
    }
    return path.name in skip_names


def copy_java_tree(src_root: Path, dest_root: Path, to_web: bool) -> tuple[int, int]:
    copied = skipped = 0
    if not src_root.exists():
        print(f"  missing: {src_root}")
        return 0, 0
    for src in src_root.rglob("*"):
        if not src.is_file():
            continue
        rel = src.relative_to(src_root)
        # resources under java? skip
        if src.suffix not in {".java", ".xml", ".yml", ".yaml", ".properties", ".factories", ".imports"}:
            # allow mapper xml only under resources path handled separately
            if src.suffix == ".xml" and "mapper" in str(rel).lower():
                pass
            elif src.suffix not in {".java"}:
                continue

        if src.suffix == ".java":
            if is_feign_file(src) or is_feign_auth_config(src):
                skipped += 1
                continue
            ctrl = is_controller(src)
            if to_web:
                # only controllers
                if not ctrl:
                    continue
                dest = WEB_JAVA / rel
            else:
                if ctrl:
                    continue
                dest = dest_root / rel
        else:
            if to_web:
                continue
            dest = dest_root / rel

        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dest)
        copied += 1
    return copied, skipped


def copy_resources(key: str, dest_module: Path) -> int:
    src = RES_MAP.get(key)
    if not src or not src.exists():
        return 0
    dest = dest_module / "src/main/resources"
    n = 0
    for f in src.rglob("*"):
        if not f.is_file():
            continue
        # skip spring cloud / bootstrap
        if f.name.startswith("bootstrap") or "nacos" in f.name.lower():
            continue
        rel = f.relative_to(src)
        target = dest / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(f, target)
        n += 1
    return n


def main():
    # ensure mall-media exists
    media_pom_dir = MALL / "mall-modules/mall-media"
    media_pom_dir.mkdir(parents=True, exist_ok=True)

    for key, (biz_src, dest_java, api_src) in MODULES.items():
        dest_module = dest_java.parents[2] if dest_java.name == "java" else dest_java
        # dest_java is .../src/main/java
        print(f"== {key} ==")
        # clean previous java (keep pom)
        if dest_java.exists():
            shutil.rmtree(dest_java)
        dest_java.mkdir(parents=True, exist_ok=True)

        c, s = copy_java_tree(biz_src, dest_java, to_web=False)
        print(f"  biz non-controller: {c}, skipped feign/auth: {s}")
        c2, s2 = copy_java_tree(biz_src, WEB_JAVA, to_web=True)
        print(f"  controllers -> mall-web: {c2}")

        if api_src and api_src.exists():
            # copy non-feign api interfaces into module
            ca, sa = copy_java_tree(api_src, dest_java, to_web=False)
            print(f"  api non-feign: {ca}, skipped: {sa}")

        rn = copy_resources(key, dest_module)
        print(f"  resources: {rn}")

    print("DONE")


if __name__ == "__main__":
    main()
