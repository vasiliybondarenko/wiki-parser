package wiki

import scala.util.matching.Regex

/**
 * Created by Bondarenko on Oct, 31, 2019
 * 17:23.
 * Project: Wikipedia
 */
trait WikiTextExtractor {

  import wiki.WikiFilters.excludeTitle

  implicit class P(s: String) {
    def replaceAllBy(reg: Regex, matcher: Regex.Match => String) = reg.replaceAllIn(s, matcher)
  }

  private val simpleLinkPattern = """\[\[([^]^\|]+)\]\]""".r
  private val linkPattern = """\[\[([^]^\|]+)\|([^]]+)\]\]""".r

  private def collectByPattern(p: Regex, text: String)(group: Int) =
    for {
      p <- p.findAllMatchIn(text).toList
      l <- List(p.group(group))
    } yield l

  def collectWikiLinks(text: String) = {
    val allLinks = collectByPattern(linkPattern, text)(1) ::: collectByPattern(simpleLinkPattern,
                                                                               text)(1)
    allLinks
      .map(_.toLowerCase)
      .filterNot(excludeTitle)
  }

  def proceedLine(str: String): String = str.trim match {

    case s
        if s.startsWith("{{") || s.endsWith("}}") || s.startsWith("{|") || s.startsWith("*") || s
          .startsWith("[[File:") || s.startsWith("|") =>
      ""
    case s =>
      val simpleLinkPattern = """\[\[([a-zA-Z ,\-]+)\]\]""".r
      val linkPattern = """\[\[([^]^\|]+)\|([^]]+)\]\]""".r
      s.replaceAll("&lt;ref(.*)/ref&gt;", "")
        .replaceAll("&lt;ref([^/]+)/&gt;", "")
        .replaceAll("<ref[^<]+</ref>", "")
        .replaceAll("<ref[^/]+", "")
        .replaceAll("""\{\{[^}]+\}\}""", "")
        .replaceAll("""\{\{[^}]]+""", "")
        .replaceAllBy(simpleLinkPattern, _ => "$1")
        .replaceAll("""\[\[[a-zA-Z]+:[^]]+\]\]""", "")
        .replaceAllBy(linkPattern, _ => "$2")
  }

}

object WikiFilters {
  def excludeTitle(title: String) = {
    val p = "[a-zA-Z .,#\\(\\)-]+"

    val excludedPrefixes = List("category:", "file:")
    val excludedSuffixes = List(".png", ".svg", ".jpg", ".gif")
    (excludedSuffixes ::: excludedPrefixes).exists(title.trim.toLowerCase().contains(_)) || !title
      .matches(p)

  }
}
