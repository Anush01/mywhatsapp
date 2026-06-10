# MyWhatsapp

> *Because WhatsApp was never meant to be your notebook.*

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-minSdk%2024-3DDC84?logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.02-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## The Story

Years ago I started using WhatsApp as my primary note-taker. Quick thoughts, links, reminders, ideas — all sent to myself in a WhatsApp chat. It was frictionless, always in my pocket, and already open.

Then I hit the wall.

Exporting more than ~30,000 messages from WhatsApp is effectively impossible. My notes were stuck. Luckily I'd been backing up periodically using WhatsApp's "Export Chat" feature, so nothing was lost — but the experience made one thing clear: I was building years of personal knowledge on a platform that didn't want me to leave.

So I built this instead.

**MyWhatsapp** is a self-messaging Android app that looks and feels exactly like WhatsApp's chat screen, but with one critical difference: you own your data, completely. No export limits. No platform lock-in. Scales to 1M+ messages, exports and imports flawlessly.

---

## Features

- **Familiar UI** — WhatsApp-style chat screen, message bubbles, top bar and all. Zero learning curve.
- **Persistent storage** — Room DB with Paging 3; only visible messages are loaded, no matter how many you have.
- **Streaming export / import** — Export your entire history to a JSON file and import it back, even at 1M+ messages, without the app ever loading it all into memory.
- **No storage permissions** — Uses Android's Storage Access Framework; the OS picks the file, not the app.
- **Dark & light themes** — WhatsApp's exact colour palette mapped to Material 3 roles. Respects system dark mode.
- **Scales where WhatsApp can't** — Cursor-based DB pagination means export speed stays flat whether you have 1,000 or 1,000,000 messages.

---

## Why It Scales

WhatsApp's export breaks beyond ~30k messages because it loads everything into memory at once. This app never does that.

| Problem | Solution |
|---|---|
| Reading entire message history into RAM | `JsonWriter` / `JsonReader` stream one message at a time |
| `OFFSET`-based DB queries (O(n²) at scale) | Cursor-based: `WHERE id > lastId ORDER BY id LIMIT 500` |
| Slow bulk imports | 500-row batch inserts inside a single transaction |

The export file is forward-compatible — a `version` field lets future imports detect and handle schema changes gracefully.

```json
{
  "version": 1,
  "exportedAt": 1749562800000,
  "messages": [
    { "id": 1, "content": "first note", "timestamp": 1749562800000 },
    ...
  ]
}
```

---

<details>
<summary><strong>Tech Stack</strong></summary>

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.2.10 |
| UI | Jetpack Compose BOM | 2026.02.01 |
| UI | Material 3 | via BOM |
| Database | Room | 2.7.1 |
| Pagination | Paging 3 + Paging Compose | 3.3.6 |
| Serialisation | Gson | 2.11.0 |
| Annotation processing | KSP | 2.2.10-2.0.2 |
| Build | AGP | 9.2.1 |
| Min SDK | — | 24 (Android 7.0) |

</details>

---

## Building

**Prerequisites:** Android Studio (Meerkat or later), JDK 17+

```bash
git clone https://github.com/Anush01/mywhatsapp.git
cd mywhatsapp
./gradlew assembleDebug
```

Install on a connected device or emulator:

```bash
./gradlew installDebug
```

---

## Migrating from WhatsApp

If you have existing WhatsApp self-chat backups (`.txt` exports), you'll need a small conversion script to turn them into the JSON format above. That's on the roadmap.

For fresh starts — just install and go.

---

## License

MIT — do whatever you want with it.
