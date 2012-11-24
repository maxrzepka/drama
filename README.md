# Clojure drama

A Clojure drama to play alone, in parallel or in group.
The plot is in 3 acts :

   * Get some data from the web : here all plays and their characters of a famous writer
   * Question the data
   * Happy End : make a web site to present your results

This project intends to be a gentle way to dive into clojure ecosystem.
Instead of learning clojure with factorial or some 4clojure problems,
why not just start directly to do something real.
And on the way to give you the feel of the clojure power : be declarative , data and functions , concise and clear syntax.

[cascalog](https://github.com/nathanmarz/cascalog) , [enlive](https://github.com/cgrand/enlive) , [ring](https://github.com/mmcgrana/ring), [moustache](https://github.com/cgrand/moustache) are ideal examples of this new way of programming.

But most of new comers see clojure as unusual and cryptic. Such expressions like
`[:div#main :ul :li [:a (h/attr= :href "/")]]` or
`(<- [?name ?sum] (persons ?name ?amount) (co/sum ?amount :>  ?sum))`
 are in fact a concise, elegant, powerful ways to express your logic.

This drama is an attempt to lower the entrance barrier of clojure
and hope you will enjoy the beauty of clojure as I do.

A html source transcript is also available http://maxrzepka.github.com/drama/doc.html

You may consider my other project https://github.com/maxrzepka/clojure-by-example to play with enlive selectors on [heroku](http://cold-dusk-9608.herokuapp.com/).

## Act 1 : Scraping with enlive

Fetch all plays along with their characters of a famous writer.
Data coming from plain web site .

## Act 2 : Querying with cascalog

No need to know Hadoop or map/reduce paradigms to make cascalog queries.
Just try it on the repl.

## Act 3 : build a Web App with ring , enlive and moustache

Building web app shoud be simple and with clojure it is.

## Usage

Clone it Fork it ... This project is a continuous work-in-progress. Feel free to use it as you like.

To launch the web app on localhost:8080 , just execute `lein run`
