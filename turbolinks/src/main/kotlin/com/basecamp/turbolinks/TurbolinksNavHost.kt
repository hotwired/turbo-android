package com.basecamp.turbolinks

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.fragment.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

abstract class TurbolinksNavHost : NavHostFragment() {
    abstract val sessionName: String
    abstract val startLocation: String
    abstract val pathConfigurationLocation: PathConfiguration.Location
    abstract val registeredActivities: List<KClass<out Activity>>
    abstract val registeredFragments: List<KClass<out Fragment>>

    lateinit var session: TurbolinksSession
        private set

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNewSession()
        initControllerGraph()
    }

    internal fun createNewSession() {
        session = TurbolinksSession.getNew(sessionName, requireActivity(), onCreateWebView(requireActivity()))
        onSessionCreated()
    }

    open fun onSessionCreated() {
        session.pathConfiguration.load(pathConfigurationLocation)
    }

    open fun onCreateWebView(context: Context): TurbolinksWebView {
        return TurbolinksWebView(context, null)
    }

    fun reset() {
        session.reset()
        session.rootLocation = startLocation
        initControllerGraph()
    }

    val currentDestination: TurbolinksDestination
        get() = childFragmentManager.primaryNavigationFragment as TurbolinksDestination?
            ?: throw IllegalStateException("No current destination found in NavHostFragment")

    // Private

    private fun initControllerGraph() {
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

        navController.apply {
            graph = buildGraph(
                activityDestinations,
                fragmentDestinations,
                fragmentDestinations.startDestination().id
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildGraph(
        activityDestinations: List<ActivityDestination>,
        fragmentDestinations: List<FragmentDestination>,
        startDestinationId: Int
    ): NavGraph {
        return createGraph(startDestination = startDestinationId) {
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

            argument("sessionName") {
                defaultValue = sessionName
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
        val startDestinationUri = session.pathConfiguration.properties(startLocation).uri
        return requireNotNull(firstOrNull { it.uri == startDestinationUri }) {
            "A start Fragment destination was not found for uri: $startDestinationUri"
        }
    }

    private fun KClass<out Any>.turbolinksAnnotation(): TurbolinksGraphDestination {
        return requireNotNull(findAnnotation()) {
            "A TurbolinksGraphDestination annotation is required for the destination: ${this.simpleName}"
        }
    }

    internal inline fun NavGraphBuilder.fragment(
        @IdRes id: Int,
        fragmentClass: KClass<out Fragment>,
        builder: FragmentNavigatorDestinationBuilder.() -> Unit
    ) = destination(
        FragmentNavigatorDestinationBuilder(
        provider[FragmentNavigator::class],
        id,
        fragmentClass
    ).apply(builder))

    internal inline fun NavGraphBuilder.dialog(
        @IdRes id: Int,
        fragmentClass: KClass<out DialogFragment>,
        builder: DialogFragmentNavigatorDestinationBuilder.() -> Unit
    ) = destination(
        DialogFragmentNavigatorDestinationBuilder(
        provider[DialogFragmentNavigator::class],
        id,
        fragmentClass
    ).apply(builder))
}
