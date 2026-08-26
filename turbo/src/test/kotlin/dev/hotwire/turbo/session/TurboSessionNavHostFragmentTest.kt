package dev.hotwire.turbo.session

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import dev.hotwire.turbo.BaseUnitTest
import dev.hotwire.turbo.config.TurboPathConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.reflect.KClass

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class TurboSessionNavHostFragmentTest : BaseUnitTest() {

    private lateinit var activity: AppCompatActivity
    private lateinit var host: TestNavHostFragment

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun `removes the deep link ids that select the destination`() {
        val intent = Intent().apply { putExtra(DEEPLINK_IDS_KEY, intArrayOf(1, 2, 3)) }
        activity = Robolectric.buildActivity(TestActivity::class.java, intent).create().get()

        host = TestNavHostFragment()
        host.removeExternalDeeplinkNavigation(activity)

        assertThat(activity.intent.hasExtra(DEEPLINK_IDS_KEY)).isFalse()
    }

    @Test
    fun `removes the deep link extras that inject the destination arguments`() {
        val intent = Intent().apply { putExtra(DEEPLINK_EXTRAS_KEY, bundleOf(LOCATION_KEY to ATTACKER_URL)) }
        activity = Robolectric.buildActivity(TestActivity::class.java, intent).create().get()

        host = TestNavHostFragment()
        host.removeExternalDeeplinkNavigation(activity)

        assertThat(activity.intent.hasExtra(DEEPLINK_EXTRAS_KEY)).isFalse()
    }

    @Test
    fun `removes the deep link args that inject per-destination arguments`() {
        val intent = Intent().apply {
            putParcelableArrayListExtra(DEEPLINK_ARGS_KEY, arrayListOf(bundleOf(LOCATION_KEY to ATTACKER_URL)))
        }
        activity = Robolectric.buildActivity(TestActivity::class.java, intent).create().get()

        host = TestNavHostFragment()
        host.removeExternalDeeplinkNavigation(activity)

        assertThat(activity.intent.hasExtra(DEEPLINK_ARGS_KEY)).isFalse()
    }

    // Regression for the full chain: an attacker-crafted launch Intent that selects a native
    // destination via deepLinkIds and injects an on-host `location` into it via deepLinkExtras.
    // The earlier host check reverted only off-host locations, so an on-host location survived and
    // reached the destination as its `location` argument. Neutralizing the whole deep-link
    // navigation leaves nothing for NavController.handleDeepLink to act on.
    @Test
    fun `neutralizes an on-host native destination deep link`() {
        val intent = Intent().apply {
            putExtra(DEEPLINK_IDS_KEY, intArrayOf(1))
            putExtra(DEEPLINK_EXTRAS_KEY, bundleOf(LOCATION_KEY to ON_HOST_ATTACKER_URL))
            putParcelableArrayListExtra(DEEPLINK_ARGS_KEY, arrayListOf(bundleOf(LOCATION_KEY to ON_HOST_ATTACKER_URL)))
        }
        activity = Robolectric.buildActivity(TestActivity::class.java, intent).create().get()

        host = TestNavHostFragment()
        host.removeExternalDeeplinkNavigation(activity)

        assertThat(activity.intent.hasExtra(DEEPLINK_IDS_KEY)).isFalse()
        assertThat(activity.intent.hasExtra(DEEPLINK_EXTRAS_KEY)).isFalse()
        assertThat(activity.intent.hasExtra(DEEPLINK_ARGS_KEY)).isFalse()
    }

    @Test
    fun `leaves a normal launch intent without deep link navigation untouched`() {
        activity = Robolectric.buildActivity(TestActivity::class.java, Intent()).create().get()

        host = TestNavHostFragment()
        host.removeExternalDeeplinkNavigation(activity)

        assertThat(activity.intent.hasExtra(DEEPLINK_IDS_KEY)).isFalse()
        assertThat(activity.intent.hasExtra(DEEPLINK_EXTRAS_KEY)).isFalse()
        assertThat(activity.intent.hasExtra(DEEPLINK_ARGS_KEY)).isFalse()
    }

    companion object {
        private const val ATTACKER_URL = "https://attacker.example/steal"

        // Same host as the configured start location below, so it would pass a host check.
        private const val ON_HOST_ATTACKER_URL = "https://example.com/other-users-attachment"
    }

}

class TestActivity : AppCompatActivity()

class TestNavHostFragment : TurboSessionNavHostFragment() {
    override val sessionName = "test"
    override val startLocation = "https://example.com/start"
    override val pathConfigurationLocation = TurboPathConfiguration.Location(
        assetFilePath = "json/test-configuration.json"
    )
    override val registeredFragments: List<KClass<out Fragment>> = emptyList()
}
