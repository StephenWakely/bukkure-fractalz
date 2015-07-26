# bukkure-fractalz
Creating fractals in Minecraft using Clojure with the [Bukkure plugin](https://github.com/SevereOverfl0w/bukkure).

To generate these triangles connect a repl to a Spigot server running Bukkure and run the following function within the `fractalz.core` namespace :

```
(make-sierpinsky-pyramid 100 "<The name of your player>")
```

Or from within the Minecraft console run

```
  /sierpinsky 100
```

Replace 100 with the size of the triangle you want to create.


## Sierpinsky Pyramids

![sierpinsky pyramid](./shot2.png)
![sierpinsky pyramid](./shot1.png)
