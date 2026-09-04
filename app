plugins { id 'com.android.application' }

android {
    namespace 'com.example.mydialer'
    compileSdk 35
    defaultConfig {
        applicationId 'com.example.mydialer'
        minSdk 23
        targetSdk 35
        versionCode 3
        versionName '3.0'
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.7.0'
}
