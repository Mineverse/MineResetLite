MineResetLite
=============

Lightweight implementation for automatic mine resets, now managed by Lyonix.

**Note from Sprock: God have mercy on any developer that has to touch this "lightweight" plugin**

## Maven
You have to install into your repo then you can do
``` 
<dependency>
  <groupId>com.koletar.jj</groupId>
  <artifactId>mineresetlite</artifactId>
  <version>0.5.0</version>
</dependency>
```

## Api
``` 
Mine mine = MineResetAPI.getMine(x, y, z);
mine.reset();
```