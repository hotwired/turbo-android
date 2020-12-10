package com.basecamp.turbo.nav

import android.app.Activity
import android.net.Uri
import androidx.annotation.IdRes
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.DialogFragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import com.basecamp.turbo.config.TurbolinksPathConfiguration
import com.basecamp.turbo.config.uri
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

internal class TurbolinksNavGraphBuilder(
    private val startLocation: String,
    private val navController: NavController,
    private val pathConfiguration: TurbolinksPathConfiguration
) {
    private data class ActivityDestination(
        val id: Int,
        val uri: Uri,
        val kClass: KClass<out Activity>
    )

    private data class FragmentDestination(
        val id: Int,
        val uri: Uri,
        val kClass: KClass<out Fragment>
    )

    fun build(
        registeredActivities: List<KClass<out Activity>>,
        registeredFragments: List<KClass<out Fragment>>
    ): NavGraph {
        var currentId = 1

        val activityDestinations = registeredActivities.map {
            ActivityDestination(
                id = currentId.also { currentId++ },
                uri = it.turbolinksAnnotation().uri.toUri(),
                kClass = it
            )
        }

        val fragmentDestinations = registeredFragments.map {
            FragmentDestination(
                id = currentId.also { currentId++ },
                uri = it.turbolinksAnnotation().uri.toUri(),
                kClass = it
            )
        }

        return createGraph(
            activityDestinations,
            fragmentDestinations,
            fragmentDestinations.startDestination().id
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun createGraph(
        activityDestinations: List<ActivityDestination>,
        fragmentDestinations: List<FragmentDestination>,
        startDestinationId: Int
    ): NavGraph {
        return navController.createGraph(startDestination = startDestinationId) {
            activityDestinations.forEach {
                activity(it.id) {
                    activityClass = it.kClass
                    deepLink(it.uri.toString())
                }
            }

            fragmentDestinations.withoutDialogs().forEach {
                fragment(it.id, it.kClass) {
                    deepLink(it.uri.toString())
                }
            }

            fragmentDestinations.dialogs().forEach {
                dialog(it.id, it.kClass as KClass<out DialogFragment>) {
                    deepLink(it.uri.toString())
                }
            }

            argument("location") {
                defaultValue = startLocation
            }
        }
    }

    private fun List<FragmentDestination>.dialogs(): List<FragmentDestination> {
        return filter { it.kClass.isSubclassOf(DialogFragment::class) }
    }

    private fun List<FragmentDestination>.withoutDialogs(): List<FragmentDestination> {
        return minus(dialogs())
    }

    private fun List<FragmentDestination>.startDestination(): FragmentDestination {
        val startDestinationUri = pathConfiguration.properties(startLocation).uri
        return requireNotNull(firstOrNull { it.uri == startDestinationUri }) {
            "A start Fragment destination was not found for uri: $startDestinationUri"
        }
    }

    private fun KClass<out Any>.turbolinksAnnotation(): TurbolinksNavGraphDestination {
        return requireNotNull(findAnnotation()) {
            "A TurbolinksNavGraphDestination annotation is required for the destination: ${this.simpleName}"
        }
    }

    // Modified from AndroidX FragmentNavigatorDestinationBuilder extensions
    private inline fun NavGraphBuilder.fragment(
        @IdRes id: Int,
        fragmentClass: KClass<out Fragment>,
        builder: FragmentNavigatorDestinationBuilder.() -> Unit
    ) = destination(
        FragmentNavigatorDestinationBuilder(
            provider[FragmentNavigator::class],
            id,
            fragmentClass
        ).apply(builder)
    )

    // Modified from AndroidX DialogFragmentNavigatorDestinationBuilder extensions
    private inline fun NavGraphBuilder.dialog(
        @IdRes id: Int,
        fragmentClass: KClass<out DialogFragment>,
        builder: DialogFragmentNavigatorDestinationBuilder.() -> Unit
    ) = destination(
        DialogFragmentNavigatorDestinationBuilder(
            provider[DialogFragmentNavigator::class],
            id,
            fragmentClass
        ).apply(builder)
    )
}
