package dev.hotwire.turbo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.After
import org.junit.Before
import org.junit.Rule

open class BaseUnitTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Before
    open fun setup() {}

    @After
    open fun teardown() {}
}
