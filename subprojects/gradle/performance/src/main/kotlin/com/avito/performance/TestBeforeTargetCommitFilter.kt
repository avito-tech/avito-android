package com.avito.performance

import com.avito.report.model.HistoryTest

internal interface TestBeforeTargetCommitFilter {

    fun filter(list: List<HistoryTest>): HistoryTest?

    class Impl(
        private val detector: CommitAncestorDetector,
        private val targetCommit: String
    ) : TestBeforeTargetCommitFilter {
        /*
        * Если targetCommit является потомком коммита [или тем же самым коммитом],
        * который вернул сервис истории, то это то что требуется для сравнения, в случае если девелоп уехал после
        * targetCommit во врем билда. В противном случае, targetCommit являлся бы предком этого коммита*/
        override fun filter(list: List<HistoryTest>): HistoryTest? {
            return list.find {
                val commit = it.getBuildCommit()
                if (commit == null) {
                    false
                } else {
                    detector.isParent(ancestor = commit, child = targetCommit)
                }
            }
        }
    }
}

