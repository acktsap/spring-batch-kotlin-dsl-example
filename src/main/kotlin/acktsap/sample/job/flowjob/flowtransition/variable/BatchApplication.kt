package acktsap.sample.job.flowjob.flowtransition.variable

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
        val testFlow = FlowBuilder<Flow>("testFlow")
            .start(
                stepBuilderFactory.get("testStep")
                    .tasklet { _, _ ->
                        println("run testTasklet")
                        throw IllegalStateException().apply { stackTrace = arrayOf() }
                    }
                    .build()
            )
            .build()
        val transitionStep = stepBuilderFactory.get("transitionStep")
            .tasklet { _, _ ->
                println("run transitionTasklet")
                RepeatStatus.FINISHED
            }
            .build()

        return jobBuilderFactory.get("beforeJob")
            .start(testFlow)
            .on("COMPLETED").to(transitionStep)
            .from(testFlow).on("TEST").fail()
            .on("*").stop()
            .end()
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        val testFlow = batch {
            flow("testFlow") {
                step("testStep") {
                    tasklet { _, _ ->
                        println("run testTasklet")
                        throw IllegalStateException().apply { stackTrace = arrayOf() }
                    }
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
                flow(testFlow) {
                    on("COMPLETED") {
                        step(transitionStep)
                    }
                    on("TEST") {
                        fail()
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
