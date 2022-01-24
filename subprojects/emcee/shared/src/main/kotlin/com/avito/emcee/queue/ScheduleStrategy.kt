package com.avito.emcee.queue

import com.squareup.moshi.Json

/**
 * @see <a href="https://github.com/avito-tech/Emcee/tree/master/Sources/ScheduleStrategy">SchedulerStrategy Reference</a>
 */
public data class ScheduleStrategy(
    @Json(name = "testSplitterType")
    val testsSplitter: TestsSplitter
) {
    public sealed class TestsSplitter(public val type: String) {

        public object Individual : TestsSplitter(type = "individual")
        public object EquallyDivided : TestsSplitter(type = "equallyDivided")
        public object Progressive : TestsSplitter(type = "progressive")
        public object UnSplit : TestsSplitter(type = "unsplit")
        public class FixedBucketSize(public val size: Int) : TestsSplitter(type = "individual")
    }
}
