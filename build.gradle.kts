plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "org.takoyaki"
version = "1.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filteringCharset = "UTF-8"
    exclude("**/*.properties")
}

application {
    mainModule.set(
        "org.takoyaki.curriculummanager"
    )

    mainClass.set(
        "org.takoyaki.curriculummanager.HelloApplication"
    )
}

javafx {
    version = "21.0.12"

    modules = listOf(
        "javafx.controls", "javafx.fxml"
    )
}

dependencies {

    implementation(
        "org.xerial:sqlite-jdbc:3.50.3.0"
    )

    testImplementation(
        "org.junit.jupiter:junit-jupiter-api:$junitVersion"
    )

    testRuntimeOnly(
        "org.junit.jupiter:junit-jupiter-engine:$junitVersion"
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

var baseName = "履修管理くん"

jlink {

    options.set(
        listOf(
            "--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"
        )
    )

    launcher {
        name = baseName
    }

    jpackage {

        imageName = baseName

        installerName = baseName

        appVersion = "1.0.0"

        icon = "src/main/resources/icon.ico"

        installerType = "exe"

        installerOptions = listOf(
            "--win-menu", "--win-shortcut", "--win-dir-chooser"
        )
    }
}
