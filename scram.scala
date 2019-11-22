import org.mongodb.scala._
import com.mongodb.ConnectionString
import com.mongodb.WriteConcern
import com.mongodb.connection.SslSettings

// tested using:
// scala version 2.12.7
// mongo-scala-driver 2.7.0

object MongoDBSCRAM extends App {
   // 1) You will need to convert your Atlas-provided PEM containing the cert/private keys into a PFX
   //    use openssl and the following line to create a PFX from your PEM:
   // openssl pkcs12 -export -in <x509>.pem -inkey <x509>.pem -out <x509>.pfx -certfile <x509>.pem
   //    and provide a password, which should match the second argument you pass to X509Certificate2
   // 2) Then you'll have to convert your pfx into a keystore:
   // keytool -importkeystore -destkeystore <x509>.keystore -srckeystore <x509>.pfx -srcstoretype pkcs12 -alias 1
   System.setProperty("javax.net.ssl.keyStore", "moe.keystore");
   System.setProperty("javax.net.ssl.keyStorePassword", "password");

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
