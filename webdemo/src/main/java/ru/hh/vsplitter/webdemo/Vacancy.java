package ru.hh.vsplitter.webdemo;

public class Vacancy {
  public int id;
  public String name;
  public String description;
  public Integer salary;

  public String requirements;
  public String responsibilities;
  public String conditions;

  // getters are only for jstl
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

  public String getRequirements() {
    return requirements;
  }

  public String getResponsibilities() {
    return responsibilities;
  }

  public String getConditions() {
    return conditions;
  }

  public Integer getSalary() {
    return salary;
  }

}
