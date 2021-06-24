package ru.avito.util

public sealed class Characters(internal val chars: List<Char>) {

    public object AlphabeticLowercase : Characters(
        chars = ('a'..'z').toList()
    )

    public object AlphabeticUppercase : Characters(
        chars = ('A'..'Z').toList()
    )

    public object Alphabetic : Characters(
        chars = AlphabeticLowercase.chars + AlphabeticUppercase.chars
    )

    public object Digits : Characters(
        chars = ('0'..'9').toList()
    )

    public object Punctuation : Characters(
        chars = listOf('.', ',', '!', '?', ';', ':', '-', '(', ')', '"').toList()
    )

    public object Special : Characters(
        chars = listOf('@', '#', '$', '%', '^', '&', '*', '{', '}', '[', ']', '/', '\\', '|')
    )

    public object Alphanumeric : Characters(
        chars = Alphabetic.chars + Digits.chars
    )

    public object Text : Characters(
        chars = Alphabetic.chars + Digits.chars + Punctuation.chars
    )

    public class SingleChar(char: Char) : Characters(listOf(char))

    public object All : Characters(
        chars = Alphabetic.chars + Digits.chars + Punctuation.chars + Special.chars
    )
}
