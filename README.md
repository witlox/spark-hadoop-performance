# Extremely simplistic Spark performance testing [![Build Status](https://travis-ci.org/witlox/spark-hadoop-performance.svg?branch=master)](https://travis-ci.org/witlox/spark-hadoop-performance)

The following happens:
- generate a bunch of random numbers
- write these to storage
- read them back from storage
- identify primes
- report the time everything took

Submission example:
```bash
spark-submit --master yarn --deploy-mode cluster spark-hadoop-performance-assembly-1.0.jar --records 1000000000 --partitions 792 --output /benchmark
```
