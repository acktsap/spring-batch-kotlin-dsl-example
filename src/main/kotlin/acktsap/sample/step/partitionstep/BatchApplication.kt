package acktsap.sample.step.partitionstep

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.extensions.kotlindsl.configuration.BatchDsl
import org.springframework.batch.extensions.kotlindsl.configuration.EnableBatchDsl
import org.springframework.batch.item.ExecutionContext
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
        val taskStep = stepBuilderFactory.get("partitionStep")
            .tasklet { _, chunkContext ->
                println("run taskStep in ${chunkContext.stepContext.stepName}")
                RepeatStatus.FINISHED
            }
            .build()

        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .partitioner("partitionStep") { gridSize ->
                        (1..gridSize).map { it.toString() to ExecutionContext() }
                            .toMap()
                    }
                    .step(taskStep)
                    .gridSize(2)
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        val partitionStep = batch {
            step("partitionStep") {
                tasklet { _, chunkContext ->
                    println("run taskStep in ${chunkContext.stepContext.stepName}")
                    RepeatStatus.FINISHED
                }
            }
        }

        job("afterJob") {
            steps {
                step("testStep") {
                    partitioner {
                        splitter("partitionStep") { gridSize ->
                            (1..gridSize).map { it.toString() to ExecutionContext() }
                                .toMap()
                        }
                        partitionHandler {
                            step(partitionStep)
                            gridSize(2)
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
