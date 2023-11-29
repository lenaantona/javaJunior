package ru.gb.lesson2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Lesson2MainTest {

  @AfterEach
  void afterEachTest() {

  }

  @Test
  void testToString() {
    Lesson2Main.Person person = new Lesson2Main.Person("Igor", 20);

    Assertions.assertEquals(person.toString(), "Igor - [20]");
  }

}