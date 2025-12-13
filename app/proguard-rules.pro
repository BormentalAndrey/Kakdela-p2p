# ProGuard rules for Kakdela-p2p

-keep class com.kakdela.p2p.** { *; }
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
