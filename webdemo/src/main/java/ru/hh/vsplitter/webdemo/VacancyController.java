package ru.hh.vsplitter.webdemo;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/vacancy")
public class VacancyController {
  @Autowired
  UrlFetcher urlFetcher;
  @Autowired
  MarkerInjector markerInjector;

  @RequestMapping(method = RequestMethod.GET)
  public String showVacancy(Model model, @RequestParam("vacancy_id") int vacancyId) {

    JsonNode node = urlFetcher.getVacancy(vacancyId);

    Vacancy vacancy = new Vacancy();
    vacancy.id = node.get("id").asInt();
    vacancy.name = node.get("name").asText();
    String description = node.get("description").asText();
    vacancy.description = markerInjector.inject("<div>" + description + "</div>");
    if (vacancy.description.startsWith("<div>")) {
      vacancy.description = vacancy.description.substring(5);
    }

    model.addAttribute("vacancy", vacancy);
    return "vacancy";
  }

}
