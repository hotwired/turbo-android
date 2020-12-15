package dev.hotwire.turbo.nav

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavGraphNavigator
import androidx.navigation.createGraph
import androidx.navigation.navOptions
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import dev.hotwire.turbo.R
import dev.hotwire.turbo.config.TurboPathConfiguration
import dev.hotwire.turbo.visit.TurboVisitOptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("UsePropertyAccessSyntax")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurboNavRuleTest {
    private lateinit var context: Context
    private lateinit var controller: TestNavHostController
    private lateinit var pathConfiguration: TurboPathConfiguration

    private val homeUrl = "http://hotwire.dev/home"
    private val featureUrl = "http://hotwire.dev/feature"
    private val newUrl = "http://hotwire.dev/feature/new"
    private val editUrl = "http://hotwire.dev/feature/edit"
    private val refreshUrl = "http://hotwire.dev/custom/refresh"
    private val resumeUrl = "http://hotwire.dev/custom/resume"

    private val webDestinationId = 1
    private val webModalDestinationId = 2
    private val webHomeDestinationId = 3

    private val webUri = Uri.parse("turbo://fragment/web")
    private val webModalUri = Uri.parse("turbo://fragment/web/modal")
    private val webHomeUri = Uri.parse("turbo://fragment/web/home")

    private val extras = null
    private val navOptions = navOptions {
        anim {
            enter = R.anim.nav_default_enter_anim
            exit = R.anim.nav_default_exit_anim
            popEnter = R.anim.nav_default_pop_enter_anim
            popExit = R.anim.nav_default_pop_exit_anim
        }
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        controller = buildControllerWithGraph()
        pathConfiguration = TurboPathConfiguration(context).apply {
            load(TurboPathConfiguration.Location(assetFilePath = "json/test-configuration.json"))
        }
    }

    @Test
    fun `navigate within context`() {
        val rule = getNavigatorRule(featureUrl)

        // Current destination
        assertThat(rule.previousLocation).isNull()
        assertThat(rule.currentLocation).isEqualTo(homeUrl)
        assertThat(rule.currentPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.isAtStartDestination).isTrue()

        // New destination
        assertThat(rule.newLocation).isEqualTo(featureUrl)
        assertThat(rule.newPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(TurboNavPresentation.PUSH)
        assertThat(rule.newNavigationMode).isEqualTo(TurboNavMode.IN_CONTEXT)
        assertThat(rule.newModalResult).isNull()
        assertThat(rule.newDestinationUri).isEqualTo(webUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    @Test
    fun `navigate to modal context`() {
        val rule = getNavigatorRule(newUrl)

        // Current destination
        assertThat(rule.previousLocation).isNull()
        assertThat(rule.currentLocation).isEqualTo(homeUrl)
        assertThat(rule.currentPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.isAtStartDestination).isTrue()

        // New destination
        assertThat(rule.newLocation).isEqualTo(newUrl)
        assertThat(rule.newPresentationContext).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(rule.newPresentation).isEqualTo(TurboNavPresentation.PUSH)
        assertThat(rule.newNavigationMode).isEqualTo(TurboNavMode.TO_MODAL)
        assertThat(rule.newModalResult).isNull()
        assertThat(rule.newDestinationUri).isEqualTo(webModalUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    @Test
    fun `navigate back to home from default context`() {
        controller.navigate(webDestinationId, locationArgs(featureUrl))
        val rule = getNavigatorRule(homeUrl)

        // Current destination
        assertThat(rule.previousLocation).isEqualTo(homeUrl)
        assertThat(rule.currentLocation).isEqualTo(featureUrl)
        assertThat(rule.currentPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(homeUrl)
        assertThat(rule.newPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(TurboNavPresentation.CLEAR_ALL)
        assertThat(rule.newNavigationMode).isEqualTo(TurboNavMode.IN_CONTEXT)
        assertThat(rule.newModalResult).isNull()
        assertThat(rule.newDestinationUri).isEqualTo(webHomeUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    @Test
    fun `navigate back to feature from modal context`() {
        controller.navigate(webDestinationId, locationArgs(featureUrl))
        controller.navigate(webModalDestinationId, locationArgs(newUrl))
        val rule = getNavigatorRule(featureUrl)

        // Current destination
        assertThat(rule.previousLocation).isEqualTo(featureUrl)
        assertThat(rule.currentLocation).isEqualTo(newUrl)
        assertThat(rule.currentPresentationContext).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(featureUrl)
        assertThat(rule.newPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(TurboNavPresentation.POP)
        assertThat(rule.newNavigationMode).isEqualTo(TurboNavMode.DISMISS_MODAL)
        assertThat(rule.newModalResult?.location).isEqualTo(featureUrl)
        assertThat(rule.newDestinationUri).isEqualTo(webUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    @Test
    fun `navigate from modal to same modal`() {
        controller.navigate(webModalDestinationId, locationArgs(newUrl))
        val rule = getNavigatorRule(newUrl)

        // Current destination
        assertThat(rule.previousLocation).isEqualTo(homeUrl)
        assertThat(rule.currentLocation).isEqualTo(newUrl)
        assertThat(rule.currentPresentationContext).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(newUrl)
        assertThat(rule.newPresentationContext).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(rule.newPresentation).isEqualTo(TurboNavPresentation.REPLACE)
        assertThat(rule.newNavigationMode).isEqualTo(TurboNavMode.IN_CONTEXT)
        assertThat(rule.newModalResult).isNull()
        assertThat(rule.newDestinationUri).isEqualTo(webModalUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    @Test
    fun `navigate from modal to new modal`() {
        controller.navigate(webModalDestinationId, locationArgs(newUrl))
        val rule = getNavigatorRule(editUrl)

        // Current destination
        assertThat(rule.previousLocation).isEqualTo(homeUrl)
        assertThat(rule.currentLocation).isEqualTo(newUrl)
        assertThat(rule.currentPresentationContext).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(editUrl)
        assertThat(rule.newPresentationContext).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(rule.newPresentation).isEqualTo(TurboNavPresentation.PUSH)
        assertThat(rule.newNavigationMode).isEqualTo(TurboNavMode.IN_CONTEXT)
        assertThat(rule.newModalResult).isNull()
        assertThat(rule.newDestinationUri).isEqualTo(webModalUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    @Test
    fun `refresh the current destination`() {
        controller.navigate(webDestinationId, locationArgs(featureUrl))
        val rule = getNavigatorRule(refreshUrl)

        // Current destination
        assertThat(rule.previousLocation).isEqualTo(homeUrl)
        assertThat(rule.currentLocation).isEqualTo(featureUrl)
        assertThat(rule.currentPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(refreshUrl)
        assertThat(rule.newPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(TurboNavPresentation.REFRESH)
        assertThat(rule.newNavigationMode).isEqualTo(TurboNavMode.REFRESH)
        assertThat(rule.newModalResult).isNull()
        assertThat(rule.newDestinationUri).isEqualTo(webUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    @Test
    fun `resume the previous destination after leaving modal context`() {
        controller.navigate(webDestinationId, locationArgs(featureUrl))
        controller.navigate(webModalDestinationId, locationArgs(newUrl))
        val rule = getNavigatorRule(resumeUrl)

        // Current destination
        assertThat(rule.previousLocation).isEqualTo(featureUrl)
        assertThat(rule.currentLocation).isEqualTo(newUrl)
        assertThat(rule.currentPresentationContext).isEqualTo(TurboNavPresentationContext.MODAL)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(resumeUrl)
        assertThat(rule.newPresentationContext).isEqualTo(TurboNavPresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(TurboNavPresentation.NONE)
        assertThat(rule.newNavigationMode).isEqualTo(TurboNavMode.DISMISS_MODAL)
        assertThat(rule.newModalResult).isNotNull()
        assertThat(rule.newModalResult?.location).isEqualTo(resumeUrl)
        assertThat(rule.newModalResult?.shouldNavigate).isFalse()
        assertThat(rule.newDestinationUri).isEqualTo(webUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    private fun getNavigatorRule(
        location: String,
        visitOptions: TurboVisitOptions = TurboVisitOptions(),
        bundle: Bundle? = null
    ): TurboNavRule {
        return TurboNavRule(
            location, visitOptions, bundle, navOptions, extras, pathConfiguration, controller
        )
    }

    private fun locationArgs(location: String): Bundle {
        return bundleOf("location" to location)
    }

    private fun buildControllerWithGraph(): TestNavHostController {
        return TestNavHostController(context).apply {
            graph = createGraph(startDestination = webHomeDestinationId) {
                destination(
                    NavDestinationBuilder(
                        navigator = provider.getNavigator<NavGraphNavigator>("test"),
                        id = webDestinationId
                    ).apply {
                        deepLink(webUri.toString())
                    }
                )

                destination(
                    NavDestinationBuilder(
                        navigator = provider.getNavigator<NavGraphNavigator>("test"),
                        id = webModalDestinationId
                    ).apply {
                        deepLink(webModalUri.toString())
                    }
                )

                destination(
                    NavDestinationBuilder(
                        navigator = provider.getNavigator<NavGraphNavigator>("test"),
                        id = webHomeDestinationId
                    ).apply {
                        argument("location") { defaultValue = homeUrl }
                        deepLink(webHomeUri.toString())
                    }
                )
            }
        }
    }
}
