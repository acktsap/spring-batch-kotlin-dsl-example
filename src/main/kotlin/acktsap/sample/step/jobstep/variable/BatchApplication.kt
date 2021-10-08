package acktsap.sample.step.jobstep.variable

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
    fun beforeJob(jobBuilderFactory: JobBuilderFactory, stepBuilderFactory: StepBuilderFactory): Job {
        val anotherJob = jobBuilderFactory.get("anotherJob")
            .start(
                stepBuilderFactory.get("anotherStep")
                    .allowStartIfComplete(true)
                    .tasklet { _, _ ->
                        println("run anotherTasklet")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .build()

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
        val anotherJob = batch {
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

        job("afterJob") {
            steps {
                step("testStep") {
                    job(anotherJob)
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
