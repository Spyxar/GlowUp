plugins {
    id("maven-publish")
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
}

version = "${project.property("mod_version")}+${stonecutter.current.version}"
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String
}

repositories {
    //Cloth Config
    maven(url = "https://maven.shedaniel.me/")
    //ModMenu
    maven(url = "https://maven.terraformersmc.com/releases/")
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    api("com.terraformersmc:modmenu:${property("modmenu_version")}")
    api("me.shedaniel.cloth:cloth-config-fabric:${property("clothconfig_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    //Toml
    implementation("com.moandjiezana.toml:toml4j:${project.property("toml4j_version")}")
    include("com.moandjiezana.toml:toml4j:${project.property("toml4j_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft", stonecutter.current.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to stonecutter.current.version,
            "loader_version" to project.property("loader_version") as String
        )
    }
}

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and create separate worlds
    }
}

stonecutter {
    dependencies["modmenu"] = project.property("modmenu_version").toString()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    targetCompatibility = JavaVersion.VERSION_25
    sourceCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { name ->
            "${name}_${inputs.properties["archivesName"]}"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}