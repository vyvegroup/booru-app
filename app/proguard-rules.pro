# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.booru.app.model.** { *; }
-dontwarn okio.**
-dontwarn retrofit2.**
