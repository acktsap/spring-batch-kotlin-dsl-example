package acktsap.sample.step.jobstep.bean

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.extensions.kotlindsl.configuration.BatchDsl
import org.springframework.batch.extensions.kotlindsl.configuration.EnableBatchDsl
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
@EnableBatchDsl
class BatchApplication {
    // common
    @Bean
    fun anotherJob(batch: BatchDsl): Job = batch {
        job("anotherJob") {
            steps {
                step("anotherStep") {
                    allowStartIfComplete(true)
                    tasklet { _, _ ->
                        println("run anotherTasklet")
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
    }

    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
        @Qualifier("anotherJob") anotherJob: Job
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .job(anotherJob)
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                step("testStep") {
                    jobBean("anotherJob")
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
