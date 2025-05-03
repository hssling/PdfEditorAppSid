# PdfEditorApp

[![Android CI](https://github.com/hssling/PdfEditorAppSid/actions/workflows/android.yml/badge.svg)](https://github.com/hssling/PdfEditorAppSid/actions/workflows/android.yml)
[![Release APK](https://github.com/hssling/PdfEditorAppSid/actions/workflows/release.yml/badge.svg)](https://github.com/hssling/PdfEditorAppSid/actions/workflows/release.yml)

An Android app to read, edit, and convert PDF files from scans and other sources, with the ability to edit any aspect of the PDF (text, images, annotations, etc).

---

## Features

- **Read and display PDF files**  
  Open and view any PDF with fast, accessible navigation.
- **Edit PDF Content**
  - Edit text, images, and annotations directly in PDFs.
  - Add, remove, or modify text and images on any page.
  - Draw and insert digital signatures.
- **Batch Processing**
  - Merge multiple PDFs into a single document.
  - Split a PDF by page range into multiple files.
- **Scan to PDF**
  - Capture images using your device camera (CameraX) and convert them to PDF.
- **Accessibility**
  - Content descriptions for images and interactive elements.
  - Global dark/light theme toggle.
  - Font scaling and support for screen readers.
- **Security**
  - Set and remove passwords for PDF files.
  - All file operations include robust error handling and user-friendly messages.
- **Modern UI**
  - Built with Jetpack Compose for a responsive, accessible interface.
  - Navigation drawer and settings for easy access to all features.

---

## Screenshots

Add screenshots to `docs/screenshots/` and reference them here:

![Home Screen](docs/screenshots/home.png)
![PDF Editing](docs/screenshots/edit.png)
![Scan to PDF](docs/screenshots/scan.png)

---

## Tech Stack

- **Kotlin**
- **Jetpack Compose** (UI)
- **PdfBox-Android** (PDF manipulation)
- **CameraX** (scanning)
- **Gradle** (build system)

---

## Getting Started

1. **Open this project in Android Studio.**
2. **Sync Gradle files** (Android Studio will prompt if needed).
3. **Build and run** on your Android device or emulator.

---

## Building from Command Line

To build, lint, and test the app from the command line (after the wrapper is set up):

```sh
cmd /c gradlew.bat clean assembleRelease lint test
```

---

## Documentation

- [Usage Guide](docs/USAGE.md)
- [Accessibility](docs/ACCESSIBILITY.md)
- [Security](docs/SECURITY.md)
- [Contributing](docs/CONTRIBUTING.md)
- [FAQ](docs/FAQ.md)
- [Screenshots](docs/SCREENSHOTS.md)

---

## Accessibility & UX
- All interactive elements have descriptive content labels.
- Supports dynamic font scaling and theme switching.
- Loading indicators and error messages for long-running or failed operations.

---

## Security
- Password protection for PDF files.
- Secure handling of sensitive operations and files.

---

## Advanced Editing
For advanced PDF editing (e.g., full text and image editing), consider integrating commercial SDKs like PDFTron or PSPDFKit.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## Contributing
Pull requests and issues are welcome! Please open an issue to discuss major changes before submitting a PR.
