package com.avito.report.model

fun Entry.File.Companion.createStubInstance(
    comment: String = "",
    fileAddress: FileAddress = FileAddress.File("name"),
    timeInSeconds: Long = 0,
    fileType: Entry.File.Type = Entry.File.Type.img_png
) = Entry.File(
    comment = comment,
    fileAddress = fileAddress,
    timeInSeconds = timeInSeconds,
    fileType = fileType
)
