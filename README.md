wicket-console
==============

Wicket Console for JVM 1.7

Key features:

1. Very small size
2. Ajax enabled
3. Contextual
  * ScriptContext has been stored on server side: you can write function and then call from subsequent commands
4. Embeddedable into your wicket pages
5. Available throught Wicket debug panel (in development)

Build project:
```bash
cd wicket-console
mvn clean install
```
   
Run demo:
```bash
cd wicket-console-demo
mvn jetty:run
```

Then goto http://localhost:8081/
