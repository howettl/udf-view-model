import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.gradleMavenPublishPlugin)
}

android {
    namespace = "io.github.howettl.udfviewmodel"
    compileSdk = 34

    defaultConfig {
        minSdk = 19

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    mavenPublishing {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()

        coordinates("io.github.howettl", "udfviewmodel", "0.1.0")

        pom {
            name.set("UdfViewModel")
            description.set("A view model base class enforcing a UDF pattern.")
            inceptionYear.set("2024")
            url.set("https://github.com/howettl/udf-view-model")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("http://www.opensource.org/licenses/mit-license.php")
                    distribution.set("http://www.opensource.org/licenses/mit-license.php")
                }
            }
            developers {
                developer {
                    id.set("howettl")
                    name.set("Lee Howett")
                    url.set("https://github.com/howettl")
                }
            }
            scm {
                url.set("https://github.com/howettl/udf-view-model")
                connection.set("scm:git:git://github.com/howettl/udf-view-model.git")
                developerConnection.set("scm:git:ssh://git@github.com/howettl/udf-view-model.git")
            }
        }
    }
}

dependencies {

    testImplementation(libs.junit)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
}