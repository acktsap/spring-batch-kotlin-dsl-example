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
    fun testJob1(batch: BatchDsl): Job = batch {
        job("testJob1") {
            steps {
                step("testStep1") {
                    allowStartIfComplete(true)
                    tasklet { _, _ ->
                        println("run testStep1Tasklet")
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
    }

    @Bean
    fun testJob2(batch: BatchDsl): Job = batch {
        job("testJob2") {
            steps {
                step("testStep2") {
                    allowStartIfComplete(true)
                    tasklet { _, _ ->
                        println("run testStep2Tasklet")
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
    }

    @Bean
    fun testJob3(batch: BatchDsl): Job = batch {
        job("testJob3") {
            steps {
                step("testStep3") {
                    allowStartIfComplete(true)
                    tasklet { _, _ ->
                        println("run testStep3Tasklet")
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
    }

    @Bean
    fun testJob4(batch: BatchDsl): Job = batch {
        job("testJob4") {
            steps {
                step("testStep4") {
                    allowStartIfComplete(true)
                    tasklet { _, _ ->
                        println("run testStep4Tasklet")
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
        @Qualifier("testJob1") testJob1: Job,
        @Qualifier("testJob2") testJob2: Job,
        @Qualifier("testJob3") testJob3: Job,
        @Qualifier("testJob4") testJob4: Job,
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("jobStep1")
                    .job(testJob1)
                    .build()
            )
            .next(
                stepBuilderFactory.get("jobStep2")
                    .job(testJob2)
                    .build()
            )
            .next(
                stepBuilderFactory.get("jobStep3")
                    .job(testJob3)
                    .build()
            )
            .next(
                stepBuilderFactory.get("jobStep4")
                    .job(testJob4)
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                step("jobStep1") {
                    jobBean("testJob1")
                }
                step("jobStep2") {
                    jobBean("testJob2")
                }
                step("jobStep3") {
                    jobBean("testJob3")
                }
                step("jobStep4") {
                    jobBean("testJob4")
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
