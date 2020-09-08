package com.basecamp.turbolinks

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.*
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.basecamp.turbolinks.TurbolinksNavigationRule.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurbolinksNavigationRuleTest {
    private lateinit var context: Context
    private lateinit var controller: TestNavHostController
    private lateinit var pathConfiguration: PathConfiguration

    private val homeUrl = "http://example.com/home"
    private val featureUrl = "http://example.com/feature"
    private val newUrl = "http://example.com/feature/new"
    private val editUrl = "http://example.com/feature/edit"
    private val refreshUrl = "http://example.com/custom/refresh"

    private val webDestinationId = 1
    private val webModalDestinationId = 2
    private val webHomeDestinationId = 3

    private val webUri = Uri.parse("turbolinks://fragment/web")
    private val webModalUri = Uri.parse("turbolinks://fragment/web/modal")
    private val webHomeUri = Uri.parse("turbolinks://fragment/web/home")

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
        pathConfiguration = PathConfiguration(context).apply {
            load(PathConfiguration.Location(assetFilePath = "json/test-configuration.json"))
        }
    }

    @Test
    fun `navigate within context`() {
        val rule = getNavigatorRule(featureUrl)

        // Current destination
        assertThat(rule.previousLocation).isNull()
        assertThat(rule.currentLocation).isEqualTo(homeUrl)
        assertThat(rule.currentContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.isAtStartDestination).isTrue()

        // New destination
        assertThat(rule.newLocation).isEqualTo(featureUrl)
        assertThat(rule.newContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(Presentation.PUSH)
        assertThat(rule.newNavigationMode).isEqualTo(NavigationMode.IN_CONTEXT)
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
        assertThat(rule.currentContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.isAtStartDestination).isTrue()

        // New destination
        assertThat(rule.newLocation).isEqualTo(newUrl)
        assertThat(rule.newContext).isEqualTo(PresentationContext.MODAL)
        assertThat(rule.newPresentation).isEqualTo(Presentation.PUSH)
        assertThat(rule.newNavigationMode).isEqualTo(NavigationMode.TO_MODAL)
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
        assertThat(rule.currentContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(homeUrl)
        assertThat(rule.newContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(Presentation.REPLACE_ALL)
        assertThat(rule.newNavigationMode).isEqualTo(NavigationMode.IN_CONTEXT)
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
        assertThat(rule.currentContext).isEqualTo(PresentationContext.MODAL)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(featureUrl)
        assertThat(rule.newContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(Presentation.POP)
        assertThat(rule.newNavigationMode).isEqualTo(NavigationMode.DISMISS_MODAL)
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
        assertThat(rule.currentContext).isEqualTo(PresentationContext.MODAL)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(newUrl)
        assertThat(rule.newContext).isEqualTo(PresentationContext.MODAL)
        assertThat(rule.newPresentation).isEqualTo(Presentation.REPLACE)
        assertThat(rule.newNavigationMode).isEqualTo(NavigationMode.IN_CONTEXT)
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
        assertThat(rule.currentContext).isEqualTo(PresentationContext.MODAL)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(editUrl)
        assertThat(rule.newContext).isEqualTo(PresentationContext.MODAL)
        assertThat(rule.newPresentation).isEqualTo(Presentation.PUSH)
        assertThat(rule.newNavigationMode).isEqualTo(NavigationMode.IN_CONTEXT)
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
        assertThat(rule.currentContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.isAtStartDestination).isFalse()

        // New destination
        assertThat(rule.newLocation).isEqualTo(refreshUrl)
        assertThat(rule.newContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(Presentation.REFRESH)
        assertThat(rule.newNavigationMode).isEqualTo(NavigationMode.REFRESH)
        assertThat(rule.newModalResult).isNull()
        assertThat(rule.newDestinationUri).isEqualTo(webUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    private fun getNavigatorRule(
        location: String,
        visitOptions: VisitOptions = VisitOptions(),
        bundle: Bundle? = null
    ): TurbolinksNavigationRule {
        return TurbolinksNavigationRule(
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
