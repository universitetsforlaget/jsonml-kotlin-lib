
plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.4.31"
}

group = "no.universitetsforlaget.juridika.libraries"
version = "1.0.5-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://nexus.knowit.no/nexus/repository/juridika-releases/")
        credentials {
            username = project.property("juridikaNexusUser") as String
            password = project.property("juridikaNexusPassword") as String
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("default") {
            artifactId = "jsonml"

            from(components["java"])
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = "https://nexus.knowit.no/nexus/repository/juridika-releases/"
            val snapshotsRepoUrl = "https://nexus.knowit.no/nexus/repository/juridika-snapshots/"

            url = uri(if (project.version.toString().contains("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = project.property("juridikaNexusUser") as String
                password = project.property("juridikaNexusPassword") as String
            }
        }
    }
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
