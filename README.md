## Wow, look!

[![Build Status](https://travis-ci.org/tomverran/wowlook.svg?branch=master)](https://travis-ci.org/tomverran/wowlook) [![Download](https://api.bintray.com/packages/tomverran/maven/wowlook/images/download.svg) ](https://bintray.com/tomverran/maven/wowlook/_latestVersion)

A charting library for Scala and Scala JS.
This is currently in the **very early** stages of development

### Installation

This library is available on Bintray, so in your `build.sbt`:

```scala
resolvers += Resolver.bintrayRepo("tomverran", "maven")
libraryDependencies += "io.tvc" %% "wowlook" % "0.3.0" // replace %% with %%% for ScalaJS
```

### Motiviation

I wanted to generate some charts based on some data I have in both the backend
(for static HTML files) and the frontend (for an admin tool) and I didn't have much fun
trying to either use existing Java libraries or Google Charts with ScalaJS.

As such this library generates simple (maybe _too_ simple) SVG charts.
An example of use can be found in `examples/shared/.../Examples.scala`

### Screenshots

These are screenshots of the examples:

#### Bar Chart

![image](https://user-images.githubusercontent.com/1388226/50562948-a9bec380-0d10-11e9-9654-fb1124a430d0.png)

#### Line Chart

![image](https://user-images.githubusercontent.com/1388226/50562946-a0cdf200-0d10-11e9-86d5-09ba94d4a9c6.png)

