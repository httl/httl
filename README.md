Hyper-Text Template Language(`HTTL`) and Engine
================================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.httl/httl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.httl/httl/)
[![GitHub release](https://img.shields.io/github/release/httl/httl.svg)](https://github.com/httl/httl/releases)  
[![GitHub issues](https://img.shields.io/github/issues/httl/httl.svg)](https://github.com/httl/httl/issues)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)  
[![Star](https://img.shields.io/github/stars/httl/httl.svg)](https://github.com/httl/httl/stargazers)
[![Fork](https://img.shields.io/github/forks/httl/httl.svg)](https://github.com/httl/httl/fork)

Documentation
---------------------

- Homepage: [English](http://httl.github.io/en/) | [中文](http://httl.github.io/zh/)
    - Example: [English](http://httl.github.io/en/example.html) | [中文](http://httl.github.io/zh/example.html)
    - User Guide
        - Syntax: [English](http://httl.github.io/en/syntax.html) | [中文](http://httl.github.io/zh/syntax.html)
        - Config: [English](http://httl.github.io/en/config.html) | [中文](http://httl.github.io/zh/config.html)
    - Develop Guide
        - Integration: [English](http://httl.github.io/en/integration.html) | [中文](http://httl.github.io/zh/integration.html)
        - Design: [English](http://httl.github.io/en/design.html) | [中文](http://httl.github.io/zh/design.html)
    - Help(FAQ/Team members): [English](http://httl.github.io/en/help.html) | [中文](http://httl.github.io/zh/help.html)

Download
-------------------

https://github.com/httl/httl/tags

Or clone project repository:

```bash
git clone https://github.com/httl/httl.git
```

Compile
----------------------

```bash
mvn install -Dmaven.test.skip
```

Import to `IDE`
-----------------------

### `Eclipse`

First generate `eclipse` project files by `mvn` command:

```bash
mvn eclipse:eclipse -DdownloadSources
```

Then import project into `eclipse`:

```
【File】 -> 【Import】 -> 【Existing Projects into Workspace】 -> 【Browse】(Select project directory and open)
```

Associate `httl` file type with `html` editor:

```
【Window】 -> 【Preferences】 -> 【General】 -> 【Content Types】 -> 【Text】 -> 【HTML】 -> 【Add】 -> 【*.httl】
```

### `Intellij IDEA`

`Intellij IDEA` support `maven` as first citizen, just open `pom.xml`:

```
【File】 -> 【Open】 -> 【Browse】(Select root pom.xml file and open)
```
