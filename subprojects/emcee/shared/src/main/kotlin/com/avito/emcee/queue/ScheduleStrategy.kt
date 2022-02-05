package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel

/**
 * @see <a href="https://github.com/avito-tech/Emcee/tree/master/Sources/ScheduleStrategy">SchedulerStrategy Reference</a>
 */
@JsonClass(generateAdapter = true)
public data class ScheduleStrategy(
    @Json(name = "testSplitterType")
    val testsSplitter: TestsSplitter
) {

    @JsonClass(generateAdapter = true, generator = "sealed:type")
    public sealed class TestsSplitter {

        @TypeLabel("individual")
        public object Individual : TestsSplitter()

        @TypeLabel("equallyDivided")
        public object EquallyDivided : TestsSplitter()

        @TypeLabel("progressive")
        public object Progressive : TestsSplitter()

        @TypeLabel("unSplit")
        public object UnSplit : TestsSplitter()

        @TypeLabel("fixedBucketSize")
        @JsonClass(generateAdapter = true)
        public class FixedBucketSize(public val size: Int) : TestsSplitter()
    }
}
