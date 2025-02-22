    plugins {
        id("com.android.application")
        id("com.google.gms.google-services")
    }
    
    android {
        namespace = "com.example.smarttrack"
        compileSdk = 34
        buildFeatures {
            dataBinding = true
        }
        defaultConfig {
            applicationId = "com.example.smarttrack"
            minSdk = 27
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"
    
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
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
    
    dependencies {
        // Import the BoM for the Firebase platform
        implementation ("com.squareup.picasso:picasso:2.71828")
        implementation ("de.hdodenhof:circleimageview:3.1.0")
        implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
        implementation("com.intuit.sdp:sdp-android:1.1.0")
        implementation("com.intuit.ssp:ssp-android:1.1.0")
        // Add the dependency for the Firebase Authentication library
        // When using the BoM, you don't specify versions in Firebase library dependencies
        implementation("com.google.firebase:firebase-auth")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("com.google.firebase:firebase-auth:22.3.1")
        implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
        implementation("com.google.firebase:firebase-firestore:24.10.1")
        implementation("com.google.firebase:firebase-storage:20.0.0")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    }