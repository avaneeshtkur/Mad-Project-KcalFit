# Nakafit design system observations

Source page `🖼 Cover` (`0:1`), screen PNGs `2513:7`–`2513:14`, `2513:17`, `2513:18`, and `2513:22`.

The 375 × 812 references show a white `#FFFFFF` base, nearly black `#101014` headings and dark CTA surfaces, vivid violet approximately `#6500F5` for active controls and primary actions, grey-blue approximately `#808BA3` for secondary labels, lavender approximately `#E0CEFF`, and pale lime approximately `#F1FFD2`. These values are sampled visual approximations because Figma provides only flattened PNG fills.

Observed patterns: 16 px horizontal outer margins; 20–24 px headline text; 14–16 px body text; 12 px secondary captions; large pill buttons and chips; cards with roughly 20–24 px rounded corners; hairline pale grey borders; white modal sheets with about 24 px corners; 48–56 px primary buttons. Home uses a compact floating black navigation pill with a purple active circle. Macro indicators use partial colored rings. The screen images include simulated iOS status bars and home indicators; native Android system bars should be managed by the app rather than recreated as bitmap chrome.

No font file or named type style was provided. The existing project uses `FontFamily.Default`. Original photographs and icons are embedded in flattened screen images and cannot be used independently without extracting/editing those images, so any Compose recreation must document visual gaps.
