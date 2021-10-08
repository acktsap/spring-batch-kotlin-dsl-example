package acktsap.sample.job.flowjob.split.variable

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.extensions.kotlindsl.configuration.BatchDsl
import org.springframework.batch.extensions.kotlindsl.configuration.EnableBatchDsl
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.task.SimpleAsyncTaskExecutor

@SpringBootApplication
@EnableBatchProcessing
@EnableBatchDsl
class BatchApplication {
    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
    ): Job {
        val testFlow1 = FlowBuilder<Flow>("testFlow1")
            .start(
                stepBuilderFactory.get("flowStep1")
                    .tasklet { _, _ ->
                        println("run tasklet1")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .build()
        val testFlow2 = FlowBuilder<Flow>("testFlow2")
            .start(
                stepBuilderFactory.get("flowStep2")
                    .tasklet { _, _ ->
                        println("run tasklet2")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .build()

        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory
                    .get("dummyStep")
                    .tasklet { _, _ ->
                        println("run dummyTasklet")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .split(SimpleAsyncTaskExecutor())
            .add(testFlow1, testFlow2)
            .end()
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        val testFlow1 = batch {
            flow("testFlow1") {
                step("flowStep1") {
                    tasklet { _, _ ->
                        println("run tasklet1")
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        job("afterJob") {
            flows {
                split(SimpleAsyncTaskExecutor()) {
                    flow(testFlow1)
                    flow("testFlow2") {
                        step("flowStep2") {
                            tasklet { _, _ ->
                                println("run tasklet2")
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
