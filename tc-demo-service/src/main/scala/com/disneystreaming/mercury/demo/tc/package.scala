package com.disneystreaming.mercury.demo

import cats.effect.{Async, Blocker, Concurrent, ContextShift, IO}
import com.disneystreaming.mercury.demo.tc.models.Person
import fs2.Chunk
import fs2.aws.s3.{BucketName, FileKey, S3}
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import software.amazon.awssdk.services.s3.S3Client
import cats.implicits._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import io.circe.fs2.byteStreamParser
import org.scanamo.{ScanamoCats, Table}

package object tc {

  def saveRecords(records: List[Person], bucket: BucketName, fileKey: FileKey)(
    implicit client: S3Client,
    blocker: Blocker,
    c: Concurrent[IO],
    cs: ContextShift[IO]
  ): IO[Unit] =
    S3.create[IO](client, blocker).flatMap { s3 =>
      fs2.Stream
        .emits(records)
        .map(p => Printer.noSpaces.printToByteBuffer(p.asJson))
        .flatMap(ba => fs2.Stream.chunk(Chunk.byteBuffer(ba)))
        .covary[IO]
        .through(s3.uploadFile(bucket, fileKey))
        .compile
        .drain
    }

  def recordsStream[F[_]: Concurrent: ContextShift](bucket: BucketName, fileKey: FileKey)(
    implicit client: S3Client,
    blocker: Blocker
  ): fs2.Stream[F, Person] =
    for {
      s3 <- fs2.Stream.eval(S3.create[F](client, blocker))
      s <- s3.readFile(bucket, fileKey).through(byteStreamParser[F]).evalMap(_.as[Person].liftTo[F])
    } yield s

  def storeToDynamo[F[_]: Async](table: Table[Person], dynamoClient: AmazonDynamoDBAsync): fs2.Pipe[F, Person, Unit] =
    _.evalMap(person => ScanamoCats(dynamoClient).exec(table.put(person)))

  def readFromS3AndStoreInDynamo[F[_]: Concurrent: ContextShift](
    bucket: BucketName,
    fileKey: FileKey,
    table: Table[Person]
  )(
    implicit client: S3Client,
    dynamoClient: AmazonDynamoDBAsync,
    blocker: Blocker
  ): F[Unit] = recordsStream[F](bucket, fileKey).through(storeToDynamo[F](table, dynamoClient)).compile.drain

}
