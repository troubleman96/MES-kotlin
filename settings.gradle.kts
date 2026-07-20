pluginManagement {
    repositories {
        google()
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

rootProject.name = "MES"

include(":app")
include(":core:core-designsystem")
include(":core:core-network")
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-domain")
include(":core:core-testing")
include(":feature:feature-onboarding")
include(":feature:feature-auth")
include(":feature:feature-catalog")
include(":feature:feature-cart")
include(":feature:feature-checkout")
include(":feature:feature-orders")
include(":feature:feature-notifications")
include(":feature:feature-merchant")
include(":feature:feature-profile")
