package com.avito.android.test.report.troubleshooting

import com.avito.android.test.report.Report
import com.avito.android.test.report.troubleshooting.dump.Dumper
import com.avito.android.test.report.troubleshooting.dump.ThreadDumper

public class Troubleshooter(private val dumpers: Set<Dumper>) {

    public fun troubleshootTo(report: Report) {
        dumpers.forEach { dumper ->
            report.addText(dumper.label, dumper.dump())
        }
    }

    public class Builder {
        private val dumpers: MutableSet<Dumper> = mutableSetOf()

        public fun withDefaults(): Builder {
            dumpers.add(ThreadDumper())
            return this
        }

        public fun add(dumper: Dumper): Builder {
            dumpers.add(dumper)
            return this
        }

        public fun build(): Troubleshooter = Troubleshooter(dumpers)
    }
}
