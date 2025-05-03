# Security in PdfEditorApp

## PDF Password Protection
- Users can set or remove passwords on any PDF file.
- Passwords are handled securely and never stored.

## File Access
- All file operations (open, edit, save, export) include error handling and user-friendly messages.
- Sensitive operations are only performed with explicit user action.

## Permissions
- The app requests only the minimum permissions required (storage, camera).
- All permissions are explained to the user and handled gracefully if denied.

## Data Privacy
- No user data is sent to external servers.
- All processing is done locally on the device.

## Reporting Issues
- If you find a security vulnerability, please report it via a GitHub issue or contact the maintainer directly.
