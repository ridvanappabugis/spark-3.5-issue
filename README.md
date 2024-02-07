An example project showcasing a possible issue with Spark AQE's dynamic cache repartitioning mechanism.

The assembled jar should be run with:
```
spark-submit --class ExampleApp --packages org.apache.spark:spark-avro_2.12:3.5.0 --deploy-mode cluster --master spark://spark-master:6066 --conf spark.sql.autoBroadcastJoinThreshold=-1 --conf spark.eventLog.enabled=true --conf spark.eventLog.dir=file:///spark-event-log --conf spark.cores.max=3 --driver-cores 1 --driver-memory 1g --executor-cores 1 --executor-memory 1g /data/shared/test.jar
```

Pre-assembly - just point to the proper avro data paths.

Project:
- spark 3.5.0
- sbt
- scala 2.12

Description:
Due to AutoBroadcastJoin being disabled, Spark defaults to SortMergeJoin.
Cluster mode of 2 workers with (1/1 driver) and (2x 1/1 executor).

In the given example, a self-join of `parentDF` has been performed which is then cached, then the result is joined with `childDF`.

Expected behaviour:
Proper count with no data loss.

Actual behaviour:
Data loss, lesser than expected count.

Observed behaviour:
- Not reproducable on single executor flows
- There seems to be a file-size treshold after which dataloss is observed (possibly implying that it happens when both workers start reading the same data)
- Not reproducable by disabling `spark.sql.optimizer.canChangeCachedPlanOutputPartitioning`


