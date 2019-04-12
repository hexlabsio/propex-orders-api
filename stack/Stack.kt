import io.hexlabs.kloudformation.module.serverless.Method
import io.hexlabs.kloudformation.module.serverless.serverless
import io.kloudformation.KloudFormation
import io.kloudformation.StackBuilder
import io.kloudformation.json
import io.kloudformation.property.aws.lambda.function.code

class Stack : StackBuilder {
    override fun KloudFormation.create(args: List<String>) {
        serverless("propex-api", "dev", +"hexlabs-deployments") {
            serverlessFunction(
                functionId = "propex-api",
                codeLocationKey = +args.first(),
                handler = +"org.http4k.serverless.lambda.LambdaFunction::handle",
                runtime = +"java8"
            ) {
                lambdaFunction {
                    timeout(30)
                    environment { variables(json(mapOf("HTTP4K_BOOTSTRAP_CLASS" to "io.hexlabs.propex.api.RootApi"))) }
                }
                http(cors = true) {
                    path("orders") { Method.GET() }
                }
            }
            serverlessFunctionWithCode(
                functionId = "propex-api-schema",
                handler = +"index.go",
                runtime = +"nodejs8.10",
                code = +"""
                        exports.go = (event, context, callback) => {
                            console.log("HELLO WORLD")
                        }
                    """.trimIndent()
            ) {
                lambdaFunction{
                    timeout(30)
                }
            }
        }
    }
}