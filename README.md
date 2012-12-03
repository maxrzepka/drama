# Clojure drama

A Clojure drama to play alone, in parallel or in group.
The plot is in 3 acts :

   * Get some data from the web : here all plays and their characters of a famous writer
   * Question the data
   * Happy End : make a web site to present your results

This project intends to be a gentle way to dive into clojure ecosystem.

You maybe trained your clojure skills on some 4clojure problems but why not just start directly to do something real.

And on the way it will give you the feel of the clojure power : be declarative and concise, stay focused on data and functions.

[cascalog](https://github.com/nathanmarz/cascalog) , [enlive](https://github.com/cgrand/enlive) , [ring](https://github.com/mmcgrana/ring), [moustache](https://github.com/cgrand/moustache) are the libraries used here.

But most of new comers see clojure as unusual and cryptic. Such expressions like
`[:div#main :ul :li [:a (h/attr= :href "/")]]` or
`(<- [?name ?sum] (persons ?name ?amount) (co/sum ?amount :>  ?sum))`
 are in fact a concise, elegant ways to express your logic.

This drama is an attempt to lower the entrance barrier of clojure
and hope you will enjoy the simplicity of clojure as I do.

A html source transcript is also available http://maxrzepka.github.com/drama/doc.html
and also screencasts [act 1](https://vimeo.com/54718414) [act 2](https://vimeo.com/54718413) [act 3](https://vimeo.com/54591160)



## Act 1 : Scraping with enlive

Fetch all plays of a famous writer along with their characters.
Data coming from [this web site](toutmoliere.net) .

You may consider my other project https://github.com/maxrzepka/clojure-by-example to play with enlive selectors available on [heroku](http://cold-dusk-9608.herokuapp.com/).

## Act 2 : Querying with cascalog

No need to know Hadoop or map/reduce paradigms to play with cascalog.
Just try it on the repl.

## Act 3 : Back to the Web with ring, enlive and moustache

Building web app shoud be simple and with clojure it is.

## Usage

Clone it Fork it ... This project is a continuous work-in-progress.
Feel free to use it as you like.

To launch the web app on localhost:8080 , just execute `lein run`

Your comments/questions/contributions are more than welcome.
