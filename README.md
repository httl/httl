Hyper-Text Template Language(`HTTL`) and Engine
================================================

Homepage: http://httl.github.io

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
【File】 -> 【Import】 -> 【Existing Projects into Workspace】 -> 【Browse】(Select Project directory and open)
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
