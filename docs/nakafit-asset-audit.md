# Nakafit supplied asset audit

All six source files are JPEGs and were visually inspected. Original files remain unchanged in `images/`. Android copies are stored in `app/src/main/res/drawable-nodpi/`.

| Original path | Dimensions | Visual content | Android resource | Intended UI use | Render verification |
| --- | --- | --- | --- | --- | --- |
| images/Aesthetic Gym.jpeg | 736 × 1307 | Gym at sunset, equipment and windows | nakafit_gym_sunset | Welcome, gym workout detail | copied; statically validated |
| images/49891508370684824.jpeg | 402 × 604 | Person meditating beside a lake | nakafit_meditation | Yoga/mindfulness, sleep/recovery | copied; statically validated |
| images/London, UK _ Gianni Pagnotta.jpeg | 736 × 1030 | Runner on an athletics track | nakafit_runner | Running card, onboarding, active exercise, login cover | copied; statically validated |
| images/Best Amazon Gym Essentials You Need to See in 2026.jpeg | 736 × 1307 | Strength gym with dumbbells | nakafit_strength | Strength cards, workout library/details | copied; statically validated |
| images/fitness food.jpeg | 736 × 1104 | Chicken, avocado, quinoa and vegetables | nakafit_food | Nutrition hero, meal photo/detail | copied; statically validated |
| images/Es ist immer du gegen dich selber, niemand anderes….jpeg | 736 × 991 | Gym interior with YOU vs YOU signage | nakafit_gym_motivation | Registration and training-plan hero | copied; statically validated |

Photography slots use `Image(painterResource(...), contentScale = ContentScale.Crop)` with constrained card/hero geometry. These are the user's supplied alternatives to the reference photographs. No coach portrait, isolated exercise person cutout, wearable-device product photo, or dedicated meal thumbnail was supplied; relevant available photos are used where appropriate, and identities are represented with initials rather than invented headshots.

Static resource/build validation is complete. Manual device rendering verification remains for the user's own verification pass, as requested.
