I've made this repo in order to benchmark https://github.com/non/imp
that claims to implement a faster `implicitly`.

I'm trying to check it's particular claim that `implicitly[ClassTag[Int]]` is slower than `imp[ClassTag[Int]]`.

I do it by using the best benchmarking tool available for JVM: http://openjdk.java.net/projects/code-tools/jmh/

I've checked. And what I can say is that on Oracle HotSpot version "1.8.0_72" it's not. Actually, both of them get optimized by JVM to be a simple load of same constant.

```
[info] Benchmark                       Mode  Cnt  Score   Error  Units
[info] ImplVsImplicitly.baseline       avgt   30  2.507 ± 0.031  ns/op
[info] ImplVsImplicitly.baseline:·asm  avgt         NaN            ---
[info] ImplVsImplicitly.measure        avgt   30  2.491 ± 0.044  ns/op
[info] ImplVsImplicitly.measure:·asm   avgt         NaN            ---
```

You can re-run those benchmarks by calling

  jmh:run -i 10 -wi 10 -f3 -t 1

Optionally, you could also use JVM dissasemmler plugin to see if generated assembly differs as I did.
The generated assembly was the same. Hotspot was able to optimize both of them to a single read of the very same constant object.


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
