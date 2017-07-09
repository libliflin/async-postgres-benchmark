# postgres-async-driver throughput benchmark

### Framework Benchmarks

This code was copied from FrameworkBenchmarks/light-java. Please 
see FrameworkBenchmarks for setup instructions and licenses. 

I have modified it by adding `postgres-async-driver` to the `pom.xml` file.

        <dependency>
            <groupId>com.github.alaisi.pgasync</groupId>
            <artifactId>postgres-async-driver</artifactId>
            <version>0.9</version>
        </dependency>

I have modified the `PostgresStartupookProvider` to start up the async.

I have also added an async handler `QueriesAsyncPostgresqlGetHandler`
that does the same work that the regular update handler does. It listens on `/queries2`


### Setup

Prerequisites:
1. IntelliJ IDEA
2. Apache JMeter
    1. I had to mess with hidpi settings in `jmeter.properties`
3. [go-wrk](https://github.com/adjust/go-wrk)
4. local postgres installed with all files from the postgres setup 

Running instructions:
1. Clone repo
2. To setup testing, just follow light4j instructions to run (make new run configuration with class `com.networknt.server.Server`)
3. Use Apache JMeter to test throughput. 
4. Use go-wrk to test throughput.


JMeter Configuration:

* (Thread Group) Number of Threads (2, 4, or 8)
* (Thread Group) Ramp up: 0
* (Thread Group) Loop Count: forever
* (HTTP Request) protocol: http
* (HTTP Request) server name: localhost
* (HTTP Request) Port Number: 8080
* (HTTP Request) Path: (/queries and /queries2)
* (HTTP Request) Method: GET
* (HTTP Request) Parameters: queries=10
* (Summary Report) 

go-wrk instructions:

    go-wrk -t 2 -m 10000 http://localhost:8080/queries?queries=10
    go-wrk -t 2 -m 10000 http://localhost:8080/queries2?queries=10

### Experiment Results

I have a Dell XPS 13 inch with UHD; Windows 10 Home; i7-6560U@2.20 GHz; 16GB RAM.

I started the load and kept clearing to get latest throughput. 
After 15 seconds it calmed down for both. 

For reference, the data point on framework benchmark's site for this
configuration (queries = 10; light-java; postgres) is 18,069.

I downloaded [go-wrk](https://github.com/adjust/go-wrk) to test as well; 2 Threads; 10,000 requests; 100 connections.

| Benchmark Config | /queries?queries=10 req/s | /queries2?queries=10 req/s |
|------------------|---------------------------|----------------------------|
| JMeter 2 Threads |                      2736 |                       1907 |
| JMeter 4 Threads |                      4904 |                       2208 |
| JMeter 8 Threads |                      5079 |                       2129 |
| go-wrk           |                      1870 |                        944 |


### Conclusions

It is obviously not my day job to do high throughput systems. I cannot even reproduce 
within 2x as slow what FrameworkBenchmarks was able to produce. On top of it, I don't 
even know where to start profiling this. My first and second attempts are not 
showing any strong results. I've never been able to reproduce the 
large req/s from FrameworkBenchmarks code and I don't know if that's from
my lack of access to awesome hardware, or my poor configuration skills.


Light-4j pros:

1. Starts in milliseconds. This is huge.
2. Easy to debug/restart/try new things.
3. Performance oriented. After configuring postgres correctly, I was able to get high throughput for a windows machine. 

Light-4j cons:

1. Documentation. I think there is a documentation
   rewrite happening, as it's extremely hard to navigate
   and mostly about philosophy and approaches.
2. Because of the documentation issue, I'm not sure what it would add to day to day development.
    1. When I added my class; it was an undertow provider.
    2. When I implemented my class, I just used async objects.
    
### References

* [go-wrk](https://github.com/adjust/go-wrk)
* [Apache JMeter](http://jmeter.apache.org/)
* [TechEmpower Web Framework Benchmarks](https://www.techempower.com/benchmarks/)
* [postgres-async-driver](https://github.com/alaisi/postgres-async-driver)
* [Light 4j](https://github.com/networknt/light-4j)
* [Undertow](http://undertow.io/)
* [DSL-JSON](https://github.com/ngs-doo/dsl-json)
