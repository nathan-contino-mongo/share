import org.mongodb.scala._
import com.mongodb.ConnectionString
import com.mongodb.WriteConcern
import com.mongodb.connection.SslSettings

// tested using:
// scala version 2.12.7
// mongo-scala-driver 2.7.0

object MongoDBSCRAM extends App {
   val password = "admin".toCharArray()
   val credential = MongoCredential.createScramSha1Credential("admin", "admin", password);
   val connectionString = new ConnectionString("mongodb+srv://balboaclusterm10-5sfvt.mongodb.net/test");

   val settings = MongoClientSettings.builder()
      .applyConnectionString(connectionString)
      .credential(credential)
      .retryWrites(true)
      .writeConcern(WriteConcern.MAJORITY)
      .applyToSslSettings(
         (builder : SslSettings.Builder)
         => builder.enabled(true))
      .build();

   val client : MongoClient = MongoClient(settings);

   val db : MongoDatabase = client.getDatabase("testDB");
   val collection : MongoCollection[Document] = db.getCollection("testCol");
   collection.count().subscribe(new Observer[Long] {
      override def onNext(result: Long): Unit = println("\n\n****\n" + result + "\n****\n\n")
      override def onError(e: Throwable): Unit = println(s"onError: $e")
      override def onComplete(): Unit = println("onComplete")
   })
}
