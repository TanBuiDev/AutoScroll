# Auto Scroll branding

Source: https://www.figma.com/design/eeN7ZElqAUWoF0tsC3pDBe

- `3:9`: master icon, original SVG and configured 1024×1024 PNG export.
- `3:27`: Google Play icon, original SVG and configured 512×512 PNG export.
- `3:67`: splash artwork, original SVG and configured 210×210 PNG export.
- `3:66`: splash layout implemented in `BrandSplash.kt`, horizontal #F7FBFF → #D6EDFF gradient, Inter SemiBold title #0A2661.

Downloaded artwork is retained locally. The splash SVG is rasterized at 840×840 for sharper rendering on high-density devices without changing the artwork. Launcher resources use proportional density scaling of the master export and a centered 72dp foreground inside the 108dp adaptive icon canvas. The Android system applies its launcher mask; the guides in the Figma safe-zone example are not artwork.

The Google Play PNG is a store asset, not a launcher resource. No store publishing is performed. Inter is bundled with its SIL Open Font License in Inter-OFL.txt.

Implementation verification: debug build succeeded; installation and launch succeeded on the connected Android 16 phone. The on-device splash screenshot was inspected for artwork, gradient, title and placement. Both store export dimensions and all density launcher resource dimensions were checked. Android supplies its own constrained launch screen before the 650ms Compose launch treatment. No automated tests were run and no Google Play publication was performed.
