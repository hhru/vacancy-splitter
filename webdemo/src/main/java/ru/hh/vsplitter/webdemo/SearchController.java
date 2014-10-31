package ru.hh.vsplitter.webdemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Throwables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.hh.vsplitter.split.VacancyBlock;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class SearchController {

  @Autowired
  UrlFetcher urlFetcher;
  @Autowired
  ThreadSafeSplitter splitter;

  private static final int MAX_PAGES = 50;
  private static final int PAGE_SIZE = 20;
  private static final int MAX_SNIPPET_LENGTH = 600;

  @RequestMapping(method = RequestMethod.GET)
  public String search(Model model,
                       @RequestParam(required = false, defaultValue = "") String query,
                       @RequestParam(required = false, defaultValue = "0") int page) {
    List<Vacancy> vacancies = new ArrayList<>();
    String encodedQuery;

    int pages = MAX_PAGES;

    try {
      encodedQuery = URLEncoder.encode(query, "utf-8");
      String searchResult = urlFetcher.fetchUrl(
          String.format("https://api.hh.ru/vacancies?text=%s&page=%d&per_page=%d", encodedQuery, page, PAGE_SIZE));
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(searchResult);
      if (node.hasNonNull("items")) {
        ArrayNode items = (ArrayNode) node.get("items");
        for (JsonNode item : items) {
          Vacancy vacancy = new Vacancy();
          vacancy.id = item.get("id").asInt();
          vacancy.name = item.get("name").asText();
          if (!item.get("salary").isNull()) {
            int salary = item.get("salary").asInt();
            if (salary > 0) {
              vacancy.salary = salary;
            }
          }

          JsonNode vacancyJson = urlFetcher.getVacancy(vacancy.id);
          Map<VacancyBlock, String> blocks = splitter.split("<div>" + vacancyJson.get("description").asText() + "</div>");

          vacancy.requirements = shorten(blocks.get(VacancyBlock.REQUIREMENTS), MAX_SNIPPET_LENGTH);
          vacancy.responsibilities = shorten(blocks.get(VacancyBlock.RESPONSIBILITIES), MAX_SNIPPET_LENGTH);

          vacancies.add(vacancy);
        }
      }
      pages = Math.min(pages, node.get("pages").asInt());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    model.addAttribute("vacancies", vacancies);
    model.addAttribute("query", query);
    model.addAttribute("encodedQuery", encodedQuery);
    model.addAttribute("page", page);
    model.addAttribute("pages", pages);

    return "index";
  }

  private static String shorten(String text, int limit) {
    if (text != null && text.length() > limit) {
      return text.substring(0, limit) + "...";
    } else {
      return text;
    }
  }
}
