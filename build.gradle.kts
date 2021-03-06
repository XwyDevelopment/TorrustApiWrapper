plugins {
    kotlin("jvm") version "1.7.0"
    `maven-publish`
}

group = "dev.reeve"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.code.gson:gson:2.9.0")
	implementation("com.squareup.okio:okio:3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("junit:junit:4.13.2")
}

publishing {
    publications {
        create<MavenPublication>("main") {
            artifactId = "torrust-api-wrapper"
            from(components["kotlin"])
        }
    }
}