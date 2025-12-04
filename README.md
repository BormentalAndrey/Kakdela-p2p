Kak Dela P2P - Local-first messenger scaffold

This archive contains a skeleton Android project (Jetpack Compose) that stores messages locally using Room and provides placeholders to implement P2P messaging (WebRTC/datachannel).

How to use:
1. Unzip and open in Android Studio.
2. If GitHub Actions fails due to missing wrapper jar, run locally: gradle wrapper
3. Add your images to app/src/main/res/drawable/ and mipmap for icons.
4. Implement P2P signalling and media features in app/src/main/java/com/kakdela/p2p/p2p/P2PService.kt
