package com.disneystreaming.mercury.demo.tc

import java.net.URI
import java.util.concurrent.Executors

import cats.effect.{Blocker, ContextShift, IO}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer, MultipleContainers}
import com.disneystreaming.mercury.demo.tc.models.Person
import eu.timepit.refined.auto._
import fs2.aws.s3.{BucketName, FileKey}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scanamo.{DynamoReadError, LocalDynamoDB, ScanamoCats, Table}
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import io.circe.generic.auto._
import io.github.howardjohn.scanamo.CirceDynamoFormat._
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._

import scala.concurrent.ExecutionContext

class TcDemoSpec extends AnyWordSpec with Matchers with ForAllTestContainer {
  val network = Network.newNetwork()

  val S3container: GenericContainer = new GenericContainer("minio/minio").configure { gc =>
    gc.withExposedPorts(9000)
    gc.addEnv("MINIO_ACCESS_KEY", "AKIAIOSFODNN7EXAMPLE")
    gc.addEnv("MINIO_SECRET_KEY", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
    gc.withCommand("server /data")
    gc.withNetwork(network)
    gc.withNetworkAliases("minio")
  }

  val createS3: GenericContainer = new GenericContainer("minio/mc").configure { gc =>
    gc.withCreateContainerCmdModifier(cmd => cmd.withEntrypoint("/bin/sh"))
    gc.setCommand(
      "-c",
      """/usr/bin/mc config host add testminio http://minio:9000 AKIAIOSFODNN7EXAMPLE wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY --api S3v4;
      /usr/bin/mc mb testminio/resources;
      /usr/bin/mc policy download testminio/resources;
      exit 0;
      """
    )
    gc.withNetwork(network)
    gc.dependsOn(S3container)
  }

  val dynamoDB = new GenericContainer(
    "amazon/dynamodb-local",
    waitStrategy = Some(new HttpWaitStrategy().forPath("/shell"))
  ).configure(_.withExposedPorts(8000))

  override val container = MultipleContainers(S3container, createS3, dynamoDB)

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit val blocker = Blocker.liftExecutionContext(
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(6))
  )

  val bucket = BucketName("resources")
  val fileKey = FileKey("jsontest.json")

  override def afterStart(): Unit = {
    val dynamoDbClient = LocalDynamoDB.client(dynamoDB.mappedPort(8000))
    LocalDynamoDB.createTable(dynamoDbClient)("persons")("name" -> S, "secondName" -> S)
  }

  def cleanUp(): IO[Unit] =
    IO.delay {
      val dynamoDbClient = LocalDynamoDB.client(dynamoDB.mappedPort(8000))
      LocalDynamoDB.deleteTable(dynamoDbClient)("persons")
      LocalDynamoDB.createTable(dynamoDbClient)("persons")("name" -> S, "secondName" -> S)
    }

  "TC Service " should {
    "store file in S3" in {
      val hostPort = S3container.mappedPort(9000)
      implicit val s3Client: S3Client = mkS3Client(hostPort)
      val data: List[Person] = Person("Kai", "Wang") :: Person("Dmytro", "Semenov") :: Nil
      saveRecords(data, bucket, fileKey).unsafeRunSync()
    }

    "read the S3 file and store in in DynamoDB" in {
      implicit val dynamoClient: AmazonDynamoDBAsync = LocalDynamoDB.client(dynamoDB.mappedPort(8000))
      implicit val s3Client: S3Client = mkS3Client(S3container.mappedPort(9000))
//Scanamo??? this is Scala client for DynamoDB
      val persons = Table[Person]("persons")

      val ioRes: IO[List[Either[DynamoReadError, Person]]] = for {
        _ <- readFromS3AndStoreInDynamo[IO](bucket, fileKey, persons)
        res <- ScanamoCats[IO](dynamoClient).exec(persons.scan())
      } yield res

      ioRes.unsafeRunSync() should be(Right(Person("Kai", "Wang")) :: Right(Person("Dmytro", "Semenov")) :: Nil)
    }
  }

  private def mkS3Client(s3Port: Int) = {
    val credentials =
      AwsBasicCredentials.create("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")

    S3Client
      .builder()
      .credentialsProvider(StaticCredentialsProvider.create(credentials))
      .endpointOverride(URI.create(s"http://localhost:$s3Port"))
      .region(Region.US_EAST_1)
      .build()
  }

}
