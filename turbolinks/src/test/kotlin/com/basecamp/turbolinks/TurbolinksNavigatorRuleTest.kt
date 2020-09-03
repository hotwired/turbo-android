package com.basecamp.turbolinks

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.*
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.basecamp.turbolinks.TurbolinksNavigatorRule.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurbolinksNavigatorRuleTest {
    private lateinit var context: Context
    private lateinit var controller: TestNavHostController
    private lateinit var pathConfiguration: PathConfiguration

    private val homeUrl = "http://example.com/home"
    private val featureUrl = "http://example.com/feature"
    private val newUrl = "http://example.com/new"

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
        val rule = getRule(featureUrl)

        // Current destination
        assertThat(rule.previousLocation).isNull()
        assertThat(rule.currentLocation).isEqualTo(homeUrl)
        assertThat(rule.currentContext).isEqualTo(PresentationContext.DEFAULT)

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
        val rule = getRule(newUrl)

        // Current destination
        assertThat(rule.previousLocation).isNull()
        assertThat(rule.currentLocation).isEqualTo(homeUrl)
        assertThat(rule.currentContext).isEqualTo(PresentationContext.DEFAULT)

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
        controller.navigate(1, bundleOf("location" to featureUrl))
        val rule = getRule(homeUrl)

        // Current destination
        assertThat(rule.previousLocation).isEqualTo(homeUrl)
        assertThat(rule.currentLocation).isEqualTo(featureUrl)
        assertThat(rule.currentContext).isEqualTo(PresentationContext.DEFAULT)

        // New destination
        assertThat(rule.newLocation).isEqualTo(homeUrl)
        assertThat(rule.newContext).isEqualTo(PresentationContext.DEFAULT)
        assertThat(rule.newPresentation).isEqualTo(Presentation.POP)
        assertThat(rule.newNavigationMode).isEqualTo(NavigationMode.IN_CONTEXT)
        assertThat(rule.newModalResult).isNull()
        assertThat(rule.newDestinationUri).isEqualTo(webHomeUri)
        assertThat(rule.newDestination).isNotNull()
        assertThat(rule.newNavOptions).isEqualTo(navOptions)
    }

    private fun getRule(
        location: String,
        visitOptions: VisitOptions = VisitOptions(),
        bundle: Bundle? = null
    ): TurbolinksNavigatorRule {
        return TurbolinksNavigatorRule(
            location, visitOptions, bundle, navOptions, extras, pathConfiguration, controller
        )
    }

    private fun buildControllerWithGraph(): TestNavHostController {
        val webId = 1
        val webModalId = 2
        val webHomeId = 3

        return TestNavHostController(context).apply {
            graph = createGraph(startDestination = webHomeId) {
                destination(
                    NavDestinationBuilder(provider.getNavigator<NavGraphNavigator>("test"), webId).apply {
                        deepLink(webUri.toString())
                    }
                )

                destination(
                    NavDestinationBuilder(provider.getNavigator<NavGraphNavigator>("test"), webModalId).apply {
                        deepLink(webModalUri.toString())
                    }
                )

                destination(
                    NavDestinationBuilder(provider.getNavigator<NavGraphNavigator>("test"), webHomeId).apply {
                        argument("location") { defaultValue = homeUrl }
                        deepLink(webHomeUri.toString())
                    }
                )
            }
        }
    }
}
