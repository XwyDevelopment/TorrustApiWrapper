plugins {
    kotlin("jvm") version "1.7.0"
    `maven-publish`
}

group = "dev.reeve"
version = "latest"

repositories {
    mavenCentral()
    maven("https://repo.reeve.dev/repository/maven-releases/")
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    implementation("com.google.code.gson:gson:2.9.0")
	implementation("com.squareup.okio:okio:3.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("main") {
            from(components["kotlin"])
            
            repositories {
                maven("https://repo.reeve.dev/repository/maven-releases/") {
                    name = "Nexus"
                    credentials {
                        username = System.getenv("NEXUS_REPO_USER")
                        password = System.getenv("NEXUS_REPO_PASSWORD")
                    }
                }
            }
        }
    }
}