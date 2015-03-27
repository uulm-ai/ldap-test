package ldapmon

import java.util.Date

import org.apache.directory.api.ldap.model.cursor.EntryCursor
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.ldap.client.api.LdapNetworkConnection
import rx.lang.scala.Observable

import scala.concurrent.duration.{FiniteDuration, Duration}
import scala.util.{Random, Try}

object Main {
  case class Conf(host: String,
                  basedn: String = "",
                  n: Int = 100,
                  startsPerSec: Double = 3,
                  meanDuration: Double = 32,
                  sdDuration: Double = 2,
                  truncate: Double = 3)

  val parser = new scopt.OptionParser[Conf]("ldap-test"){
    head("ldap-test","1.0")
    note("a tool to run tests on connection losses to ldap-servers")
    opt[Int]('n',"num-trials")
      .action{case (x,c) => c.copy(n=x)}
      .text("number of connection requests to produce; default is 100")
    opt[Double]('f',"start-freq")
      .action{case (x,c) => c.copy(startsPerSec = x)}
      .text("average number of requests to start per second (determines runtime, number of parallel connections)")
    opt[Double]('m',"mean-duration")
      .action{case (x,c) => c.copy(meanDuration = x)}
      .text("mean of normal distribution used to sample connection durations; default 32")
    opt[Double]('s',"sd-duration")
      .action{ case (x,c) => c.copy(sdDuration = x)}
      .text("standard deviation of normal distribution used to sample connection durations; default 2")
    opt[Double]('t',"truncate")
      .action{case (x,c) => c.copy(truncate = x)}
      .text("truncate normal distribution at sd times this value; default 3")
    arg[String]("host")
      .action{case (x,c) => c.copy(host = x)}
      .text("host name to connect to")
  }
  def test(host: String, base: String, waitingTime: Duration): Observable[(Date,Date,Boolean)] = {
    val conn = new LdapNetworkConnection(host)
    conn.connect()
    val startTime = new Date()

    def testRequest(): Boolean = {
      Try {
        //the idea is that the following request fails with an exception,
        //if the connection has been closed or interrupted
        val res: EntryCursor = conn.search("", "(uid=tgeier)", SearchScope.SUBTREE)
        res.iterator().hasNext //we need to use the request, otherwise it will be discarded by the library
        res.close()
      }.isSuccess
    }

    Observable.timer(waitingTime).map{ _ =>
      (startTime,new Date(),testRequest())
    }
  }

  def prettyResult(entry: (Date,Date,Boolean)): String = {
    val (s,e,r) = entry
    val sTime: Long = s.getTime
    val eTime: Long = e.getTime
    f"$sTime\t$eTime\t${(eTime - sTime).toDouble / 1000}%.2f\t$r"
  }

  def main(args: Array[String]): Unit = {
    parser.parse(args, Conf("fs01.informatik.uni-ulm.de")).foreach { c =>

      val r = new Random()
      def genDur(n: Int): Seq[Duration] =
        Iterator.continually(r.nextGaussian() * c.sdDuration + c.meanDuration)
          .filter(d => d > 0 && math.abs(c.meanDuration - d) < c.sdDuration * c.truncate)
          .map(d => Duration((d * 1000).toLong, "ms")).take(n).toIndexedSeq

      val startDelay: Seq[FiniteDuration] = Seq.fill(c.n)(Duration((r.nextDouble() * (c.n / c.startsPerSec) * 1000).toLong, "ms"))
      val plan: Seq[(Duration, Duration)] = startDelay zip genDur(c.n)

      System.err.println("running test...")

      val result: List[(Date, Date, Boolean)] = Observable.from(plan).flatMap { case (delay, dur) =>
        Observable.timer(delay).flatMap(_ => test(c.host, c.basedn, dur))
      }.toBlocking.toList

      System.err.println("done...")

      println("time.start\ttime.end\tduration\tsuccess")
      println(result.map(prettyResult).mkString("\n"))
    }
  }
}
