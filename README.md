#ldap-test

##Building

Run `sbt assembly` to package executable (under linux) jar in `target/scala-2.11/ldap-test`.

##Running

See the help for options:

    ldap-test 1.0
    Usage: ldap-test [options] host
    
    a tool to run tests on connection losses to ldap-servers
      -n <value> | --num-trials <value>
            number of connection requests to produce; default is 100
      -f <value> | --start-freq <value>
            average number of requests to start per second (determines runtime, number of parallel connections)
      -m <value> | --mean-duration <value>
            mean of normal distribution used to sample connection durations; default 32
      -s <value> | --sd-duration <value>
            standard deviation of normal distribution used to sample connection durations; default 2
      -t <value> | --truncate <value>
            truncate normal distribution at sd times this value; default 3
      host
            host name to connect to

The program opens a given number of connections uniformly distributed over some time frame.

Each connection is tested after a certain duration that is sampled from a truncated normal distribution.

##Output
`STDERR` produces some logging messages of the LDAP-backend and can be ignored.

The program takes a while to run, depending on the parameters, so be patient (and know what you're doing).

Output is a tab-separated CSV with 

 - starting time
 - end time
 - duration
 - success (false if the connection was lost)
 
 