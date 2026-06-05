pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AdventAI"
include(":app")
include(":core:common")
include(":core:model")
include(":core:domain")
include(":core:network")
include(":core:data")
include(":core:designsystem")
include(":core:testing")
include(":feature:home")
include(":feature:chat")
