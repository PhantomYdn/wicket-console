wicket-console
==============

Wicket Console for JVM 1.6+

Key features:

1. Very small size (<25Kb)
2. Ajax enabled
3. Contextual
  * ScriptContext has been stored on server side: you can write function and then call from subsequent commands
4. Embeddedable into your wicket pages
5. Available throught Wicket DebugBar panel
  * Dependency to wicket-devutils is optional

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

Examples
--------
Print all properties of application
```javascript
for(var p in org.apache.wicket.Application.get())
{
   println(p);
}
```

Math calculation
```javascript
1+Math.sin(1)*2
```

