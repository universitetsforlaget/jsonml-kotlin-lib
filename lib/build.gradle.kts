
plugins {
    `java-library`
    `maven-publish`
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.4.20"
}

group = "no.universitetsforlaget.juridika.libraries"
version = "1.0.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("default") {
            artifactId = "json"

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
    implementation(kotlin("stdlib", "1.3.61"))
    implementation(kotlin("reflect", "1.3.61"))
    implementation(kotlin("stdlib-jdk8"))
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    //
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")

    //
    implementation("no.universitetsforlaget.juridika.libraries:textbook-processor:2.0.3")

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:29.0-jre")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // For unit testing involving XML comparison, using hamcrest matchers.
    testImplementation("org.xmlunit:xmlunit-matchers:2.6.3")
    testImplementation("org.xmlunit:xmlunit-core:2.6.3")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
