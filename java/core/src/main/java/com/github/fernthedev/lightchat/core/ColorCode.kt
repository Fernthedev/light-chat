package com.github.fernthedev.lightchat.core

import java.util.regex.Pattern

/**
 * Simplistic enumeration of all supported color values for chat.
 * @author net.md_5
 */
enum class ColorCode(
    /**
     * The code appended to [.COLOR_CHAR] to make usable colour.
     */
    private val code: Char, val namee: String
) {
    /**
     * Represents black.
     */
    BLACK('0', "black"),

    /**
     * Represents dark blue.
     */
    DARK_BLUE('1', "dark_blue"),

    /**
     * Represents dark green.
     */
    DARK_GREEN('2', "dark_green"),

    /**
     * Represents dark blue (aqua).
     */
    DARK_AQUA('3', "dark_aqua"),

    /**
     * Represents dark red.
     */
    DARK_RED('4', "dark_red"),

    /**
     * Represents dark purple.
     */
    DARK_PURPLE('5', "dark_purple"),

    /**
     * Represents gold.
     */
    GOLD('6', "gold"),

    /**
     * Represents gray.
     */
    GRAY('7', "gray"),

    /**
     * Represents dark gray.
     */
    DARK_GRAY('8', "dark_gray"),

    /**
     * Represents blue.
     */
    BLUE('9', "blue"),

    /**
     * Represents green.
     */
    GREEN('a', "green"),

    /**
     * Represents aqua.
     */
    AQUA('b', "aqua"),

    /**
     * Represents red.
     */
    RED('c', "red"),

    /**
     * Represents light purple.
     */
    LIGHT_PURPLE('d', "light_purple"),

    /**
     * Represents yellow.
     */
    YELLOW('e', "yellow"),

    /**
     * Represents white.
     */
    WHITE('f', "white"),

    /**
     * Represents magical characters that change around randomly.
     */
    MAGIC('k', "obfuscated"),

    /**
     * Makes the text bold.
     */
    BOLD('l', "bold"),

    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH('m', "strikethrough"),

    /**
     * Makes the text appear underlined.
     */
    UNDERLINE('n', "underline"),

    /**
     * Makes the text italic.
     */
    ITALIC('o', "italic"),

    /**
     * Resets all previous chat colors or formats.
     */
    RESET('r', "reset");

    /**
     * This colour's colour char prefixed by the [.COLOR_CHAR].
     */
    private val toString: String = String(
        charArrayOf(
            '\u00A7', code
        )
    )

    override fun toString(): String {
        return toString
    }

    companion object {
        /**
         * The special character which prefixes all chat colour codes. Use this if
         * you need to dynamically convert colour codes from your custom format.
         */
        const val COLOR_CHAR: Char = '\u00A7'
        const val ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr"

        /**
         * Pattern to remove all colour codes.
         */
        private val STRIP_COLOR_PATTERN: Pattern = Pattern.compile("(?i)$COLOR_CHAR[0-9A-FK-OR]")

        /**
         * Colour instances keyed by their active character.
         */
        private val BY_CHAR: MutableMap<Char, ColorCode> = HashMap()

        init {
            for (colour in values()) {
                BY_CHAR[colour.code] = colour
            }
        }

        /**
         * Strips the given message of all color codes
         *
         * @param input String to strip of color
         * @return A copy of the input string, without any coloring
         */
        fun stripColor(input: String?): String? {
            return if (input == null) {
                null
            } else STRIP_COLOR_PATTERN.matcher(input).replaceAll("")
        }

        fun translateAlternateColorCodes(altColorChar: Char, textToTranslate: String): String {
            val b = textToTranslate.toCharArray()
            for (i in 0 until b.size - 1) {
                if (b[i] == altColorChar && ALL_CODES.indexOf(b[i + 1]) > -1) {
                    b[i] = COLOR_CHAR
                    b[i + 1] = b[i + 1].lowercaseChar()
                }
            }
            return String(b)
        }

        /**
         * Get the colour represented by the specified code.
         *
         * @param code the code to search for
         * @return the mapped colour, or null if non exists
         */
        fun getByChar(code: Char): ColorCode? {
            return BY_CHAR[code]
        }
    }
}