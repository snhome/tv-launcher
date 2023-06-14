enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
		maven { url = uri("https://jitpack.io") }

	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

	repositories {
		mavenCentral()
		google()
		maven { url = uri("https://jitpack.io") }

	}
}
