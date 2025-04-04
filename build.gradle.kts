plugins {
    kotlin("jvm") version "2.1.0"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
    implementation("org.openjfx:javafx-web:21") // For WebView
}

javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml", "javafx.web")
}
