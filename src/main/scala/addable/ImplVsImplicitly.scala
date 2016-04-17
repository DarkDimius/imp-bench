package addable

import java.util.concurrent.TimeUnit

import imp.imp
import org.openjdk.jmh.annotations._


@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class ImplVsImplicitlyAddable {

  def addTogetherBase[A: Addable](x: A, y: A) = implicitly[Addable[A]].add(x, y)
  def addTogetherImp[A: Addable](x: A, y: A) = imp[Addable[A]].add(x, y)
  def addTogetherExplicit[A](x: A, y: A)(implicit addable: Addable[A]) = addable.add(x, y)

  @Benchmark
  def baseline = addTogetherBase(1, 1)

  @Benchmark
  def imply = addTogetherImp(1, 1)

  @Benchmark
  def explicit = addTogetherExplicit(1, 1)
}
