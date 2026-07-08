package dev.hotwire.turbo.session

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
    fun `reverts to config start location when deep link host differs`() {
        val extras = bundleOf(LOCATION_KEY to "https://other.com/path")
        val intent = Intent().apply { putExtra(DEEPLINK_EXTRAS_KEY, extras) }
        activity = Robolectric.buildActivity(TestActivity::class.java, intent).create().get()

        host = TestNavHostFragment()
        host.ensureDeeplinkStartLocationValid(activity)

        val resultBundle = activity.intent.getBundleExtra(DEEPLINK_EXTRAS_KEY)
        assertThat(resultBundle?.getString(LOCATION_KEY)).isEqualTo("https://example.com/start")
    }

    @Test
    fun `does not change start location when deep link host matches config`() {
        val extras = bundleOf(LOCATION_KEY to "https://example.com/path")
        val intent = Intent().apply { putExtra(DEEPLINK_EXTRAS_KEY, extras) }
        activity = Robolectric.buildActivity(TestActivity::class.java, intent).create().get()

        host = TestNavHostFragment()
        host.ensureDeeplinkStartLocationValid(activity)

        val resultBundle = activity.intent.getBundleExtra(DEEPLINK_EXTRAS_KEY)
        assertThat(resultBundle?.getString(LOCATION_KEY)).isEqualTo("https://example.com/path")
    }

    // NavController merges deepLinkArgs over deepLinkExtras (last write wins); the intent's args
    // must not survive to override the validated start location.
    @Test
    fun `empties deepLinkArgs so they cannot override the start location`() {
        val intent = Intent().apply {
            putExtra(DEEPLINK_EXTRAS_KEY, bundleOf(LOCATION_KEY to "https://example.com/ok"))
            putParcelableArrayListExtra(DEEPLINK_ARGS_KEY, arrayListOf(bundleOf(LOCATION_KEY to ATTACKER_URL)))
        }
        activity = Robolectric.buildActivity(TestActivity::class.java, intent).create().get()

        host = TestNavHostFragment()
        host.ensureDeeplinkStartLocationValid(activity)

        val survivingArgs = activity.intent.getParcelableArrayListExtra<Bundle>(DEEPLINK_ARGS_KEY)
            ?.mapNotNull { it.getString(LOCATION_KEY) }.orEmpty()
        assertThat(survivingArgs).doesNotContain(ATTACKER_URL)
    }

    companion object {
        private const val ATTACKER_URL = "https://attacker.example/steal"
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

