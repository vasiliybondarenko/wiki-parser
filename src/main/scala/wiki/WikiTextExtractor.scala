package wiki

import scala.util.matching.Regex

/**
 * Created by Bondarenko on Oct, 31, 2019
 * 17:23.
 * Project: Wikipedia
 */
trait WikiTextExtractor {

  implicit class P(s: String) {
    def replaceAllBy(reg: Regex, matcher: Regex.Match => String) = reg.replaceAllIn(s, matcher)
  }

  def proceedLine(str: String): String = str.trim match {

    case s
        if s.startsWith("{{") || s.endsWith("}}") || s.startsWith("{|") || s.startsWith("*") || s
          .startsWith("[[File:") || s.startsWith("|") =>
      ""
    case s =>
      val simpleLinkPattern = """\[\[([a-zA-Z ,]+)\]\]""".r
      val linkPattern = """\[\[([^]^\|]+)\|([^]]+)\]\]""".r

      s.replaceAll("&lt;ref(.*)/ref&gt", "")
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
