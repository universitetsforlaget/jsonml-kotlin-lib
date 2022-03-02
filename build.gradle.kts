
plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.4.31"
}

group = "no.universitetsforlaget.juridika.libraries"
version = "1.0.6-SNAPSHOT"

val juridikaGitlabRepo: (RepositoryHandler) -> MavenArtifactRepository = {
    it.maven {
        val juridikaGitlabTokenType: String? by project
        val juridikaGitLabToken: String by project

        url = uri("https://gitlab.com/api/v4/projects/33843272/packages/maven")
        credentials(HttpHeaderCredentials::class) {
            name = juridikaGitlabTokenType ?: "Private-Token"
            value = juridikaGitLabToken // personal gitlab access token, store this in ~/.gradle/gradle.properties
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "jsonml"
            from(components["java"])
        }
    }
    repositories { juridikaGitlabRepo(this) }
}


dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // ObjectMapper, etc
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
