Hyper-Text Template Language and Engine.

Homepage:

http://httl.github.com

Checkout:

```bash
git clone https://github.com/httl/httl.git
```

Compile:

```bash
mvn install -Dmaven.test.skip
```

Test:

```bash
mvn test -Dtest=httl.test.PerformanceTest
```

Eclipse:

```bash
mvn eclipse:eclipse -DdownloadSources
```

```bash
Eclipse -> File -> Import -> Existing Projects into Workspace -> Browse -> Finished
```

```bash
Eclipse -> Window -> Preferences -> General -> Content Types -> Text -> HTML -> Add -> .httl
```