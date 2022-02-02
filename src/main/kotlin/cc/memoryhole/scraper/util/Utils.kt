package cc.memoryhole.scraper.util

import cc.memoryhole.scraper.model.patreon.PatreonMedia
import java.text.CharacterIterator
import java.text.Normalizer
import java.text.StringCharacterIterator

open class Utils {
    open fun humanReadableByteCountSI(bytes: Long): String {
        var returnValue = bytes
        if (-1000 < returnValue && returnValue < 1000) {
            return "$returnValue B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (returnValue <= -999950 || returnValue >= 999950) {
            returnValue /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", returnValue / 1000.0, ci.current())
    }

    fun guessNameFromURL(url: String): String {
        return url.split("?")[0].split("/").last()
    }

    fun guessPatreonMediaFileName(patreonMedia: PatreonMedia): String {
        // Some original filenames would contain urls or end in a base64 string - from importing from other platforms/websites
        // or use download url ending

        // Remove any parameters
        var fileName = patreonMedia.mediaAttributes.fileName

        if (fileName != null) {
            if (fileName.contains("?"))
                fileName = fileName.split("?")[0]

            // if the filename is an actual filename, and not a link
            if (fileName.contains(".")
                && fileName.length != patreonMedia.mediaAttributes.downloadUrl.length
                && !fileName.startsWith("https://")
            ) {
                if (fileName.contains("/")) {
                    return fileName.split("/").last()
                }
                return fileName
            } else if (!fileName.contains("/")) {
                // println(patreonMedia)
            }
        }

        // try to guess from the url
        var potentialGuess = guessNameFromURL(patreonMedia.mediaAttributes.downloadUrl)

        // check whether it's still a link
        if ((!potentialGuess.startsWith("https://") || !potentialGuess.contains("patreon.com"))
            && potentialGuess.contains(".")
        ) {
            if (potentialGuess.contains("?")) {
                potentialGuess = potentialGuess.split("?").first()
            }
            return potentialGuess
        }

        // if nothing works, force mediaid.ext ending, only yet observed on png/jpg

        var mediaFileEnding: String = patreonMedia.mediaAttributes.downloadUrl

        //mediaFileEnding = patreonMedia.mediaAttributes.downloadUrl.split(".").last()
        //if (mediaFileEnding.contains("?"))
        //    mediaFileEnding = mediaFileEnding.split("?").first()

        if (mediaFileEnding.length > 5) {
            mediaFileEnding = when (patreonMedia.mediaAttributes.mimetype) {
                "image/png" ->
                    "png"
                "image/jpeg" ->
                    "jpg"
                "image/webp" ->
                    "webp"
                else -> "unknown"
            }
        }
        return patreonMedia.id.toString() + "." + mediaFileEnding
    }

    // https://stackoverflow.com/a/15191508/11324248
    open fun flattenToAscii(string: String): String? {
        var string = string
        val sb = StringBuilder(string.length)
        string = Normalizer.normalize(string, Normalizer.Form.NFD)
        for (c in string.toCharArray()) {
            if (c <= '\u007F') sb.append(c)
        }
        return sb.toString()
    }
}