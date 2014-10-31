package ru.hh.vsplitter.webdemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.hh.vsplitter.split.VacancyBlock;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/vacancy")
public class VacancyController {
  private static final ObjectMapper mapper = new ObjectMapper();
  @Autowired
  UrlFetcher urlFetcher;
  @Autowired
  MarkerInjector markerInjector;
  @Autowired
  ThreadSafeSplitter splitter;

  @RequestMapping(method = RequestMethod.GET)
  public String showVacancy(Model model, @RequestParam("vacancy_id") int vacancyId) {
    String vacancyJson = urlFetcher.fetchUrl(String.format("https://api.hh.ru/vacancies/%d", vacancyId));
    try {
      JsonNode node = mapper.readTree(vacancyJson);

      Vacancy vacancy = new Vacancy();
      vacancy.id = node.get("id").asInt();
      vacancy.name = node.get("name").asText();
      String description = node.get("description").asText();
      vacancy.description = markerInjector.inject("<div>" + description + "</div>");
      if (vacancy.description.startsWith("<div>")) {
        vacancy.description = vacancy.description.substring(5);
      }

      model.addAttribute("vacancy", vacancy);

      Map<VacancyBlock, String> blocks = splitter.split("<div>" + description + "</div>");
      model.addAttribute("req", blocks.get(VacancyBlock.REQUIREMENTS));
      model.addAttribute("res", blocks.get(VacancyBlock.RESPONSIBILITIES));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    return "vacancy";
  }

  public class Vacancy {
    public int id;
    public String name;
    public String description;

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

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }

}
