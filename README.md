#Imp vs Implicitly
I've made this repo in order to benchmark https://github.com/non/imp
that claims to implement a faster `implicitly`.

I'm trying to check it's particular claim that `implicitly[ClassTag[Int]]` is slower than `imp[ClassTag[Int]]`.

##Benchmarking framework used
I do it by using the best benchmarking tool available for JVM: http://openjdk.java.net/projects/code-tools/jmh/

This is the state-of-the art tool avaliable for JVM. In this [thread] (https://groups.google.com/forum/#!topic/mechanical-sympathy/m4opvy4xq3U) you can see many problems that make most of benchmarking frameworks unreliable or simply wrong for that kind of benchmark.

## Result

I've checked. And what I can say is that on Oracle HotSpot version "1.8.0_72"(x86_64 ubuntu i7-4770) it's not. Actually, both of them get optimized by JVM to be a [simple load of same constant](https://github.com/DarkDimius/imp-bench/blob/master/baseline.assebly.txt#L71-L75).

```
[info] Benchmark                       Mode  Cnt  Score   Error  Units
[info] ImplVsImplicitly.baseline       avgt   30  2.507 ± 0.031  ns/op
[info] ImplVsImplicitly.baseline:·asm  avgt         NaN            ---
[info] ImplVsImplicitly.measure        avgt   30  2.491 ± 0.044  ns/op
[info] ImplVsImplicitly.measure:·asm   avgt         NaN            ---
```
## Run it yourself
You can re-run those benchmarks by executing in sbt promt:

     jmh:run -i 10 -wi 10 -f3 -t 1
## Runtime low-level details
I've had a look on assembly generated for both methods.
The generated assembly was the same. Hotspot was able to optimize both of them to a single read of the very same constant object.

[baseline.assembly.txt](https://github.com/DarkDimius/imp-bench/blob/master/baseline.assebly.txt) contains the assembly for the `implicitly[ClassTag[Int]]` where you can see that hotspot was able to get rid of all the overheads.

Most of those 2.5 ns are actually taken by benchmark framework itself, so the actual number running time is a bad indicator here. What is a good indicator is the assembly printout iself, that shows that hotspot was able to remove static loads and virtual calls in this example.

In order to reproduce this output you would need to run on Linux with `-prof perfasm`. You would also need a jvm that has dissassembler plugin installed. Setting it up is quite involved, and you'll likely need to build it from source of your version of JDK. Google for `hsdis` for more details.

# Bytecode difference
There is a small difference in the size of generated method:
`implicitly[ClassTag[Int]]` becomes

         0: getstatic     #13                 // Field scala/Predef$.MODULE$:Lscala/Predef$;
         3: getstatic     #18                 // Field scala/reflect/ClassTag$.MODULE$:Lscala/reflect/ClassTag$;
         6: invokevirtual #21                 // Method scala/reflect/ClassTag$.Int:()Lscala/reflect/ClassTag;
         9: invokevirtual #25                 // Method scala/Predef$.implicitly:(Ljava/lang/Object;)Ljava/lang/Object;
        12: checkcast     #27                 // class scala/reflect/ClassTag

while `impl[ClassTag[Int]]` becomes

         0: getstatic     #13                 // Field scala/reflect/ClassTag$.MODULE$:Lscala/reflect/ClassTag$;
         3: invokevirtual #16                 // Method scala/reflect/ClassTag$.Int:()Lscala/reflect/ClassTag;
         6: areturn


This may lead to methods containing many calls to `implicitly` being less likely to be inlined, but it would argue this is very uncommon.

## Contributions

I'd be very glad to see a PR that would prove me wrong. I'll learn something this way :-).

[@lloydmeta](https://github.com/lloydmeta) has contributed results from OSX (El Capitan), Oracle Java8 JDK 1.8.0_05:

    [info] Benchmark                  Mode  Cnt  Score   Error  Units
    [info] ImplVsImplicitly.baseline  avgt   30  3.268 ± 0.025  ns/op
    [info] ImplVsImplicitly.explicit  avgt   30  3.292 ± 0.036  ns/op
    [info] ImplVsImplicitly.imply     avgt   30  3.291 ± 0.039  ns/op

He's running a bit more of [code](https://github.com/lloydmeta/imp-bench/blob/b442424bc8c710cc524f5fe68963617b820d2953/src/main/scala/impbench/ImplVsImplicitly.scala) by computing `1 + 1` in a generic way. I didin't get assembly printout for his modification, but I assume that HotSpot was able to constant-fold the whole expression in a similar way.
