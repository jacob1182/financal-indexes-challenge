## Topics

#### Requirements
Three endpoints:
- The first one is called every time we receive a tick. It is also the sole input of this rest API.
- The second one returns the statistics based on the ticks of all instruments of the last 60 seconds
(sliding time interval)
- The third one returns the statistics based on the ticks of one instrument of the last 60 seconds
(sliding time interval).


1. [Installation](#installation)
2. [Assumptions](#assumptions)
3. [Points of improvements](#points-of-improvements)
4. [Personal considerations](#personal-considerations)

#### Installation

###### Requirements
1. Java 11+
2. Maven (to build the application)

This is a Spring Boot application which ease installation using fat jars.

Command to build the application:
```
mvn package
``` 
Command to run the application:

```
java -jar path/to/application.jar
``` 


#### Assumptions

The way to retrieve the statistics any moment in a constant time is to have it pre-calculated before retrieving it.

The stat is computed in two moments:
1. Every tick insertion.
2. Asynchronous recalculation every 200ms.

There is still a possibility of having some expired ticks during the 200ms window. Therefore, is possible to retrieve a stale statistic for 200ms, which is considered is accepted.

#### Points of improvements

Is possible to improve the calculation using balance tree. In order to find the max/min in a `O(log(n))` time.

The challenge here is to avoid having 2 data structures, because is needed an ordered collection by timestamp, and another by price. Using 2 TreeSet will add time complexity during additions and removals.

I was working on a [MultiplePriorityQueue](src/test/java/com/example/financialindexes/experimental/MultiplePriorityQueue.java) class which is a Linked Queue allowing to have a single collection sorted by different criterias.
 
Each node point to different nodes depending on the comparator used. 

Having a single linked nodes allows you to have the first/last value in `O(1)`. But additions, in this case takes `O(n*n)`. Is possible to use the same idea but using Balanced Tree it will improve additions to `m*log(n)` time, being `m` the amount of comparators used, `m = 2` in this case.

#### Personal considerations

This was a nice challenge. A simple logic with constraints makes it to re-think the whole logic, which is an opportunity to bring all your skills to come out with solution as nice as possible. Code, learn and have fun.
