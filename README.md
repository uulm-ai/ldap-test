#ldap-test

##Building

Run `sbt assembly` to package executable (under linux) jar in `target/scala-2.11/ldap-test`.

##Running

See the help for options.

The program opens a given number of connections normally distributed over some time frame.

Each connection is tested after a certain duration that is sampled from a truncated normal distribution.

Output is a tab-separated CSV with 

 - starting time
 - end time
 - duration
 - success (false if the connection was lost)