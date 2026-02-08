plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.BattleGrounds.GfxTool"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.BattleGrounds.GfxTool"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 10
        versionName = "4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // RecyclerView for Image Slider
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Material Components (for ShapeableImageView)
    implementation("com.google.android.material:material:1.10.0")

    // Circular ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Glide (for efficient image loading & circular crop)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // AndroidX dependencies
    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation ("com.ncorti:slidetoact:0.11.0")

    implementation("androidx.compose.material:material:1.5.0")
    implementation ("com.daimajia.swipelayout:library:1.2.0")
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.unity3d.ads:unity-ads:4.9.2") // ✅ This is the key line
    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Define Shizuku version correctly
    val shizuku_version = "13.1.5"  // Update this if a newer version is available
    implementation("dev.rikka.shizuku:api:$shizuku_version")
    implementation("dev.rikka.shizuku:provider:$shizuku_version")
}
