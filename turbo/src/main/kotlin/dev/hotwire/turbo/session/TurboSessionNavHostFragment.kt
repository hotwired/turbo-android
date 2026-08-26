package dev.hotwire.turbo.session

import android.content.Context
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PROTECTED
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dev.hotwire.turbo.config.TurboPathConfiguration
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.nav.TurboNavGraphBuilder
import dev.hotwire.turbo.views.TurboWebView
import kotlin.reflect.KClass

internal const val DEEPLINK_IDS_KEY = "android-support-nav:controller:deepLinkIds"
internal const val DEEPLINK_EXTRAS_KEY = "android-support-nav:controller:deepLinkExtras"
internal const val DEEPLINK_ARGS_KEY = "android-support-nav:controller:deepLinkArgs"
internal const val LOCATION_KEY = "location"

abstract class TurboSessionNavHostFragment : NavHostFragment() {
    /**
     * The name of the [TurboSession] instance, which is helpful for debugging
     * purposes. This is arbitrary, but must be unique in your app.
     */
    abstract val sessionName: String

    /**
     * The url of a starting location when your app starts up.
     */
    abstract val startLocation: String

    /**
     * The location of your [TurboPathConfiguration] JSON file(s) to configure
     * navigation rules.
     */
    abstract val pathConfigurationLocation: TurboPathConfiguration.Location

    /**
     * A list of registered fragments that can be navigated to. Every possible
     * [dev.hotwire.turbo.fragments.TurboFragment] destination must be listed here.
     */
    abstract val registeredFragments: List<KClass<out Fragment>>

    /**
     * A list of registered Activities that can be navigated to. This is optional.
     */
    open val registeredActivities: List<KClass<out AppCompatActivity>> = emptyList()

    /**
     * The [TurboSession] instance that is shared with all destinations that are
     * hosted inside this [TurboSessionNavHostFragment].
     */
    lateinit var session: TurboSession
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNewSession()
        removeExternalDeeplinkNavigation(requireActivity())
        initControllerGraph()
    }

    internal fun createNewSession() {
        val activity = requireActivity() as AppCompatActivity
        session = TurboSession(sessionName, activity, onCreateWebView(activity))
        onSessionCreated()
    }

    /**
     * Called whenever the [TurboSession] instance has been (re)created. A new
     * session is created whenever the [TurboSessionNavHostFragment] is created
     * and whenever the WebView render process has been terminated and a new
     * WebView instance is required.
     */
    open fun onSessionCreated() {
        session.pathConfiguration.load(pathConfigurationLocation)
    }

    /**
     * Called whenever a new WebView instance needs to be (re)created. You can
     * override this to provide your own [TurboWebView] subclass if you need
     * custom behaviors.
     */
    open fun onCreateWebView(context: Context): TurboWebView {
        return TurboWebView(context, null)
    }

    /**
     * Resets the [TurboSessionNavHostFragment] instance, it's [TurboSession]
     * instance, and the entire navigation graph to its original starting point.
     */
    fun reset(onReset: () -> Unit = {}) {
        currentNavDestination.delegate().navigator.onNavigationVisit {
            currentNavDestination.clearBackStack {
                session.reset()
                initControllerGraph()

                if (view == null) {
                    onReset()
                } else {
                    requireView().post { onReset() }
                }
            }
        }
    }

    /**
     * Retrieves the currently active [TurboNavDestination] on the backstack.
     */
    val currentNavDestination: TurboNavDestination
        get() = childFragmentManager.primaryNavigationFragment as TurboNavDestination?
            ?: throw IllegalStateException("No current destination found in NavHostFragment")

    /**
     * Google's Navigation library automatically consumes the launching Intent's deep-link
     * navigation extras (`deepLinkIds` + `deepLinkExtras`/`deepLinkArgs`) and navigates the back
     * stack accordingly. An exported Activity's launch Intent is fully attacker-controllable, so a
     * malicious app can use these extras to open an arbitrary registered destination — including a
     * native [dev.hotwire.turbo.fragments.TurboFragment] with authenticated side effects — and
     * inject a `location` argument into it.
     *
     * Turbo establishes its start destination from the navigation graph (see [startLocation]) and
     * navigates thereafter through [TurboSession] visits, never through these Intent extras, so
     * they have no legitimate producer. Remove them before the graph is created so the launching
     * Intent can't drive navigation. This also neutralizes the `location` injection an on-host
     * value would otherwise pass through the host check.
     */
    @VisibleForTesting
    internal fun removeExternalDeeplinkNavigation(activity: FragmentActivity) {
        activity.intent?.apply {
            removeExtra(DEEPLINK_IDS_KEY)
            removeExtra(DEEPLINK_EXTRAS_KEY)
            removeExtra(DEEPLINK_ARGS_KEY)
        }
    }

    private fun initControllerGraph() {
        navController.apply {
            graph = TurboNavGraphBuilder(
                startLocation = startLocation,
                pathConfiguration = session.pathConfiguration,
                navController = findNavController()
            ).build(
                registeredActivities = registeredActivities,
                registeredFragments = registeredFragments
            )
        }
    }
}
