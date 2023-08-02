![pluginIcon.svg](src%2Fmain%2Fresources%2FMETA-INF%2FpluginIcon.svg)
# ArcSupportPlugin

[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/22399-arcsupportplugin)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/22399-arcsupportplugin)

## Description

That plugin supports framework [Anuken/Arc](https://github.com/Anuken/Arc)

## [Previews](PREVIEWS.md)



## Features


#### Debug Renderers
- `arc.graphics.Color`
- `arc.files.Fi`

#### Debug Previews
- `arc.graphics.Color`

#### Line Markers
- manipulation `arc.graphics.Color`(calculation & editing color)
- displays buildin `arc.math.Interp` as interactive graphics

#### Inspections
- checks hex in `arc.graphics.Color.valueOf(__)`
- argument count checking in `Map.of` methods
- (raw) type checking in `Map.of` methods
- Detects wrong variadic parameter position in `CommandHandler`.
- Highlights parameters with duplicated names.

#### Completion & Resolving
- for properties in for `@property`
- for reflect members in `arc.util.Reflect`

#### Postfix Templates
- `intSet.iter` ->
    ```
  IntIterator iterator=intSet.iterator();
  while(iterator.hasNext){
      int next=iterator.next();
  }```
- `intSet.seq` -> `intSet.iterator().toArray()`
- `dataOutput.writes` -> `new Writes(dataOutput)`
- `dataInput.reads` -> `new Reads(dataInput)`
- `bytes`->`new Reads(new DataInput(bytes))`

### Added Languages
#### language for supporting params in arc.util.CommandHandler(LanguageID ArcCommandParams)
- Parsing
- Highlighting
- BraceMatching
- Injection in arc.util.CommandHandler.register
