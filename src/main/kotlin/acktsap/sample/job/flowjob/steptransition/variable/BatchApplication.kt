package acktsap.sample.job.flowjob.steptransition.variable

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
        val testStep = stepBuilderFactory.get("testStep")
            .tasklet { _, _ ->
                println("run testTasklet")
                throw IllegalStateException().apply { stackTrace = arrayOf() }
            }
            .build()
        val transitionStep = stepBuilderFactory.get("transitionStep")
            .tasklet { _, _ ->
                println("run transitionTasklet")
                RepeatStatus.FINISHED
            }
            .build()

        return jobBuilderFactory.get("beforeJob")
            .start(testStep)
            .on("COMPLETED").end()
            .from(testStep).on("TEST").to(transitionStep)
            .from(testStep).on("*").stop()
            .end()
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        val testStep = batch {
            step("testStep") {
                tasklet { _, _ ->
                    println("run testTasklet")
                    throw IllegalStateException().apply { stackTrace = arrayOf() }
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    println("run transitionTasklet")
                    RepeatStatus.FINISHED
                }
            }
        }

        job("afterJob") {
            flows {
                step(testStep) {
                    on("COMPLETED") {
                        end()
                    }
                    on("TEST") {
                        step(transitionStep)
                    }
                    on("*") {
                        stop()
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
