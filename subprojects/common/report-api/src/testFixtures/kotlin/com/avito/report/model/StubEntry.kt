package com.avito.report.model

public fun Entry.File.Companion.createStubInstance(
    comment: String = "",
    fileAddress: FileAddress = FileAddress.File("name"),
    timeInSeconds: Long = 0,
    fileType: Entry.File.Type = Entry.File.Type.img_png
): Entry.File = Entry.File(
    comment = comment,
    fileAddress = fileAddress,
    timeInSeconds = timeInSeconds,
    fileType = fileType
)
