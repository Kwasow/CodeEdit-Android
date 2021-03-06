// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0")
        classpath(kotlin("gradle-plugin", version = "1.4.0"))

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }

    this.register("testClasses") {
        println("Task 'testClasses': Not implemented")
    }
}
