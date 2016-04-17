package bench

import org.openjdk.jmh.annotations._
import imp.imp
import java.util.concurrent.TimeUnit
import scala.reflect.ClassTag


@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class ImplVsImplicitly {

  @Benchmark
  def baseline =
     implicitly[ClassTag[Int]]

  @Benchmark
  def measure =
    imp[ClassTag[Int]]

}

