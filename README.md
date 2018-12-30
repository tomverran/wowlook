## Wow, look!

A charting library for Scala and Scala JS.
This is currently in the **very early** stages of development so doesn't work really.
I'm really only uploading this because I'd be sad if my PC died and I lost all the code.

### Motiviation

I wanted to generate some charts based on some data I have in both the backend
(for static HTML files) and the frontend (for an admin tool) and I didn't have much fun
trying to either use existing Java libraries or Google Charts with ScalaJS.

As such this library generates simple (maybe _too_ simple) SVG charts.
An example of use can be found in `jvm/.../Examples.scala`

### To do

- An actually working distribution graph
- A line graph, used as the basis for the above
- A key so you can see what the series are
- Some actual tests of some description
- Better use of CSS classes