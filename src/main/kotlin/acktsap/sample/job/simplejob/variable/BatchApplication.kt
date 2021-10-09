package acktsap.sample.job.simplejob.variable

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.extensions.kotlindsl.configuration.BatchDsl
import org.springframework.batch.extensions.kotlindsl.configuration.EnableBatchDsl
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

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
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("testStep1")
                    .tasklet { _, _ ->
                        println("run testTasklet1")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .next(
                stepBuilderFactory.get("testStep1")
                    .tasklet { _, _ ->
                        println("run testTasklet2")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                step("testStep1") {
                    tasklet { _, _ ->
                        println("run testTasklet1")
                        RepeatStatus.FINISHED
                    }
                }
                step("testStep2") {
                    tasklet { _, _ ->
                        println("run testTasklet2")
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
