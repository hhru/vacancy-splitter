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
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class SearchController {

  @Autowired
  UrlFetcher urlFetcher;

  @RequestMapping(method = RequestMethod.GET)
  public String search(Model model,
                       @RequestParam(required = false, defaultValue = "") String query,
                       @RequestParam(required = false, defaultValue = "0") int page) {
    List<Vacancy> vacancies = new ArrayList<>();
    String encodedQuery;

    int pages = 50;

    try {
      encodedQuery = URLEncoder.encode(query, "utf-8");
      String searchResult = urlFetcher.fetchUrl(
          String.format("https://api.hh.ru/vacancies?text=%s&page=%d&per_page=40", encodedQuery, page));
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(searchResult);
      if (node.hasNonNull("items")) {
        ArrayNode items = (ArrayNode) node.get("items");
        for (JsonNode item : items) {
          Vacancy vacancy = new Vacancy();
          vacancy.id = item.get("id").asInt();
          vacancy.name = item.get("name").asText();
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

  public class Vacancy {
    public int id;
    public String name;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

}
