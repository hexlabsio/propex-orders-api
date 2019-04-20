import io.hexlabs.kloudformation.module.serverless.Method
import io.hexlabs.kloudformation.module.serverless.Serverless
import io.hexlabs.kloudformation.module.serverless.ServerlessFunction
import io.hexlabs.kloudformation.module.serverless.serverless
import io.kloudformation.KloudFormation
import io.kloudformation.StackBuilder
import io.kloudformation.Value
import io.kloudformation.function.GetAZs
import io.kloudformation.function.Select
import io.kloudformation.json
import io.kloudformation.model.KloudFormationTemplate.Builder.Companion.awsRegion
import io.kloudformation.model.Output
import io.kloudformation.model.iam.IamPolicyVersion
import io.kloudformation.model.iam.PrincipalType
import io.kloudformation.model.iam.actions
import io.kloudformation.model.iam.allResources
import io.kloudformation.model.iam.policyDocument
import io.kloudformation.property.aws.ec2.securitygroup.ingress
import io.kloudformation.resource.aws.ec2.securityGroup
import io.kloudformation.resource.aws.kms.key
import io.kloudformation.resource.aws.rds.DBInstance
import io.kloudformation.resource.aws.rds.dBInstance
import io.kloudformation.resource.aws.rds.dBSubnetGroup

typealias Lambda = io.kloudformation.resource.aws.lambda.Function.Builder

class Stack : StackBuilder {

    private fun KloudFormation.lambdas(securityGroupId: Value<String>, subnets: List<Value<String>>, databaseEndpoint: Value<String>, databasePort: Value<String>, codeLocation: String) =
        serverless("propex-api", "dev", +"hexlabs-deployments", privateConfig = Serverless.PrivateConfig(+listOf(securityGroupId), +subnets)) {
            val defaultLambdaConfig: Lambda.(ServerlessFunction.Parts.LambdaProps) -> Unit = {
                timeout(30)
                memorySize(2048)
                environment { variables(json(mapOf(
                    "HTTP4K_BOOTSTRAP_CLASS" to "io.hexlabs.propex.api.RootApi",
                    "DATABASE_ENDPOINT" to databaseEndpoint,
                    "DATABASE_PORT" to databasePort
                ))) }
            }
            serverlessFunction("propex-api", +codeLocation, +"org.http4k.serverless.lambda.LambdaFunction::handle", +"java8") {
                lambdaFunction(defaultLambdaConfig)
                http(cors = true) {
                    path("orders") { Method.GET(); Method.POST(); }
                    path("products") { Method.GET(); Method.POST(); Method.DELETE(); }
                }
            }
            serverlessFunction("propex-db-init", +codeLocation, +"io.hexlabs.propex.db.DbInit::handle", +"java8") {
                lambdaFunction(defaultLambdaConfig)
            }
        }

    private fun KloudFormation.database(lambdaSecurityGroupId: Value<String>, subnets: List<Value<String>>): DBInstance {
        val dbSecurityGroup = securityGroup(+"Access to Propex DB") {
            securityGroupIngress(listOf(
                ingress(+"tcp") {
                    fromPort(Value.Of(5432))
                    toPort(Value.Of(5432))
                    sourceSecurityGroupId(lambdaSecurityGroupId)
                },
                ingress(+"tcp") {
                    fromPort(Value.Of(0))
                    toPort(Value.Of(9999))
                    cidrIp("0.0.0.0/0")
                }
            ))
        }
        val subnetGroup = dBSubnetGroup(+"propex-database-subnet-group", +subnets)
        return dBInstance(+"db.t3.micro") {
            dBName("PropexDev")
            dBInstanceIdentifier("propex-dev-1")
            allocatedStorage("10")
            engine("postgres")
            publiclyAccessible(true)
            availabilityZone(Select(+"0", GetAZs(awsRegion)))
            vPCSecurityGroups(listOf(dbSecurityGroup.GroupId()))
            dBSubnetGroupName(subnetGroup.ref())
            masterUsername("postgres")
            masterUserPassword("postgres")
            port(+"5432")
        }
    }

    override fun KloudFormation.create(args: List<String>) {
        val buildUserArn = "arn:aws:iam::662158168835:user/circle-ci"
        val subnets = listOf(+"subnet-c38de28b", +"subnet-cfc11895")
        val lambdaSecurityGroup = securityGroup(+"Propex Lambda")
        val database = database(lambdaSecurityGroup.GroupId(), subnets)
        val serverless = lambdas(lambdaSecurityGroup.GroupId(), subnets, database.EndpointAddress(), database.EndpointPort(), codeLocation = args.first())
        key(policyDocument(id = "propex-key-policy", version = IamPolicyVersion.V2.version) {
            statement(
                sid = "Allow encrypt for build user",
                resource = allResources,
                action = actions("kms:GenerateDataKey*", "kms:Describe*", "kms:Encrypt", "kms:Create*", "kms:Put*")
            ) { principal(PrincipalType.AWS, listOf(+buildUserArn)) }
            statement(
                sid = "Allow decrypt for lambda",
                resource = allResources,
                action = actions("kms:GenerateDataKey*", "kms:DescribeKey", "kms:Decrypt")
            ) { principal(PrincipalType.AWS, (serverless.functions.mapNotNull { it.role }).distinct().map { it.Arn() }) }
        })
        outputs("DbInitFunction" to Output(serverless.functions.last().function.ref()))
        outputs("DatabaseDNS" to Output(database.EndpointAddress()))
        outputs("DatabasePort" to Output(database.EndpointPort()))
    }
}