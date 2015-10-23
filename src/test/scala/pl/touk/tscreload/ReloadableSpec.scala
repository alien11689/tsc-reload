/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.touk.tscreload

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util.function.Function

import com.typesafe.config.Config
import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

class ReloadableSpec extends FlatSpec with Matchers with GivenWhenThen{

  it should "reload nested value after change" in {
    Given("configuration file with initial value")
    val configFile = new File("target/foo.conf")
    def writeValueToConfigFile(value: Int) =
      Files.write(
        Paths.get(configFile.toURI),
        s"""foo {
            |  bar: $value
            |}
            |""".stripMargin.getBytes(Charset.forName("UTF-8"))
      )
    val initialFooBarValue = 1
    writeValueToConfigFile(initialFooBarValue)

    When("parse reloadable config file")
    val reloadable = ReloadableConfigFactory.parseFile(configFile)

    And("transform reloadable config to return nested value")
    val reloadableFooBar = reloadable.map(new Function[Config, Int] {
      override def apply(cfg: Config): Int = cfg.getInt("foo.bar")
    })

    Then("nested value should be same as initial")
    reloadableFooBar.currentValue() shouldEqual initialFooBarValue

    When("write new value to config file")
    val nextFooBarValue = 2
    writeValueToConfigFile(nextFooBarValue)

    Then("nested value should be same as new value")
    reloadableFooBar.currentValue() shouldEqual nextFooBarValue
  }

}